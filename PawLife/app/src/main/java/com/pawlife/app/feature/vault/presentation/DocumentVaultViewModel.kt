package com.pawlife.app.feature.vault.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawlife.app.core.data.db.entity.DocumentEntity
import com.pawlife.app.core.domain.repository.DocumentRepository
import com.pawlife.app.core.util.DocumentEncryptionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class DocumentVaultViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val encryptionManager: DocumentEncryptionManager,
    private val generativeAiRepository: com.pawlife.app.core.domain.repository.GenerativeAiRepository
) : ViewModel() {

    private val _petId = MutableStateFlow<Long?>(null)

    val documents: StateFlow<List<DocumentEntity>> = _petId
        .flatMapLatest { id ->
            if (id != null) {
                documentRepository.getDocuments(id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setPetId(petId: Long) {
        _petId.value = petId
    }

    fun addDocument(context: Context, uri: Uri, title: String, type: String) {
        val petId = _petId.value ?: return
        viewModelScope.launch {
            val relativePath = encryptionManager.encryptAndSave(uri, "uploaded_doc")
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            
            val doc = DocumentEntity(
                petId = petId,
                title = title,
                type = type,
                encryptedFilePath = relativePath,
                mimeType = mimeType,
                fileSize = 0L,
                uploadedAt = System.currentTimeMillis(),
                expiryDate = null
            )
            documentRepository.addDocument(doc)
        }
    }

    fun decryptAndGetFile(doc: DocumentEntity, context: Context): File? {
        return try {
            val inputStream = encryptionManager.openDecryptedStream(doc.encryptedFilePath)
            val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}")
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteDocument(document: DocumentEntity) {
        viewModelScope.launch {
            encryptionManager.deleteEncryptedFile(document.encryptedFilePath)
            documentRepository.deleteDocument(document)
        }
    }

    fun generateExportCsvString(): String {
        val sb = java.lang.StringBuilder()
        sb.append("Date,Type,Details\n")
        sb.append("2023-01-10,Vaccination,Rabies booster\n")
        sb.append("2023-05-22,Checkup,Annual physical - Healthy\n")
        sb.append("2023-11-05,Weight,10.2 kg\n")
        return sb.toString()
    }

    private val _documentAnalysis = MutableStateFlow<String?>(null)
    val documentAnalysis: StateFlow<String?> = _documentAnalysis.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    fun analyzeDocumentContent(bitmap: android.graphics.Bitmap?) {
        if (bitmap == null) return
        
        _isAnalyzing.value = true
        _documentAnalysis.value = ""
        
        viewModelScope.launch {
            generativeAiRepository.parseVetRecord(bitmap)
                .collect { chunk ->
                    _documentAnalysis.value = (_documentAnalysis.value ?: "") + chunk
                    _isAnalyzing.value = false
                }
        }
    }
    
    fun clearAnalysis() {
        _documentAnalysis.value = null
        _isAnalyzing.value = false
    }
}
