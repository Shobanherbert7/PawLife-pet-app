package com.pawlife.app.core.data.repository

import com.pawlife.app.core.data.db.dao.DocumentDao
import com.pawlife.app.core.data.db.entity.DocumentEntity
import com.pawlife.app.core.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
    private val documentDao: DocumentDao
) : DocumentRepository {
    override fun getDocuments(petId: Long): Flow<List<DocumentEntity>> {
        return documentDao.getDocuments(petId)
    }

    override suspend fun getDocumentById(id: Long): DocumentEntity? {
        return documentDao.getDocumentById(id)
    }

    override suspend fun addDocument(document: DocumentEntity): Long {
        return documentDao.insertDocument(document)
    }

    override suspend fun deleteDocument(document: DocumentEntity) {
        documentDao.deleteDocument(document)
    }
}
