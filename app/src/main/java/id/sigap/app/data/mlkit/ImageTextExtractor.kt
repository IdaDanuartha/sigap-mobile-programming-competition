package id.sigap.app.data.mlkit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import id.sigap.app.core.util.Outcome
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/** Reads text (OCR) and QR payloads from an image on device. The image is never stored. */
class ImageTextExtractor(private val context: Context) {

    /** Returns recognized text plus any QR or barcode values, joined by new lines. */
    suspend fun extract(uri: Uri): Outcome<String> = try {
        val image = buildEnhancedInputImage(uri)
        val text = recognizeText(image)
        val codes = scanCodes(image)
        Outcome.Success((listOf(text) + codes).filter { it.isNotBlank() }.joinToString("\n"))
    } catch (e: Exception) {
        Outcome.Failure(e)
    }

    @Suppress("DEPRECATION")
    private fun buildEnhancedInputImage(uri: Uri): InputImage {
        val bitmap = runCatching {
            val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            // Upscale smaller screenshots so ML Kit OCR reads small chat text reliably
            if (raw.width in 1..1079) {
                val scale = (1200f / raw.width).coerceAtMost(2.5f)
                val scaled = Bitmap.createScaledBitmap(
                    raw,
                    (raw.width * scale).toInt(),
                    (raw.height * scale).toInt(),
                    true,
                )
                if (scaled !== raw) raw.recycle()
                scaled
            } else {
                raw
            }
        }.getOrNull()

        return if (bitmap != null) {
            InputImage.fromBitmap(bitmap, 0)
        } else {
            InputImage.fromFilePath(context, uri)
        }
    }

    private suspend fun recognizeText(image: InputImage): String =
        suspendCancellableCoroutine { cont ->
            val client = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            client.process(image)
                .addOnSuccessListener { result ->
                    val ordered = result.textBlocks
                        .sortedBy { it.boundingBox?.top ?: 0 }
                        .joinToString("\n") { it.text.trim() }
                        .ifBlank { result.text }
                    cont.resume(ordered)
                }
                .addOnFailureListener { cont.resumeWithException(it) }
            cont.invokeOnCancellation { client.close() }
        }

    private suspend fun scanCodes(image: InputImage): List<String> =
        suspendCancellableCoroutine { cont ->
            val client = BarcodeScanning.getClient()
            client.process(image)
                .addOnSuccessListener { codes -> cont.resume(codes.mapNotNull { it.rawValue }) }
                .addOnFailureListener { cont.resume(emptyList()) }
            cont.invokeOnCancellation { client.close() }
        }
}
