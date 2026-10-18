package com.pawlife.app.feature.family.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FamilyTask(
    val id: String = "",
    val title: String = "",
    val time: String = "",
    val assignee: String = "Unassigned",
    val isCompleted: Boolean = false
)

data class CoParentMember(
    val name: String,
    val role: String,
    val avatar: String,
    val isOnline: Boolean = true
)

@HiltViewModel
class FamilyHubViewModel @Inject constructor() : ViewModel() {
    private val defaultTasks = listOf(
        FamilyTask("t1", "Feed Max (Morning Kibble)", "08:30 AM", "You", isCompleted = true),
        FamilyTask("t2", "Give Monthly Flea / Tick Chew", "10:00 AM", "Sarah (Co-Parent)", isCompleted = false),
        FamilyTask("t3", "Evening 2.5 km Park Walk", "06:00 PM", "Alex (Walker)", isCompleted = false),
        FamilyTask("t4", "Brush Coat & Dental Check", "08:30 PM", "Unassigned", isCompleted = false)
    )

    private val _tasks = MutableStateFlow<List<FamilyTask>>(defaultTasks)
    val tasks: StateFlow<List<FamilyTask>> = _tasks.asStateFlow()

    private val _team = MutableStateFlow(
        listOf(
            CoParentMember("You (Shoban)", "Primary Owner", "👑"),
            CoParentMember("Sarah", "Co-Parent", "👩"),
            CoParentMember("Alex", "Dog Walker", "🐕"),
            CoParentMember("Dr. Priya Sharma", "Attending Vet", "🩺")
        )
    )
    val team: StateFlow<List<CoParentMember>> = _team.asStateFlow()

    init {
        listenToFirestoreTasks()
    }

    private fun listenToFirestoreTasks() {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("family_tasks").addSnapshotListener { snapshot, _ ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val cloudTasks = snapshot.documents.map { doc ->
                        FamilyTask(
                            id = doc.id,
                            title = doc.getString("title") ?: "Pet Care Task",
                            time = doc.getString("time") ?: "Today",
                            assignee = doc.getString("assignee") ?: "Unassigned",
                            isCompleted = doc.getBoolean("isCompleted") ?: false
                        )
                    }
                    _tasks.value = cloudTasks
                }
            }
        } catch (_: Exception) {}
    }

    fun toggleTask(index: Int) {
        val current = _tasks.value.toMutableList()
        val task = current.getOrNull(index) ?: return
        val updated = task.copy(isCompleted = !task.isCompleted)
        current[index] = updated
        _tasks.value = current

        // Sync to Firestore
        try {
            val db = FirebaseFirestore.getInstance()
            val taskId = if (task.id.isNotBlank()) task.id else "task_${index}"
            val taskMap = hashMapOf(
                "title" to updated.title,
                "time" to updated.time,
                "assignee" to updated.assignee,
                "isCompleted" to updated.isCompleted,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("family_tasks").document(taskId).set(taskMap)
        } catch (_: Exception) {}
    }

    fun addTeamMember(name: String, role: String, avatar: String = "👤") {
        if (name.isBlank()) return
        val current = _team.value.toMutableList()
        current.add(CoParentMember(name.trim(), role.ifBlank { "Co-Parent" }, avatar))
        _team.value = current
    }

    fun removeTeamMember(index: Int) {
        val current = _team.value.toMutableList()
        if (index in current.indices && current.size > 1) {
            current.removeAt(index)
            _team.value = current
        }
    }
}
