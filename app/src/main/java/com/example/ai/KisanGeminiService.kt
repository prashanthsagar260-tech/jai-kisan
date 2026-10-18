package com.example.ai

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "KisanGeminiService"

data class GeminiAgriResponse(
    val replyTelugu: String,
    val replyEnglish: String,
    val keyActionItems: List<String> = emptyList(),
    val isRealGeminiResponse: Boolean = true
)

data class GeminiCropDiagnosis(
    val cropNameTe: String,
    val cropNameEn: String,
    val conditionNameTe: String,
    val conditionNameEn: String,
    val severity: String,
    val pathogenType: String,
    val symptomsTe: String,
    val organicRemedyTe: String,
    val chemicalRemedyTe: String,
    val preventiveTipsTe: String,
    val spokenSummaryTe: String
)

class KisanGeminiService {

    // Structured JSON schema for dual-language Telugu & English agricultural consultation
    private val agriResponseSchema = Schema.obj(
        properties = mapOf(
            "replyTelugu" to Schema.string(description = "Detailed, respectful agricultural answer in simple, natural Telugu (తెలుగు) for the farmer"),
            "replyEnglish" to Schema.string(description = "English translation/summary of the agricultural answer"),
            "keyActionItems" to Schema.array(
                items = Schema.string(),
                description = "1 to 3 concise, practical next action items for the farmer in Telugu"
            )
        )
    )

    private val cropDiagnosisSchema = Schema.obj(
        properties = mapOf(
            "cropNameTe" to Schema.string(description = "Crop name in Telugu (e.g., మిర్చి, వరి, పత్తి, టమాటా)"),
            "cropNameEn" to Schema.string(description = "Crop name in English"),
            "conditionNameTe" to Schema.string(description = "Diagnosed disease or pest name in Telugu"),
            "conditionNameEn" to Schema.string(description = "Diagnosed disease or pest name in English"),
            "severity" to Schema.string(description = "Severity level: Low, Moderate, or High"),
            "pathogenType" to Schema.string(description = "Fungal, Bacterial, Viral, Pest, or Nutrient Deficiency"),
            "symptomsTe" to Schema.string(description = "Key visual symptoms identified on leaf, fruit, or stem in Telugu"),
            "organicRemedyTe" to Schema.string(description = "Organic/natural remedy with recommended dosage in Telugu"),
            "chemicalRemedyTe" to Schema.string(description = "Standard chemical pesticide/fungicide recommendation with dosage in Telugu"),
            "preventiveTipsTe" to Schema.string(description = "Preventive agricultural practices in Telugu"),
            "spokenSummaryTe" to Schema.string(description = "Short 2-sentence voice summary in Telugu for voice readout to the farmer")
        )
    )

    // Lazy initialization of Gemini 3.8 Flash models as mandated by the Firebase AI Logic skill
    private val agriConsultantModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = agriResponseSchema
                temperature = 0.4f
            },
            systemInstruction = content {
                text(
                    """
                    You are Jai Kisan AI - an empathetic, reliable, and expert agricultural advisor serving farmers in Telangana and Andhra Pradesh, India.
                    You assist farmers with:
                    - Real-time mandi price trends, market arrival advice, and best times to sell.
                    - Weather impacts, rainfall forecast adaptations, and irrigation schedules.
                    - Crop advisory for regional crops: Teja Chilli, Paddy (Sona Masuri, BPT), Cotton (Bt Cotton), Maize, Tomato, Groundnut, Turmeric.
                    - Pest & disease remedies (providing BOTH safe organic/neem remedies and chemical sprays with exact dosages per litre or acre).
                    - Cold storage and logistics advice for preserving harvested produce.
                    Always maintain an encouraging, respectful tone suitable for Indian farmers. Ensure Telugu output is authentic, clear, and easy to understand when spoken aloud.
                    """.trimIndent()
                )
            }
        )
    }

    private val visionDiagnosisModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = cropDiagnosisSchema
                temperature = 0.2f
            },
            systemInstruction = content {
                text(
                    """
                    You are an expert crop pathologist and agronomist examining crop photos submitted by farmers in South India.
                    Analyze the submitted image carefully:
                    - Identify the crop (e.g., Chilli, Paddy, Cotton, Tomato, Groundnut, Turmeric).
                    - Inspect leaves, stems, fruit for lesions, discolorations, curling, fungal spots, insect damage, or rot.
                    - Provide a highly accurate diagnosis along with practical, certified organic and chemical treatment recommendations.
                    - Provide all advisory text in clear Telugu.
                    """.trimIndent()
                )
            }
        )
    }

    /**
     * Ask Gemini agricultural advisor a question in Telugu or English.
     */
    suspend fun askAgriAdvisor(userQuery: String): Result<GeminiAgriResponse> {
        return try {
            withTimeout(60_000L) {
                val prompt = "రైతు అడిగిన ప్రశ్న (Farmer's inquiry): $userQuery"
                val response = agriConsultantModel.generateContent(prompt)
                val jsonText = response.text ?: throw IllegalStateException("Gemini returned empty response text")

                val json = JSONObject(jsonText)
                val replyTe = json.optString("replyTelugu", "")
                val replyEn = json.optString("replyEnglish", "")
                val actionsJson = json.optJSONArray("keyActionItems")
                val actionList = mutableListOf<String>()
                if (actionsJson != null) {
                    for (i in 0 until actionsJson.length()) {
                        actionList.add(actionsJson.getString(i))
                    }
                }

                Result.success(
                    GeminiAgriResponse(
                        replyTelugu = replyTe.ifBlank { "సమాచారం సిద్ధంగా ఉంది." },
                        replyEnglish = replyEn.ifBlank { "Information is ready." },
                        keyActionItems = actionList,
                        isRealGeminiResponse = true
                    )
                )
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini generation timed out after 60s", e)
            Result.failure(e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API consultation failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Multimodal crop disease diagnosis using Gemini 3.8 Flash Vision.
     */
    suspend fun diagnoseCropPhoto(bitmap: Bitmap, cropHint: String?): Result<GeminiCropDiagnosis> {
        return try {
            withTimeout(60_000L) {
                val promptText = if (!cropHint.isNullOrBlank()) {
                    "ఈ పంట చిత్రం $cropHint (Crop: $cropHint). ఆకు లేదా కాయపై ఉన్న తెగులును విశ్లేషించి చికిత్స మరియు మందుల వివరాలు ఇవ్వండి."
                } else {
                    "ఈ పంట చిత్రంలో ఉన్న ఆకు లేదా కాయ లక్షణాలను పరిశీలించి తెగులు మరియు నివారణోపాయాలను అందించండి."
                }

                val response = visionDiagnosisModel.generateContent(
                    content {
                        image(bitmap)
                        text(promptText)
                    }
                )

                val jsonText = response.text ?: throw IllegalStateException("Gemini vision returned empty response")
                val json = JSONObject(jsonText)

                val diagnosis = GeminiCropDiagnosis(
                    cropNameTe = json.optString("cropNameTe", cropHint ?: "పంట"),
                    cropNameEn = json.optString("cropNameEn", cropHint ?: "Crop"),
                    conditionNameTe = json.optString("conditionNameTe", "లక్షణాలు గుర్తించబడ్డాయి"),
                    conditionNameEn = json.optString("conditionNameEn", "Identified Condition"),
                    severity = json.optString("severity", "మధ్యస్థం (Moderate)"),
                    pathogenType = json.optString("pathogenType", "శిలీంధ్రం (Fungal)"),
                    symptomsTe = json.optString("symptomsTe", "ఆకులపై మచ్చలు మరియు రంగు మార్పు."),
                    organicRemedyTe = json.optString("organicRemedyTe", "వేపనూనె 5ml లీటరు నీటికి కలిపి పిచికారీ చేయండి."),
                    chemicalRemedyTe = json.optString("chemicalRemedyTe", "కాపర్ ఆక్సిక్లోరైడ్ 3గ్రా లీటరు నీటికి పిచికారీ చేయండి."),
                    preventiveTipsTe = json.optString("preventiveTipsTe", "సరైన నీటి పారుదల మరియు గాలి వెలుతురు ఉండేలా చూసుకోండి."),
                    spokenSummaryTe = json.optString("spokenSummaryTe", "పంటలో తెగులు గుర్తించబడింది. సకాలంలో సిఫార్సు చేసిన మందులను పిచికారీ చేయండి.")
                )

                Result.success(diagnosis)
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini vision diagnosis timed out", e)
            Result.failure(e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini vision diagnosis error: ${e.message}", e)
            Result.failure(e)
        }
    }
}
