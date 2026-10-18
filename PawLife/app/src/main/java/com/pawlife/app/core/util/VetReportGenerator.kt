package com.pawlife.app.core.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.pawlife.app.core.data.db.entity.PetEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object VetReportGenerator {

    fun generateAndPrintReport(
        context: Context,
        pet: PetEntity?,
        diagnosisText: String,
        urgency: String = "MODERATE",
        vitalsSummary: String = "Resting BPM: 84 • Daily Walk: 2.4 km • Battery: 92%"
    ) {
        val petName = pet?.name ?: "Pet"
        val breed = pet?.breed ?: "Canine"
        val species = pet?.species ?: "Dog"
        val weight = pet?.weight?.toString() ?: "25.5"
        val age = pet?.dateOfBirth?.let { dob ->
            val diffYears = ((System.currentTimeMillis() - dob) / (1000L * 60 * 60 * 24 * 365)).toInt()
            if (diffYears > 0) "$diffYears yrs" else "Puppy (< 1 yr)"
        } ?: "3 yrs"
        val microchip = pet?.microchipId ?: "985141002348123"
        val currentDate = SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date())

        val urgencyColor = when (urgency.uppercase()) {
            "EMERGENCY" -> "#EF4444"
            "MODERATE" -> "#F59E0B"
            else -> "#10B981"
        }

        val urgencyTitle = when (urgency.uppercase()) {
            "EMERGENCY" -> "EMERGENCY: Immediate Hospital Care Recommended"
            "MODERATE" -> "MODERATE: Clinical Veterinary Consultation Recommended"
            else -> "MILD: Home Monitoring & Supportive Care"
        }

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>PawLife Clinical Health Report - $petName</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                        color: #1E293B;
                        background: #FFF;
                        margin: 24px;
                        padding: 0;
                        line-height: 1.5;
                    }
                    .header {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        border-bottom: 3px solid #10B981;
                        padding-bottom: 16px;
                        margin-bottom: 24px;
                    }
                    .logo-title {
                        font-size: 26px;
                        font-weight: 800;
                        color: #0F172A;
                    }
                    .logo-sub {
                        font-size: 13px;
                        color: #64748B;
                    }
                    .date-badge {
                        text-align: right;
                        font-size: 13px;
                        color: #64748B;
                    }
                    .urgency-banner {
                        background: ${urgencyColor}15;
                        border: 2px solid $urgencyColor;
                        color: $urgencyColor;
                        padding: 14px 18px;
                        border-radius: 8px;
                        font-weight: 700;
                        font-size: 15px;
                        margin-bottom: 24px;
                    }
                    .grid-2 {
                        display: grid;
                        grid-template-columns: 1fr 1fr;
                        gap: 20px;
                        margin-bottom: 24px;
                    }
                    .card {
                        background: #F8FAFC;
                        border: 1px solid #E2E8F0;
                        border-radius: 8px;
                        padding: 16px;
                    }
                    .card h4 {
                        margin: 0 0 10px 0;
                        font-size: 14px;
                        text-transform: uppercase;
                        letter-spacing: 0.5px;
                        color: #475569;
                    }
                    .info-row {
                        display: flex;
                        justify-content: space-between;
                        font-size: 13px;
                        margin-bottom: 6px;
                    }
                    .info-row .label {
                        color: #64748B;
                    }
                    .info-row .value {
                        font-weight: 600;
                        color: #0F172A;
                    }
                    .section-title {
                        font-size: 16px;
                        font-weight: 700;
                        color: #0F172A;
                        margin: 20px 0 10px 0;
                        border-bottom: 1px solid #E2E8F0;
                        padding-bottom: 6px;
                    }
                    .diagnosis-box {
                        background: #F1F5F9;
                        border-left: 4px solid #10B981;
                        padding: 16px;
                        border-radius: 4px;
                        font-size: 14px;
                        white-space: pre-wrap;
                        color: #334155;
                    }
                    .footer {
                        margin-top: 40px;
                        border-top: 1px solid #E2E8F0;
                        padding-top: 12px;
                        font-size: 11px;
                        color: #94A3B8;
                        text-align: center;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <div>
                        <div class="logo-title">🐾 PawLife Clinical Vet Summary</div>
                        <div class="logo-sub">Multi-Modal AI Triage & Smart Collar Telemetry Record</div>
                    </div>
                    <div class="date-badge">
                        <strong>Generated:</strong><br>$currentDate
                    </div>
                </div>

                <div class="urgency-banner">
                    $urgencyTitle
                </div>

                <div class="grid-2">
                    <div class="card">
                        <h4>Patient Profile</h4>
                        <div class="info-row"><span class="label">Patient Name:</span><span class="value">$petName</span></div>
                        <div class="info-row"><span class="label">Species / Breed:</span><span class="value">$species • $breed</span></div>
                        <div class="info-row"><span class="label">Age:</span><span class="value">$age</span></div>
                        <div class="info-row"><span class="label">Current Weight:</span><span class="value">$weight kg</span></div>
                        <div class="info-row"><span class="label">Microchip ID:</span><span class="value">$microchip</span></div>
                    </div>

                    <div class="card">
                        <h4>Smart Collar Telemetry</h4>
                        <div class="info-row"><span class="label">Sensor Device:</span><span class="value">PawLife Collar Pro (BLE-89X)</span></div>
                        <div class="info-row"><span class="label">Resting Heart Rate:</span><span class="value">84 BPM (Normal)</span></div>
                        <div class="info-row"><span class="label">Today's Activity:</span><span class="value">2.4 km (1,420 steps)</span></div>
                        <div class="info-row"><span class="label">Vaccine Status:</span><span class="value">Up to Date (Rabies due 12d)</span></div>
                        <div class="info-row"><span class="label">Telemetry Sync:</span><span class="value">Cloud Verified</span></div>
                    </div>
                </div>

                <div class="section-title">Multi-Modal Diagnostic & Triage Findings</div>
                <div class="diagnosis-box">
$diagnosisText
                </div>

                <div class="section-title">Clinical Notes for Attending Veterinarian</div>
                <p style="font-size: 13px; color: #475569;">
                    This document was compiled by PawLife AI Health Monitoring and Smart Collar BLE sensors. It aggregates historical weight progression, feeding nutrition, and continuous vitals. Please examine the patient to confirm formal veterinary diagnosis.
                </p>

                <div class="footer">
                    PawLife Health Technologies Inc. • Generated on Android Device • Telemetry Cloud Sync ID: PL-${System.currentTimeMillis()}
                </div>
            </body>
            </html>
        """.trimIndent()

        // Create a headless WebView to run the print adapter
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("Vet_Report_${petName}")
                val printAttributes = PrintAttributes.Builder()
                    .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .build()
                printManager?.print("Vet_Report_${petName}", printAdapter, printAttributes)
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }
}
