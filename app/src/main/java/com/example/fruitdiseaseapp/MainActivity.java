package com.example.fruitdiseaseapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    TextView txtPlaceholder;
    Button btnCamera, btnGallery, btnPredict;
    Button btnApple, btnOrange, btnMango;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_IMAGE = 2;

    Bitmap selectedBitmap = null;
    String selectedFruit = "Apple";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        txtPlaceholder = findViewById(R.id.txtPlaceholder);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        btnPredict = findViewById(R.id.btnPredict);

        btnApple = findViewById(R.id.btnApple);
        btnOrange = findViewById(R.id.btnOrange);
        btnMango = findViewById(R.id.btnMango);

        // CAMERA PERMISSION FIX 🔥
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 100);
        }

        // FRUIT SELECTION
        btnApple.setOnClickListener(v -> {
            selectedFruit = "Apple";
            selectFruit(btnApple);
        });

        btnOrange.setOnClickListener(v -> {
            selectedFruit = "Orange";
            selectFruit(btnOrange);
        });

        btnMango.setOnClickListener(v -> {
            selectedFruit = "Mango";
            selectFruit(btnMango);
        });

        // CAMERA
        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        });

        // GALLERY
        btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        // PREDICT
        btnPredict.setOnClickListener(v -> {

            if (selectedBitmap == null) {
                Toast.makeText(this, "Select image first", Toast.LENGTH_SHORT).show();
                return;
            }

            String disease = "Healthy";
            float confidence = 97.3f;

            Intent intent = new Intent(MainActivity.this, ResultActivity.class);

            // SEND IMAGE
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            selectedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            intent.putExtra("image", byteArray);
            intent.putExtra("fruit", selectedFruit);
            intent.putExtra("disease", disease);
            intent.putExtra("confidence", confidence);

            startActivity(intent);
        });
    }

    // HANDLE IMAGE RESULT
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

                txtPlaceholder.setVisibility(TextView.GONE);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // BUTTON HIGHLIGHT
    private void selectFruit(Button selectedBtn) {

        btnApple.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
        btnOrange.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
        btnMango.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));

        selectedBtn.setBackgroundTintList(getColorStateList(android.R.color.holo_green_dark));
    }
}