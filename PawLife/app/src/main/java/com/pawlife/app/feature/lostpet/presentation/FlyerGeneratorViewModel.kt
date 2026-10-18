package com.pawlife.app.feature.lostpet.presentation

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import com.pawlife.app.core.domain.repository.PetRepository
import com.pawlife.app.core.data.db.entity.PetEntity
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FlyerGeneratorViewModel @Inject constructor(
    private val petRepository: PetRepository
) : ViewModel() {

    val allPets: Flow<List<PetEntity>> = petRepository.getAllPets()

    fun getPet(petId: Long): Flow<PetEntity?> = petRepository.getPetById(petId)

    fun generateFlyerBitmap(view: View): Bitmap {
        // Assume the view is a ComposeView of size 1080x1440
        val bitmap = Bitmap.createBitmap(1080, 1440, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun shareFlyerBitmap(context: Context, bitmap: Bitmap) {
        val file = File(context.cacheDir, "lost_pet_flyer.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "URGENT: Missing pet! Please help find them. See the attached flyer for details.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Lost Pet Flyer"))
    }
}
