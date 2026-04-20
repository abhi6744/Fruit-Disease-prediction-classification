package com.example.menu

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.abs

class DiseaseClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()
    private var outputClassesCount: Int = 0

    companion object {
        private const val IMG_SIZE = 224
        private const val PIXEL_SIZE = 3
        private const val CONFIDENCE_THRESHOLD = 0.80f
        private const val UNCERTAIN_THRESHOLD = 0.60f
        private const val MARGIN_THRESHOLD = 0.15f

        // Leaf gate thresholds
        private const val GREEN_RATIO_THRESHOLD = 0.18f
        private const val BROWN_YELLOW_RATIO_THRESHOLD = 0.18f
        private const val COMBINED_LEAF_THRESHOLD = 0.20f
    }

    fun loadModel(fruit: String) {
        close()

        val assetFileDescriptor = context.assets.openFd("${fruit}_disease.tflite")
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength

        val modelBuffer = fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            startOffset,
            declaredLength
        )

        val options = Interpreter.Options().apply {
            setNumThreads(4)
        }

        interpreter = Interpreter(modelBuffer, options)
        interpreter?.allocateTensors()

        labels = context.assets.open("${fruit}_labels.txt")
            .bufferedReader()
            .readLines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val outputShape = interpreter?.getOutputTensor(0)?.shape()
        outputClassesCount = if (outputShape != null && outputShape.size >= 2) {
            outputShape[1]
        } else {
            labels.size
        }
    }

    fun classify(bitmap: Bitmap): ClassificationResult {
        val interpreter = interpreter ?: return ClassificationResult.Error("Model not loaded")
        if (labels.isEmpty()) return ClassificationResult.Error("Labels not loaded")

        // Stage 1: Leaf gate — reject non-leaf images before running the model
        if (!isLikelyLeaf(bitmap)) {
            return ClassificationResult.LowConfidence
        }

        // Stage 2: Run the disease classification model
        return try {
            val safeBitmap = ensureSoftwareBitmap(bitmap)
            val resizedBitmap = Bitmap.createScaledBitmap(safeBitmap, IMG_SIZE, IMG_SIZE, true)
            val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)

            val classCount = if (outputClassesCount > 0) outputClassesCount else labels.size
            val outputArray = Array(1) { FloatArray(classCount) }

            interpreter.run(inputBuffer, outputArray)

            val scores = outputArray[0]
            if (scores.isEmpty()) {
                return ClassificationResult.Error("Empty prediction output")
            }

            val safeCount = minOf(scores.size, labels.size)
            if (safeCount == 0) {
                return ClassificationResult.Error("Model output does not match labels")
            }

            val indexedScores = (0 until safeCount)
                .map { index -> index to scores[index] }
                .sortedByDescending { it.second }

            val top1Index = indexedScores[0].first
            val top1Score = indexedScores[0].second
            val top1Label = labels.getOrElse(top1Index) { "Unknown" }

            val top2Score = if (indexedScores.size > 1) indexedScores[1].second else 0f
            val scoreMargin = abs(top1Score - top2Score)

            if (top1Score < UNCERTAIN_THRESHOLD) {
                return ClassificationResult.LowConfidence
            }

            if (top1Score < CONFIDENCE_THRESHOLD || scoreMargin < MARGIN_THRESHOLD) {
                return ClassificationResult.LowConfidence
            }

            val normalizedLabel = top1Label.trim().lowercase()

            if (normalizedLabel.contains("healthy")) {
                ClassificationResult.Healthy(top1Label, top1Score)
            } else {
                ClassificationResult.Diseased(top1Label, top1Score)
            }

        } catch (e: Exception) {
            ClassificationResult.Error("Analysis failed: ${e.message}")
        }
    }

    private fun isLikelyLeaf(bitmap: Bitmap): Boolean {
        return try {
            val safeBitmap = ensureSoftwareBitmap(bitmap)
            val resized = Bitmap.createScaledBitmap(safeBitmap, 64, 64, true)

            val pixels = IntArray(64 * 64)
            resized.getPixels(pixels, 0, 64, 0, 0, 64, 64)

            var greenCount = 0
            var brownYellowCount = 0
            val total = pixels.size

            for (pixel in pixels) {
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                // Green leaf pixels — healthy leaf color
                val isGreen = g > r + 15 && g > b + 15 && g > 55

                // Brown leaf pixels — diseased/dry leaf color
                // Brown: red > green > blue, moderate brightness
                val isBrown = r > 80 && r > g && g > b &&
                        r - b > 30 && r < 200 && g < 160

                // Yellow leaf pixels — yellowing diseased leaf
                // Yellow: red and green both high, blue low
                val isYellow = r > 120 && g > 100 && b < 100 &&
                        abs(r - g) < 80 && r > b + 40 && g > b + 30

                // Light green / pale green diseased leaf
                val isPaleGreen = g > r && g > b && g > 80 &&
                        g - r < 30 && g - b < 30

                if (isGreen) greenCount++
                if (isBrown || isYellow || isPaleGreen) brownYellowCount++
            }

            val greenRatio = greenCount.toFloat() / total
            val brownYellowRatio = brownYellowCount.toFloat() / total
            val combinedRatio = greenRatio + brownYellowRatio

            // Accept if enough green OR enough brown/yellow OR strong combined signal
            greenRatio >= GREEN_RATIO_THRESHOLD ||
                    brownYellowRatio >= BROWN_YELLOW_RATIO_THRESHOLD ||
                    combinedRatio >= COMBINED_LEAF_THRESHOLD

        } catch (e: Exception) {
            // If leaf check itself fails, allow the model to run
            true
        }
    }

    private fun ensureSoftwareBitmap(bitmap: Bitmap): Bitmap {
        return if (bitmap.config == Bitmap.Config.HARDWARE ||
            bitmap.config != Bitmap.Config.ARGB_8888
        ) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * IMG_SIZE * IMG_SIZE * PIXEL_SIZE)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(IMG_SIZE * IMG_SIZE)
        bitmap.getPixels(pixels, 0, IMG_SIZE, 0, 0, IMG_SIZE, IMG_SIZE)

        for (pixel in pixels) {
            val r = ((pixel shr 16) and 0xFF) / 255.0f
            val g = ((pixel shr 8) and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f

            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }

        byteBuffer.rewind()
        return byteBuffer
    }

    fun close() {
        interpreter?.close()
        interpreter = null
        outputClassesCount = 0
    }
}

sealed class ClassificationResult {
    data class Healthy(val label: String, val confidence: Float) : ClassificationResult()
    data class Diseased(val label: String, val confidence: Float) : ClassificationResult()
    object LowConfidence : ClassificationResult()
    data class Error(val message: String) : ClassificationResult()
}