package com.pawlife.app.feature.vault.presentation

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.pawlife.app.presentation.theme.BackgroundDark
import com.pawlife.app.presentation.theme.CardDark
import com.pawlife.app.presentation.theme.Primary
import com.pawlife.app.presentation.theme.SlateLight
import com.pawlife.app.presentation.theme.SlateSubtle
import com.pawlife.app.presentation.theme.Warning
import com.pawlife.app.presentation.theme.Success
import com.pawlife.app.presentation.theme.premiumShadow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.OutputStreamWriter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentVaultScreen(
    petId: Long,
    viewModel: DocumentVaultViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(petId) {
        viewModel.setPetId(petId)
    }

    val documents by viewModel.documents.collectAsStateWithLifecycle(initialValue = emptyList())

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Hardcoding title for simplicity in Sprint 2, would typically show a dialog here
            viewModel.addDocument(context, it, "New Document", "other")
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(viewModel.generateExportCsvString())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Secure Vault",
                    style = MaterialTheme.typography.displaySmall,
                    color = SlateLight,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { createDocumentLauncher.launch("PetHealthRecords.csv") }) {
                    Icon(Icons.Default.FileDownload, contentDescription = "Export CSV", tint = Primary)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { filePickerLauncher.launch("*/*") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Document")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (documents.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(top = 100.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = SlateSubtle, modifier = Modifier.padding(bottom = 16.dp).height(64.dp))
                        Text("No documents secured yet", style = MaterialTheme.typography.titleLarge, color = SlateLight)
                        Text("Upload records to keep them safe.", style = MaterialTheme.typography.bodyMedium, color = SlateSubtle)
                    }
                }
            }
            items(documents) { doc ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .premiumShadow(Primary, 0.05f, 20.dp, 10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            val file = viewModel.decryptAndGetFile(doc, context)
                            file?.let {
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    it
                                )
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, doc.mimeType)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = CardDark.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(doc.title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Added ${SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(doc.uploadedAt))}",
                                style = MaterialTheme.typography.labelMedium,
                                color = SlateSubtle
                            )
                        }
                        IconButton(
                            onClick = { 
                                val file = viewModel.decryptAndGetFile(doc, context)
                                if (file != null) {
                                    try {
                                        val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                                        viewModel.analyzeDocumentContent(bitmap)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            },
                            modifier = Modifier.background(CardDark, RoundedCornerShape(12.dp)).padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "AI Parse", tint = Primary)
                        }
                        IconButton(
                            onClick = { viewModel.deleteDocument(doc) },
                            modifier = Modifier.background(CardDark, RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Warning)
                        }
                    }
                }
            }
        }
    }
    
    val documentAnalysis by viewModel.documentAnalysis.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()

    if (documentAnalysis != null || isAnalyzing) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewModel.clearAnalysis() },
            title = { Text("AI Document Analysis", style = MaterialTheme.typography.titleLarge) },
            text = {
                if (isAnalyzing && documentAnalysis.isNullOrBlank()) {
                    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.CircularProgressIndicator(color = Primary)
                    }
                } else {
                    LazyColumn {
                        item {
                            Text(documentAnalysis ?: "", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { viewModel.clearAnalysis() }) {
                    Text("Close", color = Primary)
                }
            },
            containerColor = BackgroundDark,
            titleContentColor = Color.White,
            textContentColor = SlateLight
        )
    }
}
