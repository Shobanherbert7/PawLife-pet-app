package com.pawlife.app.feature.pets.presentation

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.pawlife.app.core.data.db.entity.PetEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FlyerGenerator {

    fun generateLostPetFlyer(context: Context, pet: PetEntity): File {
        val pdfDocument = PdfDocument()
        
        // standard US Letter size: 8.5 x 11 inches -> 612 x 792 points
        val pageInfo = PdfDocument.PageInfo.Builder(612, 792, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Draw white background
        val bgPaint = Paint().apply { color = Color.WHITE }
        canvas.drawRect(0f, 0f, pageInfo.pageWidth.toFloat(), pageInfo.pageHeight.toFloat(), bgPaint)

        // Draw LOST PET Red Banner
        val bannerPaint = Paint().apply { color = Color.parseColor("#D32F2F") }
        canvas.drawRect(0f, 50f, pageInfo.pageWidth.toFloat(), 150f, bannerPaint)

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 72f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("LOST PET", pageInfo.pageWidth / 2f, 125f, titlePaint)

        // Draw Pet Name
        val namePaint = Paint().apply {
            color = Color.BLACK
            textSize = 54f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Have you seen ${pet.name}?", pageInfo.pageWidth / 2f, 220f, namePaint)

        // Draw Details
        val detailPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        var currentY = 320f
        val startX = 60f
        val lineSpacing = 40f

        val ageYears = ((System.currentTimeMillis() - pet.dateOfBirth) / 31556952000L).coerceAtLeast(1)

        canvas.drawText("Breed: ${pet.breed}", startX, currentY, detailPaint); currentY += lineSpacing
        canvas.drawText("Age: $ageYears Years Old", startX, currentY, detailPaint); currentY += lineSpacing
        canvas.drawText("Weight: ${pet.weight} kg", startX, currentY, detailPaint); currentY += lineSpacing
        
        currentY += 40f
        canvas.drawText("Please help us find our beloved pet!", startX, currentY, detailPaint)
        currentY += lineSpacing
        canvas.drawText("If found, please contact us immediately.", startX, currentY, detailPaint)

        // Contact Info Box
        val boxPaint = Paint().apply {
            color = Color.parseColor("#FFF3E0")
            style = Paint.Style.FILL
        }
        val boxStrokePaint = Paint().apply {
            color = Color.parseColor("#FFB74D")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        
        val boxTop = currentY + 60f
        val boxBottom = boxTop + 120f
        canvas.drawRect(startX, boxTop, pageInfo.pageWidth - startX, boxBottom, boxPaint)
        canvas.drawRect(startX, boxTop, pageInfo.pageWidth - startX, boxBottom, boxStrokePaint)

        val contactTitlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val contactDetailPaint = Paint().apply {
            color = Color.parseColor("#D32F2F")
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        
        canvas.drawText("CONTACT:", pageInfo.pageWidth / 2f, boxTop + 45f, contactTitlePaint)
        canvas.drawText("555-0199 / owner@email.com", pageInfo.pageWidth / 2f, boxTop + 95f, contactDetailPaint)

        pdfDocument.finishPage(page)

        // Save to cache directory
        val fileName = "Lost_Pet_Flyer_${pet.name}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.pdf"
        
        // Ensure "pdfs" directory exists in cache (matching FileProvider paths)
        val pdfsDir = File(context.cacheDir, "pdfs")
        if (!pdfsDir.exists()) {
            pdfsDir.mkdirs()
        }
        
        val file = File(pdfsDir, fileName)
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return file
    }
}
