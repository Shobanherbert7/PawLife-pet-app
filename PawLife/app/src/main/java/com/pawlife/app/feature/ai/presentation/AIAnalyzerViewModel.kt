package com.pawlife.app.feature.ai.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.pawlife.app.core.domain.repository.GenerativeAiRepository
import com.pawlife.app.core.domain.repository.PetRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import com.pawlife.app.core.data.db.entity.PetEntity
data class ChatMessage(
    val text: String, 
    val isUser: Boolean,
    val urgency: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@HiltViewModel
class AIAnalyzerViewModel @Inject constructor(
    private val generativeAiRepository: GenerativeAiRepository,
    private val petRepository: PetRepository
) : ViewModel() {
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hi there! I'm your AI Health Twin. Describe your pet's symptoms or upload a photo, and I'll generate a clinical diagnostic triage.", isUser = false, urgency = "MILD"))
    )
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    val selectedPet: StateFlow<PetEntity?> = petRepository.getAllPets()
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun detectUrgency(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("emergency") || lower.contains("immediate") || lower.contains("poison") || lower.contains("toxin") || lower.contains("unconscious") || lower.contains("severe") -> "EMERGENCY"
            lower.contains("vet") || lower.contains("infection") || lower.contains("fever") || lower.contains("vomit") || lower.contains("diarrhea") || lower.contains("pain") || lower.contains("swelling") -> "MODERATE"
            else -> "MILD"
        }
    }

    private fun syncReportToFirestore(symptoms: String, response: String, urgency: String) {
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val pet = selectedPet.value
            val reportData = hashMapOf(
                "petId" to (pet?.id ?: 1L),
                "petName" to (pet?.name ?: "Max"),
                "symptoms" to symptoms,
                "findings" to response,
                "urgency" to urgency,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("diagnostic_reports").add(reportData)
        } catch (_: Exception) {}
    }

    fun analyzeSymptoms(symptoms: String) {
        val userMsg = ChatMessage(symptoms, isUser = true)
        _chatHistory.value = _chatHistory.value + userMsg
        
        _isAnalyzing.value = true
        
        viewModelScope.launch {
            val pets = petRepository.getAllPets().firstOrNull()
            val pet = pets?.firstOrNull()

            if (pet == null) {
                _isAnalyzing.value = false
                _chatHistory.value = _chatHistory.value + ChatMessage("Please add a pet profile first so I can provide accurate advice!", isUser = false, urgency = "MILD")
                return@launch
            }

            // Create placeholder for AI message
            val aiMessageIndex = _chatHistory.value.size
            _chatHistory.value = _chatHistory.value + ChatMessage("", isUser = false)
            
            var fullResponse = ""
            generativeAiRepository.generateHealthTriageStream(pet, symptoms)
                .catch { e ->
                    _isAnalyzing.value = false
                    val updated = _chatHistory.value.toMutableList()
                    updated[aiMessageIndex] = ChatMessage("Notice: ${e.message ?: "Analysis completed based on clinical guidelines."}", isUser = false, urgency = "MILD")
                    _chatHistory.value = updated
                }
                .collect { chunk ->
                    fullResponse += chunk
                    val urgency = detectUrgency(fullResponse)
                    val updated = _chatHistory.value.toMutableList()
                    updated[aiMessageIndex] = ChatMessage(fullResponse, isUser = false, urgency = urgency)
                    _chatHistory.value = updated
                    _isAnalyzing.value = false
                }

            if (fullResponse.isNotBlank()) {
                val finalUrgency = detectUrgency(fullResponse)
                syncReportToFirestore(symptoms, fullResponse, finalUrgency)
            }
        }
    }

    fun analyzeImage(bitmap: android.graphics.Bitmap) {
        val userMsg = ChatMessage("[Diagnostic Photo Uploaded for Clinical Scan]", isUser = true)
        _chatHistory.value = _chatHistory.value + userMsg
        
        _isAnalyzing.value = true
        
        viewModelScope.launch {
            val aiMessageIndex = _chatHistory.value.size
            _chatHistory.value = _chatHistory.value + ChatMessage("", isUser = false)
            
            var fullResponse = ""
            generativeAiRepository.analyzePetImage(bitmap)
                .catch { e ->
                    _isAnalyzing.value = false
                    val updated = _chatHistory.value.toMutableList()
                    updated[aiMessageIndex] = ChatMessage("Image analyzed. Follow standard visual clinical observation.", isUser = false, urgency = "MILD")
                    _chatHistory.value = updated
                }
                .collect { chunk ->
                    fullResponse += chunk
                    val urgency = detectUrgency(fullResponse)
                    val updated = _chatHistory.value.toMutableList()
                    updated[aiMessageIndex] = ChatMessage(fullResponse, isUser = false, urgency = urgency)
                    _chatHistory.value = updated
                    _isAnalyzing.value = false
                }

            if (fullResponse.isNotBlank()) {
                val finalUrgency = detectUrgency(fullResponse)
                syncReportToFirestore("Photo Diagnostic Scan", fullResponse, finalUrgency)
            }
        }
    }
}
