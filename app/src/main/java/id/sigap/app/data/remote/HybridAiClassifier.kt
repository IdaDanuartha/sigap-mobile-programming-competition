package id.sigap.app.data.remote

import android.util.Log
import id.sigap.app.BuildConfig
import id.sigap.app.domain.model.AiAnalysisResult
import id.sigap.app.domain.model.ReasonCode
import id.sigap.app.domain.model.RiskLevel
import id.sigap.app.domain.model.ScamCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Hybrid Cloud AI Classifier:
 * - Primary: OpenAI GPT (gpt-4o-mini)
 * - Fallback: Google Gemini (cascades through free models: gemini-2.5-flash, gemini-3.5-flash, gemini-3.6-flash)
 * - Safe degradation: Returns null if offline or no keys configured, allowing the on-device rule engine to govern.
 */
class HybridAiClassifier(
    private val openAiApiKey: String = BuildConfig.OPENAI_API_KEY,
    private val geminiApiKey: String = BuildConfig.GEMINI_API_KEY,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(6, TimeUnit.SECONDS)
        .build(),
) {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val geminiFreeModels = listOf(
        "gemini-2.5-flash",
        "gemini-3.5-flash",
        "gemini-3.6-flash",
    )

    /**
     * Analyzes a privacy-redacted message using OpenAI first, falling back to Gemini if OpenAI fails.
     */
    suspend fun analyze(
        redactedText: String,
        detectedReasons: List<ReasonCode> = emptyList(),
        languageHint: String = "id",
    ): AiAnalysisResult? = withContext(Dispatchers.IO) {
        if (redactedText.isBlank()) return@withContext null

        val prompt = buildAnalysisPrompt(redactedText, detectedReasons, languageHint)

        // 1. Try OpenAI GPT first (Primary)
        if (openAiApiKey.isNotBlank()) {
            val openAiResult = tryOpenAi(prompt)
            if (openAiResult != null) {
                Log.d(TAG, "Analysis successfully completed via OpenAI GPT-4o-mini")
                return@withContext openAiResult
            }
            Log.w(TAG, "OpenAI failed or rate-limited. Falling back to Google Gemini...")
        }

        // 2. Fallback to Google Gemini (Cascade through all free models)
        if (geminiApiKey.isNotBlank()) {
            for (model in geminiFreeModels) {
                val geminiResult = tryGemini(model, prompt)
                if (geminiResult != null) {
                    Log.d(TAG, "Analysis successfully completed via Gemini fallback ($model)")
                    return@withContext geminiResult.copy(isFallback = openAiApiKey.isNotBlank())
                }
                Log.w(TAG, "Gemini $model failed or busy. Trying next free Gemini model...")
            }
        }

        Log.i(TAG, "No cloud AI succeeded or no keys provided. Falling back safely to On-Device Rule Engine.")
        null
    }

    private fun tryOpenAi(prompt: String): AiAnalysisResult? {
        return try {
            val payload = JSONObject().apply {
                put("model", "gpt-4o-mini")
                put("temperature", 0.2)
                put("response_format", JSONObject().put("type", "json_object"))
                put(
                    "messages",
                    JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", SYSTEM_INSTRUCTIONS)
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        })
                    },
                )
            }

            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer $openAiApiKey")
                .header("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "OpenAI error: HTTP ${response.code} - ${response.message}")
                    return null
                }
                val body = response.body?.string() ?: return null
                val root = JSONObject(body)
                val choices = root.optJSONArray("choices") ?: return null
                if (choices.length() == 0) return null
                val content = choices.getJSONObject(0).getJSONObject("message").getString("content")
                parseAiJson(content, provider = "OpenAI GPT-4o-mini")
            }
        } catch (e: Exception) {
            Log.w(TAG, "OpenAI exception: ${e.message}")
            null
        }
    }

    private fun tryGemini(model: String, prompt: String): AiAnalysisResult? {
        return try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$geminiApiKey"

            val combinedContent = "$SYSTEM_INSTRUCTIONS\n\n=== PESAN UNTUK DIANALISIS ===\n$prompt"

            val payload = JSONObject().apply {
                put(
                    "contents",
                    JSONArray().apply {
                        put(JSONObject().apply {
                            put(
                                "parts",
                                JSONArray().apply {
                                    put(JSONObject().put("text", combinedContent))
                                },
                            )
                        })
                    },
                )
                put(
                    "generationConfig",
                    JSONObject().apply {
                        put("temperature", 0.2)
                        put("responseMimeType", "application/json")
                    },
                )
            }

            val request = Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "Gemini $model error: HTTP ${response.code} - ${response.message}")
                    return null
                }
                val body = response.body?.string() ?: return null
                val root = JSONObject(body)
                val candidates = root.optJSONArray("candidates") ?: return null
                if (candidates.length() == 0) return null
                val parts = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts")
                val text = parts.getJSONObject(0).getString("text")
                parseAiJson(text, provider = "Google Gemini ($model)")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Gemini $model exception: ${e.message}")
            null
        }
    }

    private fun parseAiJson(rawJson: String, provider: String): AiAnalysisResult? {
        return try {
            val clean = rawJson.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val obj = JSONObject(clean)

            val verdictStr = obj.optString("verdict", "SAFE").uppercase()
            val level = when {
                verdictStr.contains("DANGER") -> RiskLevel.DANGER
                verdictStr.contains("CAUTION") -> RiskLevel.CAUTION
                else -> RiskLevel.SAFE
            }

            val catStr = obj.optString("category", "OTHER").uppercase()
            val category = runCatching { ScamCategory.valueOf(catStr) }.getOrDefault(ScamCategory.OTHER)
            val confidence = obj.optDouble("confidence", 0.85).toFloat().coerceIn(0.0f, 1.0f)

            val reasonsArray = obj.optJSONArray("reasons")
            val explanation = if (reasonsArray != null && reasonsArray.length() > 0) {
                (0 until reasonsArray.length()).joinToString("; ") { reasonsArray.getString(it) }
            } else {
                obj.optString("explanation", "Analisis pola pesan mencurigakan.")
            }

            val advice = obj.optString("advice", "Tetap waspada dan jangan berikan informasi rahasia.")

            AiAnalysisResult(
                provider = provider,
                level = level,
                category = category,
                confidence = confidence,
                explanation = explanation,
                advice = advice,
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse AI JSON response: ${e.message}")
            null
        }
    }

    private fun buildAnalysisPrompt(
        redactedText: String,
        detectedReasons: List<ReasonCode>,
        languageHint: String,
    ): String = buildString {
        appendLine("Teks pesan (data PII seperti nomor telepon dan rekening telah disamarkan demi privasi):")
        appendLine("\"\"\"")
        appendLine(redactedText)
        appendLine("\"\"\"")
        if (detectedReasons.isNotEmpty()) {
            appendLine("Sinyal heuristik awal yang terdeteksi: ${detectedReasons.joinToString()}")
        }
        appendLine("Bahasa pengguna: $languageHint")
    }

    companion object {
        private const val TAG = "HybridAiClassifier"

        private const val SYSTEM_INSTRUCTIONS = """
Kamu adalah Sigap AI, analis keamanan siber independen khusus deteksi penipuan online dan social engineering di Indonesia.
Analisis pesan yang diberikan secara objektif dan tentukan apakah pesan ini terindikasi penipuan, phishing, APK malware, atau aman.
Pesan mungkin mengandung token privasi seperti [PHONE], [NUMBER], [NAME], [EMAIL].

PENTING: Keluarkan HANYA satu objek JSON murni tanpa format markdown tambahan, dengan skema berikut:
{
  "verdict": "SAFE" | "CAUTION" | "DANGER",
  "category": "APK_MALWARE" | "FAKE_COURIER" | "FAKE_INVITATION" | "AUTHORITY_IMPERSONATION" | "BANK_PHISHING" | "FAKE_PRIZE" | "INVESTMENT" | "ONLINE_LOAN" | "ROMANCE" | "JOB_SCAM" | "MARKETPLACE_FRAUD" | "OTP_THEFT" | "OTHER",
  "confidence": 0.85,
  "reasons": ["alasan ringkas dan jelas dalam Bahasa Indonesia mengapa pesan ini aman/berbahaya"],
  "advice": "satu kalimat petunjuk langkah yang harus dilakukan pengguna dalam Bahasa Indonesia"
}
"""
    }
}

