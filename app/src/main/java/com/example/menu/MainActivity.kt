package com.example.menu

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var classifier: DiseaseClassifier
    private lateinit var imgPreview: ImageView
    private lateinit var tvSelectedFruit: TextView
    private lateinit var btnApple: Button
    private lateinit var btnMango: Button
    private lateinit var btnOrange: Button
    private lateinit var btnGallery: Button
    private lateinit var btnCamera: Button
    private lateinit var btnAnalyse: Button

    private var selectedFruit = "apple"
    private var currentBitmap: Bitmap? = null
    private var cameraImageUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val bitmap = getBitmapFromUri(uri)
                currentBitmap = bitmap
                imgPreview.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to load image from gallery", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            try {
                val bitmap = getBitmapFromUri(cameraImageUri!!)
                currentBitmap = bitmap
                imgPreview.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to load captured image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Camera capture cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imgPreview = findViewById(R.id.imgPreview)
        tvSelectedFruit = findViewById(R.id.tvSelectedFruit)
        btnApple = findViewById(R.id.btnApple)
        btnMango = findViewById(R.id.btnMango)
        btnOrange = findViewById(R.id.btnOrange)
        btnGallery = findViewById(R.id.btnGallery)
        btnCamera = findViewById(R.id.btnCamera)
        btnAnalyse = findViewById(R.id.btnAnalyse)

        classifier = DiseaseClassifier(this)

        updateFruitSelection("apple")

        btnApple.setOnClickListener { updateFruitSelection("apple") }
        btnMango.setOnClickListener { updateFruitSelection("mango") }
        btnOrange.setOnClickListener { updateFruitSelection("orange") }

        btnGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        btnCamera.setOnClickListener {
            checkCameraPermissionAndOpen()
        }

        btnAnalyse.setOnClickListener {
            analyseSelectedImage()
        }
    }

    private fun analyseSelectedImage() {
        val bitmap = currentBitmap
        if (bitmap == null) {
            Toast.makeText(this, "Please select or capture an image first", Toast.LENGTH_SHORT).show()
            return
        }

        val result = classifier.classify(bitmap)

        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("fruit", selectedFruit)

        when (result) {
            is ClassificationResult.Healthy -> {
                intent.putExtra("result_type", "healthy")
                intent.putExtra("label", result.label)
                intent.putExtra("confidence", result.confidence)
            }

            is ClassificationResult.Diseased -> {
                intent.putExtra("result_type", "diseased")
                intent.putExtra("label", result.label)
                intent.putExtra("confidence", result.confidence)
            }

            is ClassificationResult.LowConfidence -> {
                intent.putExtra("result_type", "not_leaf")
            }

            is ClassificationResult.Error -> {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                return
            }
        }

        startActivity(intent)
    }

    private fun updateFruitSelection(fruit: String) {
        selectedFruit = fruit

        try {
            classifier.loadModel(fruit)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load $fruit model", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedColor = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor("#1B5E20")
        )
        val normalColor = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor("#C5B3F2")
        )
        val whiteColor = getColor(android.R.color.white)

        btnApple.backgroundTintList = if (fruit == "apple") selectedColor else normalColor
        btnMango.backgroundTintList = if (fruit == "mango") selectedColor else normalColor
        btnOrange.backgroundTintList = if (fruit == "orange") selectedColor else normalColor

        btnApple.setTextColor(whiteColor)
        btnMango.setTextColor(whiteColor)
        btnOrange.setTextColor(whiteColor)

        val displayName = fruit.replaceFirstChar { it.uppercase() }
        tvSelectedFruit.text = "Selected: $displayName"
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        try {
            val photoFile = File.createTempFile("leaf_", ".jpg", cacheDir)
            cameraImageUri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                photoFile
            )
            cameraLauncher.launch(cameraImageUri!!)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        classifier.close()
    }
}