package com.example.fruitdiseaseapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button btnCamera, btnGallery, btnPredict;
    Spinner spinner;
    ProgressBar progressBar;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_IMAGE = 2;

    Bitmap selectedBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = findViewById(R.id.spinnerFruit);
        imageView = findViewById(R.id.imageView);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        btnPredict = findViewById(R.id.btnPredict);
        progressBar = findViewById(R.id.progressBar);

        // Spinner data
        String[] fruits = {"Apple", "Banana", "Mango", "Orange"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, fruits);
        spinner.setAdapter(adapter);

        // Camera
        btnCamera.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        });

        // Gallery
        btnGallery.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, PICK_IMAGE);
        });

        // Predict
        btnPredict.setOnClickListener(v -> {

            if (selectedBitmap == null) {
                Toast.makeText(this, "Select image first", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            btnPredict.setEnabled(false);

            new Handler().postDelayed(() -> {

                progressBar.setVisibility(View.GONE);
                btnPredict.setEnabled(true);

                String fruit = spinner.getSelectedItem().toString();

                // Fake prediction (replace with model later)
                String disease = "Healthy";
                float confidence = 98.0f;

                // Convert image → byte[]
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                selectedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                // Send data
                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                intent.putExtra("image", byteArray);
                intent.putExtra("fruit", fruit);
                intent.putExtra("disease", disease);
                intent.putExtra("confidence", confidence);

                startActivity(intent);

            }, 1500);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            try {
                if (requestCode == REQUEST_IMAGE_CAPTURE) {
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    selectedBitmap = bitmap;
                    imageView.setImageBitmap(bitmap);
                }

                else if (requestCode == PICK_IMAGE) {
                    Uri uri = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    selectedBitmap = bitmap;
                    imageView.setImageBitmap(bitmap);
                }

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}