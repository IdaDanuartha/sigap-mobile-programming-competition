package id.sigap.app.data.mlkit

import android.content.Context
import android.net.Uri
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
        val image = InputImage.fromFilePath(context, uri)
        val text = recognizeText(image)
        val codes = scanCodes(image)
        Outcome.Success((listOf(text) + codes).filter { it.isNotBlank() }.joinToString("\n"))
    } catch (e: Exception) {
        Outcome.Failure(e)
    }

    private suspend fun recognizeText(image: InputImage): String =
        suspendCancellableCoroutine { cont ->
            val client = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            client.process(image)
                .addOnSuccessListener { cont.resume(it.text) }
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
