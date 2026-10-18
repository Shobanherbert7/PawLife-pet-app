package com.pawlife.app.feature.family.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pawlife.app.presentation.theme.*
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyHubScreen(
    viewModel: FamilyHubViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasks.collectAsState()
    val team by viewModel.team.collectAsState()
    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val context = LocalContext.current

    var showManageDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Family Hub", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            ) 
        },
        containerColor = BackgroundDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                CalendarStrip()
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Team", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { showManageDialog = true }) {
                        Text("Manage", color = Secondary, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    team.take(5).forEachIndexed { index, member ->
                        val color = when (index % 4) {
                            0 -> Primary
                            1 -> Secondary
                            2 -> Warning
                            else -> Danger
                        }
                        FamilyAvatar(
                            name = member.name,
                            color = color,
                            avatarEmoji = member.avatar,
                            onClick = { showManageDialog = true }
                        )
                    }
                    
                    // Team Plus (+) Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(CardDark)
                            .border(1.5.dp, Primary.copy(alpha = 0.5f), CircleShape)
                            .clickable { showAddMemberDialog = true },
                        contentAlignment = Alignment.Center
                    ) { 
                        Icon(Icons.Default.Add, contentDescription = "Add Team Member", tint = Primary)
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).premiumShadow(color = Success, alpha = 0.1f, shadowBlurRadius = 32.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Daily Progress", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SlateLight)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$completedCount of $totalCount tasks complete", style = MaterialTheme.typography.bodyMedium, color = SlateSubtle)
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(64.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxSize(),
                                color = Success,
                                trackColor = CardDark,
                                strokeWidth = 6.dp
                            )
                            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Success)
                        }
                    }
                }
            }
            
            item {
                Text("Today's Tasks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            }
            
            itemsIndexed(tasks) { index, task ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        if (dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                            viewModel.toggleTask(index)
                        }
                        false // Always bounce back, we just want the action trigger
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp),
                    backgroundContent = {
                        val color = if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) Success else Color.Transparent
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(color, RoundedCornerShape(16.dp))
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Complete", tint = Color.White)
                        }
                    }
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.05f)))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleTask(index) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.toggleTask(index) }) {
                                Icon(
                                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                    contentDescription = "Toggle Complete",
                                    tint = if (task.isCompleted) Success else SlateSubtle
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (task.isCompleted) SlateSubtle else SlateLight,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(task.time, style = MaterialTheme.typography.bodySmall, color = SlateSubtle)
                            }
                            
                            if (task.assignee.isNotBlank()) {
                                Surface(
                                    color = BackgroundDark,
                                    shape = CircleShape,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(task.assignee.first().toString(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Warning)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Manage Team Modal Dialog ───────────────────────────────────────────────
    if (showManageDialog) {
        AlertDialog(
            onDismissRequest = { showManageDialog = false },
            containerColor = CardDark,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = Secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manage Pet Care Team", color = SlateLight, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    IconButton(onClick = { showManageDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SlateSubtle)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Co-parents and caregivers with shared pet access:",
                        color = SlateSubtle,
                        fontSize = 13.sp
                    )
                    
                    team.forEachIndexed { index, member ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BackgroundDark.copy(alpha = 0.6f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = CardDark,
                                        shape = CircleShape,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(member.avatar, fontSize = 18.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(member.name, color = SlateLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(member.role, color = Primary, fontSize = 11.sp)
                                    }
                                }

                                if (index > 0) {
                                    IconButton(
                                        onClick = {
                                            viewModel.removeTeamMember(index)
                                            Toast.makeText(context, "${member.name} removed from team", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Danger.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                                    }
                                } else {
                                    Surface(
                                        color = Primary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Owner", color = Primary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            showManageDialog = false
                            showAddMemberDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Secondary.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Secondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add New Co-Parent / Caregiver", color = Secondary, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showManageDialog = false }) {
                    Text("Done", color = Primary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // ── Add Team Member Dialog ────────────────────────────────────────────────
    if (showAddMemberDialog) {
        var memberName by remember { mutableStateOf("") }
        var selectedRole by remember { mutableStateOf("Co-Parent") }
        var selectedAvatar by remember { mutableStateOf("👩") }

        val roles = listOf("Co-Parent", "Dog Walker", "Pet Sitter", "Family Member", "Vet Tech")
        val avatarOptions = listOf("👩", "👨", "👧", "🐕", "🐾", "🩺", "🌟")

        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            containerColor = CardDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Team Member", color = SlateLight, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Grant shared access to pet schedules and care tasks:", color = SlateSubtle, fontSize = 12.sp)

                    OutlinedTextField(
                        value = memberName,
                        onValueChange = { memberName = it },
                        label = { Text("Caregiver Name", color = SlateSubtle) },
                        placeholder = { Text("e.g. Sarah, Alex, Aunt Jane", color = SlateSubtle.copy(alpha = 0.5f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = SlateLight,
                            unfocusedTextColor = SlateLight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Role Selector
                    Text("Assign Role:", color = SlateLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(roles) { role ->
                            val isSelected = selectedRole == role
                            Surface(
                                modifier = Modifier.clickable { selectedRole = role },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Primary.copy(alpha = 0.2f) else BackgroundDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Primary else Color.White.copy(alpha = 0.08f))
                            ) {
                                Text(
                                    text = role,
                                    color = if (isSelected) Primary else SlateSubtle,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Avatar Selector
                    Text("Choose Avatar:", color = SlateLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        avatarOptions.forEach { emoji ->
                            val isSelected = selectedAvatar == emoji
                            Surface(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clickable { selectedAvatar = emoji },
                                shape = CircleShape,
                                color = if (isSelected) Primary.copy(alpha = 0.25f) else BackgroundDark,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isSelected) Primary else Color.Transparent)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(emoji, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (memberName.isNotBlank()) {
                            viewModel.addTeamMember(memberName, selectedRole, selectedAvatar)
                            Toast.makeText(context, "$memberName added as $selectedRole!", Toast.LENGTH_SHORT).show()
                            showAddMemberDialog = false
                        } else {
                            Toast.makeText(context, "Please enter a caregiver name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Add Member", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = false }) {
                    Text("Cancel", color = SlateSubtle)
                }
            }
        )
    }
}

@Composable
fun FamilyAvatar(
    name: String,
    color: Color,
    avatarEmoji: String = "",
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .premiumShadow(color, 0.2f, 8.dp, 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (avatarEmoji.isNotBlank()) {
            Text(avatarEmoji, fontSize = 20.sp)
        } else {
            Text(name.first().toString(), color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CalendarStrip() {
    val calendar = Calendar.getInstance()
    val days = (0..6).map { 
        val cal = calendar.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, it)
        cal
    }

    androidx.compose.foundation.lazy.LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(days.size) { index ->
            val date = days[index]
            val isToday = index == 0
            val bgColor = if (isToday) Primary else CardDark
            val textColor = if (isToday) Color.White else SlateSubtle
            
            val dayOfWeekFormat = java.text.SimpleDateFormat("EEE", Locale.getDefault())
            val dayOfWeekStr = dayOfWeekFormat.format(date.time)
            val dayOfMonth = date.get(Calendar.DAY_OF_MONTH).toString()
            
            Column(
                modifier = Modifier
                    .width(60.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(bgColor)
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(dayOfWeekStr, style = MaterialTheme.typography.labelMedium, color = textColor)
                Spacer(modifier = Modifier.height(8.dp))
                Text(dayOfMonth, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
            }
        }
    }
}
