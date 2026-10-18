package com.pawlife.app.feature.ai.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawlife.app.core.domain.repository.GenerativeAiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WhatIfSimulatorViewModel @Inject constructor(
    private val generativeAiRepository: GenerativeAiRepository
) : ViewModel() {
    private val _chatMessages = MutableStateFlow<List<Pair<String, Boolean>>>(emptyList())
    val chatMessages: StateFlow<List<Pair<String, Boolean>>> = _chatMessages
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun submitScenario(scenario: String) {
        if (scenario.isBlank()) return
        val currentMessages = _chatMessages.value.toMutableList()
        currentMessages.add(Pair(scenario, true))
        _chatMessages.value = currentMessages
        _isLoading.value = true
        
        viewModelScope.launch {
            var fullResponse = ""
            val aiMessageIndex = currentMessages.size
            currentMessages.add(Pair("", false))
            
            generativeAiRepository.simulateWhatIfScenario(scenario)
                .catch { e ->
                    currentMessages[aiMessageIndex] = Pair("Error simulating scenario.", false)
                    _chatMessages.value = ArrayList(currentMessages)
                    _isLoading.value = false
                }
                .collect { chunk ->
                    fullResponse += chunk
                    currentMessages[aiMessageIndex] = Pair(fullResponse, false)
                    _chatMessages.value = ArrayList(currentMessages)
                }
            _isLoading.value = false
        }
    }
}
