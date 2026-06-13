package com.nabd.ai.local.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nabd.ai.local.domain.model.LocalModel
import com.nabd.ai.local.domain.model.ModelImportState
import com.nabd.ai.local.rag.db.KnowledgeDocumentEntity
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onImport: (Uri) -> Unit,
    onSelect: (LocalModel) -> Unit,
    onDelete: (LocalModel) -> Unit,
    onImportDocument: (Uri, String) -> Unit,
    onDeleteDocument: (String) -> Unit,
    onProviderSelect: (com.nabd.ai.local.engine.ProviderType) -> Unit,
    onOpenAiKeyChange: (String) -> Unit,
    onGeminiKeyChange: (String) -> Unit,
    onAnthropicKeyChange: (String) -> Unit
) {
    val modelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { onImport(it) }
    }

    val docLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { onImportDocument(it, "New Document") }
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title = { 
                Text(
                    "Settings & Knowledge", 
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                ) 
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            divider = { Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)) }
        ) {
            val tabs = listOf("Providers", "Models", "Knowledge")
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { 
                        Text(
                            title, 
                            color = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        ) 
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> ProvidersTab(uiState, onProviderSelect, onOpenAiKeyChange, onGeminiKeyChange, onAnthropicKeyChange)
            1 -> ModelsTab(uiState, modelLauncher, onSelect, onDelete)
            2 -> KnowledgeTab(uiState, docLauncher, onDeleteDocument)
        }
    }
}

@Composable
fun ProvidersTab(
    uiState: SettingsUiState,
    onProviderSelect: (com.nabd.ai.local.engine.ProviderType) -> Unit,
    onOpenAiKeyChange: (String) -> Unit,
    onGeminiKeyChange: (String) -> Unit,
    onAnthropicKeyChange: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                "Select LLM Provider", 
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        item {
            com.nabd.ai.local.engine.ProviderType.values().forEach { provider ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { onProviderSelect(provider) }
                ) {
                    RadioButton(
                        selected = uiState.activeProvider == provider.name,
                        onClick = { onProviderSelect(provider) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(provider.name, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
        item {
            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            Spacer(Modifier.height(16.dp))
            Text(
                "API Keys", 
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        item {
            var localOpenAiKey by remember { mutableStateOf(uiState.openAiApiKey ?: "") }
            com.nabd.ai.local.ui.components.ProviderKeyInput(
                providerName = "OpenAI",
                currentKey = localOpenAiKey,
                onKeyChanged = { localOpenAiKey = it },
                onSave = { onOpenAiKeyChange(localOpenAiKey) }
            )
        }
        item {
            var localGeminiKey by remember { mutableStateOf(uiState.geminiApiKey ?: "") }
            com.nabd.ai.local.ui.components.ProviderKeyInput(
                providerName = "Gemini",
                currentKey = localGeminiKey,
                onKeyChanged = { localGeminiKey = it },
                onSave = { onGeminiKeyChange(localGeminiKey) }
            )
        }
        item {
            var localAnthropicKey by remember { mutableStateOf(uiState.anthropicApiKey ?: "") }
            com.nabd.ai.local.ui.components.ProviderKeyInput(
                providerName = "Anthropic",
                currentKey = localAnthropicKey,
                onKeyChanged = { localAnthropicKey = it },
                onSave = { onAnthropicKeyChange(localAnthropicKey) }
            )
        }
    }
}

@Composable
fun ModelsTab(
    uiState: SettingsUiState,
    launcher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    onSelect: (LocalModel) -> Unit,
    onDelete: (LocalModel) -> Unit
) {
    Column {
        if (uiState.importState !is ModelImportState.Idle) {
            ImportProgressCard(uiState.importState)
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Available Models", 
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            )
            Button(
                onClick = { launcher.launch(arrayOf("*/*")) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.width(4.dp))
                Text("Import", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.models) { model ->
                ModelItem(
                    model = model,
                    isActive = model.fileName == uiState.activeModelName,
                    onSelect = { onSelect(model) },
                    onDelete = { onDelete(model) }
                )
            }
        }
    }
}

@Composable
fun KnowledgeTab(
    uiState: SettingsUiState,
    launcher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    onDelete: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Knowledge Base", 
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            )
            Button(
                onClick = { launcher.launch(arrayOf("*/*")) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.width(4.dp))
                Text("Add Doc", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.documents) { doc ->
                KnowledgeItem(doc = doc, onDelete = { onDelete(doc.id) })
            }
        }
    }
}

@Composable
fun KnowledgeItem(doc: KnowledgeDocumentEntity, onDelete: () -> Unit) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val sizeFormatter = DecimalFormat("#.##")
    val sizeInMb = doc.size.toDouble() / (1024 * 1024)

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = doc.title, fontWeight = FontWeight.Bold)
                Text(
                    text = "${doc.status} • ${sizeFormatter.format(sizeInMb)} MB • ${dateFormatter.format(Date(doc.createdAt))}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun ImportProgressCard(state: ModelImportState) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = when (state) {
                    is ModelImportState.Validating -> "Validating GGUF..."
                    is ModelImportState.Copying -> "Importing Model..."
                    is ModelImportState.Completed -> "Import Successful"
                    is ModelImportState.Failed -> "Import Failed"
                    else -> ""
                },
                style = MaterialTheme.typography.titleSmall
            )
            if (state is ModelImportState.Copying) {
                LinearProgressIndicator(progress = state.progress, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun ModelItem(model: LocalModel, isActive: Boolean, onSelect: () -> Unit, onDelete: () -> Unit) {
    val sizeInGb = model.sizeBytes.toDouble() / (1024 * 1024 * 1024)
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() },
        border = if (isActive) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)) else null
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = model.fileName, fontWeight = FontWeight.Bold)
                Text(text = "%.2f GB".format(sizeInGb), style = MaterialTheme.typography.bodySmall)
            }
            if (isActive) Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
        }
    }
}
