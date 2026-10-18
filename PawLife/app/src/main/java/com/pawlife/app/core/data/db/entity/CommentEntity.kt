package com.pawlife.app.core.data.db.entity

data class CommentEntity(
    val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String? = null,
    val text: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
