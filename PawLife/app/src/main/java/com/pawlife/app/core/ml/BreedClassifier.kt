package com.pawlife.app.core.ml

import android.content.Context
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import com.google.mlkit.common.model.LocalModel

@androidx.camera.core.ExperimentalGetImage
class BreedClassifier(context: Context) {
    private val localModel = LocalModel.Builder()
        .setAssetFilePath("mobilenet.tflite")
        .build()
        
    private val options = CustomImageLabelerOptions.Builder(localModel)
        .setMaxResultCount(5)
        .build()

    private val labeler = ImageLabeling.getClient(options)

    fun processImageProxy(imageProxy: ImageProxy, onResult: (List<ImageLabel>) -> Unit) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            labeler.process(image)
                .addOnSuccessListener { labels ->
                    onResult(labels)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
