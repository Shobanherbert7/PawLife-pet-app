package com.pawlife.app.feature.passport.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.pawlife.app.feature.passport.domain.PetPassportInfo
import com.pawlife.app.feature.passport.domain.VaccinationRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PassportUiState(
    val passport: PetPassportInfo = PetPassportInfo(),
    val vaccinations: List<VaccinationRecord> = emptyList(),
    val showQrModal: Boolean = false,
    val showAddDialog: Boolean = false,
    val isSaving: Boolean = false
)

@HiltViewModel
class PassportViewModel @Inject constructor() : ViewModel() {

    private val defaultVaccinations = listOf(
        VaccinationRecord(
            id = "v1",
            petId = 1L,
            vaccineName = "Rabies 3-Year Inactivated Booster",
            category = "Core Canine",
            administeredDate = "15 Oct 2024",
            expiryDate = "14 Oct 2027",
            batchLotNumber = "RAB-8812-DEF",
            clinicName = "Anna Nagar Pet Hospital",
            doctorName = "Dr. Priya Sharma, BVSc",
            status = "VALID"
        ),
        VaccinationRecord(
            id = "v2",
            petId = 1L,
            vaccineName = "Canine DHPPi (5-in-1 Distemper & Parvo)",
            category = "Core Canine",
            administeredDate = "02 Sep 2024",
            expiryDate = "01 Sep 2027",
            batchLotNumber = "NOV-4401-DH",
            clinicName = "Anna Nagar Pet Hospital",
            doctorName = "Dr. Priya Sharma, BVSc",
            status = "VALID"
        ),
        VaccinationRecord(
            id = "v3",
            petId = 1L,
            vaccineName = "Bordetella Bronchiseptica (Kennel Cough)",
            category = "Lifestyle / Boarding",
            administeredDate = "20 Apr 2026",
            expiryDate = "19 Apr 2027",
            batchLotNumber = "BOR-1092-KC",
            clinicName = "Madras Veterinary College Trauma Center",
            doctorName = "Dr. K. Raman, MVSc",
            status = "VALID"
        ),
        VaccinationRecord(
            id = "v4",
            petId = 1L,
            vaccineName = "Broad-Spectrum Deworming (Praziquantel)",
            category = "Preventive Parasite",
            administeredDate = "01 Oct 2026",
            expiryDate = "01 Jan 2027",
            batchLotNumber = "DWM-3310-Z",
            clinicName = "Anna Nagar Pet Hospital",
            doctorName = "Dr. Priya Sharma, BVSc",
            status = "VALID"
        )
    )

    private val _uiState = MutableStateFlow(PassportUiState(vaccinations = defaultVaccinations))
    val uiState: StateFlow<PassportUiState> = _uiState.asStateFlow()

    init {
        listenToFirestoreVaccines()
    }

    private fun listenToFirestoreVaccines() {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("vaccinations")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        val cloudRecords = snapshot.documents.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            VaccinationRecord(
                                id = doc.id,
                                petId = (data["petId"] as? Long) ?: 1L,
                                vaccineName = data["vaccineName"] as? String ?: "Vaccine",
                                category = data["category"] as? String ?: "General",
                                administeredDate = data["administeredDate"] as? String ?: "Recent",
                                expiryDate = data["expiryDate"] as? String ?: "Valid",
                                batchLotNumber = data["batchLotNumber"] as? String ?: "LOT-001",
                                clinicName = data["clinicName"] as? String ?: "Pet Clinic",
                                doctorName = data["doctorName"] as? String ?: "Attending Vet",
                                status = data["status"] as? String ?: "VALID",
                                timestamp = (data["timestamp"] as? Long) ?: System.currentTimeMillis()
                            )
                        }

                        // Merge cloud records on top of defaults
                        val merged = (cloudRecords + defaultVaccinations)
                            .distinctBy { it.id.ifEmpty { it.vaccineName } }
                            .sortedByDescending { it.timestamp }

                        _uiState.update { it.copy(vaccinations = merged) }
                    }
                }
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    fun toggleQrModal(show: Boolean) {
        _uiState.update { it.copy(showQrModal = show) }
    }

    fun toggleAddDialog(show: Boolean) {
        _uiState.update { it.copy(showAddDialog = show) }
    }

    fun addVaccination(
        vaccineName: String,
        category: String,
        batchLot: String,
        clinicName: String,
        doctorName: String,
        administeredDate: String,
        expiryDate: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val newRecord = VaccinationRecord(
                id = "v_" + System.currentTimeMillis(),
                petId = 1L,
                vaccineName = vaccineName.trim(),
                category = category.trim(),
                administeredDate = administeredDate.trim().ifEmpty { "Today" },
                expiryDate = expiryDate.trim().ifEmpty { "1 Year Valid" },
                batchLotNumber = batchLot.trim().ifEmpty { "LOT-2026-X" },
                clinicName = clinicName.trim().ifEmpty { "Anna Nagar Pet Hospital" },
                doctorName = doctorName.trim().ifEmpty { "Dr. Priya Sharma, BVSc" },
                status = "VALID",
                timestamp = System.currentTimeMillis()
            )

            // Optimistic update
            _uiState.update {
                it.copy(
                    vaccinations = listOf(newRecord) + it.vaccinations,
                    isSaving = false,
                    showAddDialog = false
                )
            }

            // Sync to Firestore
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("vaccinations").document(newRecord.id).set(
                    mapOf(
                        "petId" to newRecord.petId,
                        "vaccineName" to newRecord.vaccineName,
                        "category" to newRecord.category,
                        "administeredDate" to newRecord.administeredDate,
                        "expiryDate" to newRecord.expiryDate,
                        "batchLotNumber" to newRecord.batchLotNumber,
                        "clinicName" to newRecord.clinicName,
                        "doctorName" to newRecord.doctorName,
                        "status" to newRecord.status,
                        "timestamp" to newRecord.timestamp
                    )
                )
            } catch (e: Exception) {
                // Keep local optimistic
            }
        }
    }
}
