package com.pawlife.app.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pawlife.app.core.data.db.entity.CommunityPostEntity
import com.pawlife.app.core.domain.repository.CommunityRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CommunityRepository {
    
    private val postsCollection = firestore.collection("community_posts")

    override fun getAllPosts(): Flow<List<CommunityPostEntity>> = callbackFlow {
        val listener = postsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val posts = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(CommunityPostEntity::class.java)?.copy(id = doc.id)
                    }
                    trySend(posts)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun insertPosts(posts: List<CommunityPostEntity>) {
        // We typically add one post at a time from the UI
        for (post in posts) {
            val docRef = if (post.id.isNotBlank()) postsCollection.document(post.id) else postsCollection.document()
            docRef.set(post).await()
        }
    }

    override suspend fun updateLike(postId: String, amount: Int, isLiked: Boolean) {
        val docRef = postsCollection.document(postId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val currentLikes = snapshot.getLong("likes") ?: 0L
            transaction.update(docRef, "likes", currentLikes + amount)
            // Note: In a real app, you'd maintain a subcollection of user IDs who liked it
            // rather than a boolean on the post itself, but this fulfills the demo.
        }.await()
    }

    override fun getCommentsForPost(postId: String): Flow<List<com.pawlife.app.core.data.db.entity.CommentEntity>> = callbackFlow {
        val listener = postsCollection.document(postId).collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(com.pawlife.app.core.data.db.entity.CommentEntity::class.java)?.copy(id = doc.id)
                    }
                    trySend(comments)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addComment(comment: com.pawlife.app.core.data.db.entity.CommentEntity) {
        val docRef = postsCollection.document(comment.postId).collection("comments").document()
        val commentToSave = comment.copy(id = docRef.id)
        
        firestore.runTransaction { transaction ->
            transaction.set(docRef, commentToSave)
            
            val postRef = postsCollection.document(comment.postId)
            val postSnapshot = transaction.get(postRef)
            val currentComments = postSnapshot.getLong("commentsCount") ?: 0L
            transaction.update(postRef, "commentsCount", currentComments + 1)
        }.await()
    }
}
