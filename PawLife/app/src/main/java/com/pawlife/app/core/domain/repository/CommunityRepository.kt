package com.pawlife.app.core.domain.repository

import com.pawlife.app.core.data.db.entity.CommunityPostEntity
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    fun getAllPosts(): Flow<List<CommunityPostEntity>>
    suspend fun insertPosts(posts: List<CommunityPostEntity>)
    suspend fun updateLike(postId: String, amount: Int, isLiked: Boolean)
    fun getCommentsForPost(postId: String): Flow<List<com.pawlife.app.core.data.db.entity.CommentEntity>>
    suspend fun addComment(comment: com.pawlife.app.core.data.db.entity.CommentEntity)
}
