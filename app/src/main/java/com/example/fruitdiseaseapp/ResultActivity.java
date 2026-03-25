package com.example.fruitdiseaseapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ResultActivity extends AppCompatActivity {

    ImageView resultImage;
    TextView tvFruit, tvResult, tvConfidence, tvDetails;
    CardView statusCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        resultImage = findViewById(R.id.resultImage);
        tvFruit = findViewById(R.id.tvFruit);
        tvResult = findViewById(R.id.tvResult);
        tvConfidence = findViewById(R.id.tvConfidence);
        tvDetails = findViewById(R.id.tvDetails);
        statusCard = findViewById(R.id.statusCard);

        // Get data
        byte[] imageBytes = getIntent().getByteArrayExtra("image");
        String fruit = getIntent().getStringExtra("fruit");
        String disease = getIntent().getStringExtra("disease");
        float confidence = getIntent().getFloatExtra("confidence", 0);

        // Set image
        if (imageBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            resultImage.setImageBitmap(bitmap);
        }

        // Set text
        tvFruit.setText("Fruit: " + fruit);
        tvConfidence.setText("Confidence: " + String.format("%.1f", confidence) + "%");

        if (disease != null && disease.equalsIgnoreCase("Healthy")) {
            statusCard.setCardBackgroundColor(Color.parseColor("#2E7D32"));
            tvResult.setText("Healthy Fruit");
        } else if (disease != null) {
            statusCard.setCardBackgroundColor(Color.parseColor("#C62828"));
            tvResult.setText("Disease: " + disease);
        } else {
            tvResult.setText("No result");
        }

        tvDetails.setText(getDiseaseInfo(disease));
    }

    private String getDiseaseInfo(String disease) {

        if (disease == null) return "No data available";

        switch (disease.toLowerCase()) {

            case "healthy":
                return "Cause:\nNo disease\n\nSymptoms:\nFresh fruit\n\nPrevention:\nProper care\n\nCure:\nNot needed";

            case "leaf spot":
                return "Cause:\nFungal infection\n\nSymptoms:\nSpots on leaves\n\nPrevention:\nAvoid moisture\n\nCure:\nUse fungicide";

            default:
                return "Cause:\nUnknown\n\nSymptoms:\nVaries\n\nPrevention:\nGeneral care\n\nCure:\nConsult expert";
        }
    }
}