package com.example.fruitdiseaseapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    ImageView resultImage;
    TextView tvResult, tvConfidence, tvDetails;
    LinearLayout statusCard;
    ProgressBar progressConfidence;
    Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        resultImage = findViewById(R.id.resultImage);
        tvResult = findViewById(R.id.tvResult);
        tvConfidence = findViewById(R.id.tvConfidence);
        tvDetails = findViewById(R.id.tvDetails);
        statusCard = findViewById(R.id.statusCard);
        progressConfidence = findViewById(R.id.progressConfidence);
        btnBack = findViewById(R.id.btnBack);

        // GET DATA
        byte[] imageBytes = getIntent().getByteArrayExtra("image");
        String disease = getIntent().getStringExtra("disease");
        float confidence = getIntent().getFloatExtra("confidence", 0);

        // SET IMAGE
        if (imageBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            resultImage.setImageBitmap(bitmap);
        }

        // SET TEXT
        tvConfidence.setText("Confidence: " + String.format("%.1f", confidence) + "%");
        progressConfidence.setProgress((int) confidence);

        if (disease != null && disease.equalsIgnoreCase("Healthy")) {
            tvResult.setText("Healthy Fruit");
            statusCard.setBackgroundColor(Color.parseColor("#2E7D32"));
        } else {
            tvResult.setText("Disease: " + disease);
            statusCard.setBackgroundColor(Color.parseColor("#C62828"));
        }

        tvDetails.setText(getDiseaseInfo(disease));

        // BACK BUTTON
        btnBack.setOnClickListener(v -> finish());
    }

    private String getDiseaseInfo(String disease) {

        if (disease == null) return "No data available";

        switch (disease.toLowerCase()) {

            case "healthy":
                return "Cause:\nNo disease\n\nSymptoms:\nFresh fruit\n\nPrevention:\nMaintain hygiene\n\nCure:\nNot required";

            case "leaf spot":
                return "Cause:\nFungal infection\n\nSymptoms:\nSpots on leaves\n\nPrevention:\nAvoid excess moisture\n\nCure:\nUse fungicide";

            default:
                return "Cause:\nUnknown\n\nSymptoms:\nVaries\n\nPrevention:\nGeneral care\n\nCure:\nConsult expert";
        }
    }
}