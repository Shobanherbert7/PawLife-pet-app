package com.pawlife.app.feature.passport.domain

data class VaccinationRecord(
    val id: String = "",
    val petId: Long = 1L,
    val vaccineName: String = "",
    val category: String = "Core Canine",
    val administeredDate: String = "",
    val expiryDate: String = "",
    val batchLotNumber: String = "",
    val clinicName: String = "",
    val doctorName: String = "",
    val status: String = "VALID", // VALID, EXPIRING_SOON, OVERDUE
    val timestamp: Long = System.currentTimeMillis()
)

data class PetPassportInfo(
    val passportId: String = "PAW-IN-2026-98514",
    val petName: String = "Max",
    val species: String = "Canine",
    val breed: String = "Golden Retriever",
    val gender: String = "Male (Intact)",
    val birthDate: String = "14 Oct 2023",
    val microchipId: String = "985141002348123",
    val microchipStandard: String = "ISO 11784/11785 FDX-B (134.2 kHz)",
    val officialRegistry: String = "Kennel Club of India (KCI) & PawLife Global",
    val guardianName: String = "Shoban Herbert",
    val guardianContact: String = "+91 98401 23456",
    val guardianCity: String = "Chennai, Tamil Nadu, India",
    val rabiesCertificateNo: String = "RAB-TN-2025-4491",
    val immunityStatus: String = "FULLY VACCINATED",
    val publicVerificationUrl: String = "http://localhost:5173/?passport=1"
)
