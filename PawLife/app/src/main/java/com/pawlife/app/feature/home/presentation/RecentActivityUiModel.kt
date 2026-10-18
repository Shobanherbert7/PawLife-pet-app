package com.pawlife.app.feature.home.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class RecentActivityUiModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val timestampMs: Long,
    val icon: ImageVector,
    val color: Color
)
