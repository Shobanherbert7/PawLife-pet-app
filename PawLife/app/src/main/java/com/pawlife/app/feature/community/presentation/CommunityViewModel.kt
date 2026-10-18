package com.pawlife.app.feature.community.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pawlife.app.core.data.db.entity.CommentEntity
import com.pawlife.app.core.data.db.entity.CommunityPostEntity
import com.pawlife.app.core.domain.repository.CommunityRepository
import com.pawlife.app.feature.community.domain.ActiveParkDog
import com.pawlife.app.feature.community.domain.ParkLocation
import com.pawlife.app.feature.community.domain.PlaydateCandidate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val repository: CommunityRepository
) : ViewModel() {

    // 1. Social Posts Feed
    val posts: StateFlow<List<CommunityPostEntity>> = repository.getAllPosts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // 2. Sub-Tab Selection (0 = Parks, 1 = Playdates, 2 = Social Feed)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // 3. Playdate Energy Filter
    private val _energyFilter = MutableStateFlow("All")
    val energyFilter: StateFlow<String> = _energyFilter.asStateFlow()

    // 4. Toast / UI Event Stream
    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow = _eventFlow.asSharedFlow()

    // 5. Initial Dog Parks in Chennai
    private val initialParks = listOf(
        ParkLocation(
            id = "park_semmozhi",
            name = "Semmozhi Poonga Dog Trail",
            address = "Cathedral Road, Teynampet, Chennai",
            distanceKm = 2.8,
            totalCheckedIn = 3,
            activeDogs = listOf(
                ActiveParkDog("Rocky", "French Bulldog", "🐶", "15m ago", "Rahul K."),
                ActiveParkDog("Bella", "Beagle", "🐕", "35m ago", "Sneha R."),
                ActiveParkDog("Oscar", "Standard Poodle", "🐩", "50m ago", "Vikram D.")
            ),
            bestTimeToVisit = "Mornings 06:30 - 08:30 AM",
            isOffLeash = true,
            isUserCheckedIn = false
        ),
        ParkLocation(
            id = "park_besant_beach",
            name = "Besant Nagar Beach Coastal Dog Run",
            address = "6th Avenue, Elliot's Beach, Chennai",
            distanceKm = 7.5,
            totalCheckedIn = 4,
            activeDogs = listOf(
                ActiveParkDog("Bruno", "German Shepherd", "🦮", "10m ago", "Karthik"),
                ActiveParkDog("Milo", "Labrador", "🐕", "25m ago", "Arun M."),
                ActiveParkDog("Coco", "Golden Retriever", "🐕", "40m ago", "Divya T."),
                ActiveParkDog("Leo", "Siberian Husky", "🐺", "1h ago", "Gautam")
            ),
            bestTimeToVisit = "Sunrises & Cool Evenings (5-7 PM)",
            isOffLeash = true,
            isUserCheckedIn = false
        ),
        ParkLocation(
            id = "park_tower",
            name = "Anna Nagar Tower Park Agility Trail",
            address = "3rd Main Road, Anna Nagar, Chennai",
            distanceKm = 1.4,
            totalCheckedIn = 2,
            activeDogs = listOf(
                ActiveParkDog("Simba", "Golden Retriever", "🦮", "20m ago", "Anand S."),
                ActiveParkDog("Daisy", "Pug", "🐶", "45m ago", "Pooja V.")
            ),
            bestTimeToVisit = "07:00 - 09:00 AM",
            isOffLeash = false,
            isUserCheckedIn = false
        )
    )

    private val _parks = MutableStateFlow<List<ParkLocation>>(initialParks)
    val parks: StateFlow<List<ParkLocation>> = _parks.asStateFlow()

    // 6. Playdate Candidates Directory
    private val initialPlaydates = listOf(
        PlaydateCandidate(
            id = "play_1",
            petName = "Rocky",
            breed = "French Bulldog",
            size = "Medium",
            energyLevel = "Moderate",
            playStyle = "Ball Fetcher & Wrestler",
            compatibilityPercent = 96,
            distanceKm = 1.2,
            ownerName = "Rahul K. (Anna Nagar)",
            avatarEmoji = "🐶",
            isVerifiedVaccinated = true
        ),
        PlaydateCandidate(
            id = "play_2",
            petName = "Bruno",
            breed = "German Shepherd",
            size = "Large",
            energyLevel = "High",
            playStyle = "Rough & Tumble Runner",
            compatibilityPercent = 88,
            distanceKm = 2.4,
            ownerName = "Karthik (Kilpauk)",
            avatarEmoji = "🦮",
            isVerifiedVaccinated = true
        ),
        PlaydateCandidate(
            id = "play_3",
            petName = "Bella",
            breed = "Beagle",
            size = "Medium",
            energyLevel = "High",
            playStyle = "Gentle Chaser & Sniffer",
            compatibilityPercent = 94,
            distanceKm = 3.1,
            ownerName = "Sneha R. (T Nagar)",
            avatarEmoji = "🐕",
            isVerifiedVaccinated = true
        ),
        PlaydateCandidate(
            id = "play_4",
            petName = "Simba",
            breed = "Shih Tzu",
            size = "Small",
            energyLevel = "Calm",
            playStyle = "Sunbather & Slow Walker",
            compatibilityPercent = 82,
            distanceKm = 1.8,
            ownerName = "Pooja V. (Shenoy Nagar)",
            avatarEmoji = "🐩",
            isVerifiedVaccinated = true
        )
    )

    private val _playdates = MutableStateFlow<List<PlaydateCandidate>>(initialPlaydates)
    val playdates: StateFlow<List<PlaydateCandidate>> = _playdates.asStateFlow()

    init {
        viewModelScope.launch {
            val currentPosts = repository.getAllPosts().first()
            if (currentPosts.isEmpty()) {
                seedMockData()
            }
        }
        listenToFirestoreParkCheckIns()
    }

    private fun listenToFirestoreParkCheckIns() {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("park_checkins")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        val checkinMap = snapshot.documents.associate { doc ->
                            doc.id to ((doc.get("isCheckedIn") as? Boolean) ?: false)
                        }
                        _parks.update { list ->
                            list.map { park ->
                                val isChecked = checkinMap[park.id] ?: park.isUserCheckedIn
                                park.copy(isUserCheckedIn = isChecked)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun setEnergyFilter(filter: String) {
        _energyFilter.value = filter
    }

    fun toggleParkCheckIn(parkId: String, petName: String = "Max") {
        viewModelScope.launch {
            val currentPark = _parks.value.find { it.id === parkId } ?: return@launch
            val nextState = !currentPark.isUserCheckedIn

            _parks.update { list ->
                list.map { p ->
                    if (p.id == parkId) {
                        val updatedDogs = if (nextState) {
                            listOf(ActiveParkDog(petName, "Golden Retriever", "🐕", "Just now", "You (Shoban)")) + p.activeDogs
                        } else {
                            p.activeDogs.filter { it.ownerName != "You (Shoban)" }
                        }
                        p.copy(
                            isUserCheckedIn = nextState,
                            totalCheckedIn = if (nextState) p.totalCheckedIn + 1 else maxOf(0, p.totalCheckedIn - 1),
                            activeDogs = updatedDogs
                        )
                    } else p
                }
            }

            val msg = if (nextState) "Checked in at ${currentPark.name}! Other owners can now see Max." else "Checked out from ${currentPark.name}."
            _eventFlow.emit(msg)

            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("park_checkins").document(parkId).set(
                    mapOf(
                        "parkId" to parkId,
                        "parkName" to currentPark.name,
                        "isCheckedIn" to nextState,
                        "checkedInPet" to petName,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                // Keep local state
            }
        }
    }

    fun sendPlaydateInvite(candidate: PlaydateCandidate) {
        viewModelScope.launch {
            _eventFlow.emit("Playdate invitation sent to ${candidate.ownerName} for ${candidate.petName}! 🐾")
        }
    }

    private suspend fun seedMockData() {
        val mocks = listOf(
            CommunityPostEntity(
                id = UUID.randomUUID().toString(),
                authorId = "user_1",
                authorName = "Sarah Jenkins",
                authorAvatarUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80",
                content = "Great morning run at Semmozhi Poonga Dog Trail! Max had so much fun with Rocky the Frenchie. 🐶❤️",
                imageUrls = "https://images.unsplash.com/photo-1552053831-71594a27632d",
                petId = null,
                likes = 124,
                commentsCount = 12,
                tags = "ParkMeetup,GoldenRetriever",
                createdAt = System.currentTimeMillis() - 3600000,
                isLikedByMe = false
            ),
            CommunityPostEntity(
                id = UUID.randomUUID().toString(),
                authorId = "user_2",
                authorName = "David Smith",
                authorAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e",
                content = "Beach walk at Elliot's Besant Nagar. Gentle ocean breeze and plenty of off-leash sand to sprint on! 🌊🐕",
                imageUrls = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba",
                petId = null,
                likes = 89,
                commentsCount = 5,
                tags = "BeachRun,HappyPaws",
                createdAt = System.currentTimeMillis() - 7200000,
                isLikedByMe = false
            ),
            CommunityPostEntity(
                id = UUID.randomUUID().toString(),
                authorId = "user_3",
                authorName = "Emily Chen",
                authorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330",
                content = "First agility obstacle course completed at Anna Nagar Tower Park! Practice makes perfect.",
                imageUrls = "https://images.unsplash.com/photo-1548199973-03cce0bbc87b",
                petId = null,
                likes = 210,
                commentsCount = 34,
                tags = "AgilityTraining,DogLife",
                createdAt = System.currentTimeMillis() - 86400000,
                isLikedByMe = true
            )
        )
        try {
            repository.insertPosts(mocks)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleLike(postId: String, isLiked: Boolean) {
        viewModelScope.launch {
            val amount = if (isLiked) -1 else 1
            repository.updateLike(postId, amount, !isLiked)
        }
    }

    fun addPost(content: String, imageUri: Uri? = null) {
        viewModelScope.launch {
            var uploadedUrl: String? = null
            if (imageUri != null) {
                try {
                    val ref = FirebaseStorage.getInstance().reference.child("community_posts/${UUID.randomUUID()}")
                    ref.putFile(imageUri).await()
                    uploadedUrl = ref.downloadUrl.await().toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val newPost = CommunityPostEntity(
                id = UUID.randomUUID().toString(),
                authorId = "current_user",
                authorName = "Shoban",
                authorAvatarUrl = null,
                content = content,
                imageUrls = uploadedUrl,
                petId = null,
                likes = 0,
                commentsCount = 0,
                tags = "Community",
                createdAt = System.currentTimeMillis(),
                isLikedByMe = false
            )
            repository.insertPosts(listOf(newPost))
        }
    }

    fun getCommentsForPost(postId: String): Flow<List<CommentEntity>> {
        return repository.getCommentsForPost(postId)
    }

    fun addComment(postId: String, text: String) {
        viewModelScope.launch {
            val comment = CommentEntity(
                postId = postId,
                authorId = "current_user",
                authorName = "Shoban",
                text = text
            )
            repository.addComment(comment)
        }
    }
}
