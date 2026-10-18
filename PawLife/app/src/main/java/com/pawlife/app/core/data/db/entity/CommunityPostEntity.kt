package com.pawlife.app.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String? = null,
    val content: String = "",
    val imageUrls: String? = null,       // JSON array
    val petId: Long? = null,             // optional tag
    val likes: Int = 0,
    val commentsCount: Int = 0,
    val tags: String? = null,            // JSON array of tag strings
    val createdAt: Long = 0L,
    val syncedAt: Long = System.currentTimeMillis(),
    val isLikedByMe: Boolean = false
)
