package com.pawlife.app.core.domain.repository

import com.pawlife.app.core.data.db.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun getDocuments(petId: Long): Flow<List<DocumentEntity>>
    suspend fun getDocumentById(id: Long): DocumentEntity?
    suspend fun addDocument(document: DocumentEntity): Long
    suspend fun deleteDocument(document: DocumentEntity)
}
