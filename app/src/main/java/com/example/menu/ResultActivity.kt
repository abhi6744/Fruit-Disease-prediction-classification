package com.example.menu

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    data class DiseaseInfo(
        val name: String,
        val cause: String,
        val precaution: String
    )

    private val diseaseDatabase = mapOf(
        "apple_scab" to DiseaseInfo(
            name = "Apple Scab",
            cause = "Caused by the fungus Venturia inaequalis. It spreads in cool, wet weather and survives on infected fallen leaves.",
            precaution = "Apply fungicide early in spring, remove fallen infected leaves, and improve air circulation around trees."
        ),
        "black_rot" to DiseaseInfo(
            name = "Black Rot",
            cause = "Caused by the fungus Botryosphaeria obtusa. It often enters through wounds and spreads in warm, humid conditions.",
            precaution = "Prune infected branches, remove mummified fruits, and apply a recommended fungicide."
        ),
        "cedar_apple_rust" to DiseaseInfo(
            name = "Cedar Apple Rust",
            cause = "Caused by the fungus Gymnosporangium juniperi-virginianae. It usually spreads between juniper and apple hosts.",
            precaution = "Remove nearby juniper hosts if possible and apply fungicide during the spring infection period."
        ),
        "anthracnose" to DiseaseInfo(
            name = "Anthracnose",
            cause = "Caused by fungal infection, commonly favored by warm and humid conditions.",
            precaution = "Use appropriate fungicide, avoid overhead watering, and maintain orchard hygiene."
        ),
        "bacterial_canker" to DiseaseInfo(
            name = "Bacterial Canker",
            cause = "Caused by bacterial infection that enters through wounds or natural openings and spreads more in wet conditions.",
            precaution = "Prune infected parts carefully, disinfect tools, and use suitable bactericide where recommended."
        ),
        "cutting_weevil" to DiseaseInfo(
            name = "Cutting Weevil Damage",
            cause = "Caused by weevil attack on young shoots and plant tissue.",
            precaution = "Remove affected shoots, monitor infestation, and use proper insect management practices."
        ),
        "die_back" to DiseaseInfo(
            name = "Die Back",
            cause = "Often associated with fungal infection entering through wounds and stressed plant tissue.",
            precaution = "Prune dead branches, protect cut surfaces, and avoid plant stress."
        ),
        "gall_midge" to DiseaseInfo(
            name = "Gall Midge",
            cause = "Caused by gall midge insect infestation affecting young leaves and shoots.",
            precaution = "Remove affected leaves and apply insect control measures at early stages."
        ),
        "powdery_mildew" to DiseaseInfo(
            name = "Powdery Mildew",
            cause = "Caused by fungal infection that appears as powdery growth, especially in humid conditions.",
            precaution = "Use sulfur or recommended fungicide, improve airflow, and avoid excess nitrogen."
        ),
        "sooty_mould" to DiseaseInfo(
            name = "Sooty Mould",
            cause = "Caused by fungi growing on honeydew secreted by sap-sucking insects.",
            precaution = "Control the insect infestation first and clean affected foliage if needed."
        ),
        "black_spot" to DiseaseInfo(
            name = "Black Spot",
            cause = "Caused by fungal infection that spreads in wet and humid conditions.",
            precaution = "Remove infected material and apply suitable fungicide at intervals."
        ),
        "canker" to DiseaseInfo(
            name = "Citrus Canker",
            cause = "Caused by bacterial infection that spreads through rain splash, tools, and infected material.",
            precaution = "Remove infected parts, disinfect tools, and use preventive bactericide where appropriate."
        ),
        "greening" to DiseaseInfo(
            name = "Citrus Greening (HLB)",
            cause = "Caused by bacterial infection spread by citrus psyllid. It is a serious citrus disease.",
            precaution = "Remove infected trees where necessary and control the psyllid vector using proper management."
        ),
        "melanose" to DiseaseInfo(
            name = "Melanose",
            cause = "Caused by the fungus Diaporthe citri, often spreading from dead twigs during wet weather.",
            precaution = "Prune dead wood and apply protective fungicide when needed."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val fruit = intent.getStringExtra("fruit") ?: "apple"
        val resultType = intent.getStringExtra("result_type") ?: "not_leaf"
        val label = intent.getStringExtra("label") ?: ""
        val confidence = intent.getFloatExtra("confidence", 0f)
        val confidencePercent = (confidence * 100).toInt()

        val tvTitle = findViewById<TextView>(R.id.tvResultTitle)
        val tvSubtitle = findViewById<TextView>(R.id.tvResultSubtitle)
        val tvConfidence = findViewById<TextView>(R.id.tvConfidence)
        val layoutDiseaseInfo = findViewById<LinearLayout>(R.id.layoutDiseaseInfo)
        val tvDiseaseName = findViewById<TextView>(R.id.tvDiseaseName)
        val tvCause = findViewById<TextView>(R.id.tvCause)
        val tvPrecaution = findViewById<TextView>(R.id.tvPrecaution)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val fruitName = fruit.replaceFirstChar { it.uppercase() }

        when (resultType) {
            "not_leaf" -> {
                tvTitle.text = "Invalid or Unclear Image"
                tvSubtitle.text = "Please upload a clear close-up image of the selected fruit leaf only."
                tvConfidence.visibility = View.GONE
                layoutDiseaseInfo.visibility = View.GONE
            }

            "healthy" -> {
                tvTitle.text = "Healthy Leaf"
                tvSubtitle.text = "The selected $fruitName leaf appears healthy."
                tvConfidence.text = "Confidence: $confidencePercent%"
                tvConfidence.visibility = View.VISIBLE
                layoutDiseaseInfo.visibility = View.GONE
            }

            "diseased" -> {
                val normalizedKey = normalizeLabel(label)
                val info = diseaseDatabase[normalizedKey]

                tvTitle.text = "Disease Detected"
                tvSubtitle.text = "A disease may have been detected on the selected $fruitName leaf."
                tvConfidence.text = "Confidence: $confidencePercent%"
                tvConfidence.visibility = View.VISIBLE
                layoutDiseaseInfo.visibility = View.VISIBLE

                if (info != null) {
                    tvDiseaseName.text = info.name
                    tvCause.text = info.cause
                    tvPrecaution.text = info.precaution
                } else {
                    tvDiseaseName.text = prettifyLabel(label)
                    tvCause.text = "Detailed cause information is not available for this class label."
                    tvPrecaution.text = "Please consult an agricultural expert and retest with a clearer leaf image."
                }
            }

            else -> {
                tvTitle.text = "Invalid Result"
                tvSubtitle.text = "Unable to analyze this image properly. Please try again with a clear leaf photo."
                tvConfidence.visibility = View.GONE
                layoutDiseaseInfo.visibility = View.GONE
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun normalizeLabel(label: String): String {
        var key = label.trim().lowercase()

        if (key.contains("___")) {
            key = key.substringAfterLast("___")
        }

        key = key.replace(" ", "_")
            .replace("-", "_")
            .replace("/", "_")
            .replace("__", "_")

        return key
    }

    private fun prettifyLabel(label: String): String {
        var text = label.trim()

        if (text.contains("___")) {
            text = text.substringAfterLast("___")
        }

        return text.replace("_", " ")
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }
}