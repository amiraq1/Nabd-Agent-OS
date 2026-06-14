package com.nabd.ai.agora.ui.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions

private val BG_DIALOG     = Color(0xFF0D1117)
private val ACCENT_NEON    = Color(0xFF00FF66)
private val TEXT_MUTED     = Color(0xFF5B6B85)
private val FONT_MONO      = FontFamily.Monospace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderConfigSheet(
    providerName: String,
    currentKey: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var draftKey by remember(currentKey) { mutableStateOf(currentKey) }
    var keyVisible by remember { mutableStateOf(false) }

    val keyHint = when (providerName) {
        "OpenAI"    -> "sk-..."
        "Google"    -> "AIza..."
        "Anthropic" -> "sk-ant-..."
        "DeepSeek"  -> "sk-..."
        "NVIDIA"    -> "nvapi-..."
        else        -> ""
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BG_DIALOG,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = TEXT_MUTED.copy(alpha = 0.4f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = providerName,
                    fontFamily = FONT_MONO,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Enter your API key",
                    color = TEXT_MUTED,
                    fontFamily = FONT_MONO,
                    fontSize = 12.sp
                )
            }

            OutlinedTextField(
                value = draftKey,
                onValueChange = { draftKey = it },
                label = { Text("API Key", fontFamily = FONT_MONO) },
                placeholder = { Text(keyHint, color = TEXT_MUTED.copy(alpha = 0.5f), fontFamily = FONT_MONO) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { keyVisible = !keyVisible }) {
                        Icon(
                            imageVector = if (keyVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = null,
                            tint = TEXT_MUTED
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ACCENT_NEON,
                    unfocusedBorderColor = TEXT_MUTED.copy(alpha = 0.5f),
                    focusedLabelColor = ACCENT_NEON,
                    unfocusedLabelColor = TEXT_MUTED,
                    cursorColor = ACCENT_NEON,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Cancel",
                        color = TEXT_MUTED,
                        fontFamily = FONT_MONO
                    )
                }

                Button(
                    onClick = {
                        onSave(draftKey.trim())
                        onDismiss()
                    },
                    enabled = draftKey.trim().isNotBlank(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ACCENT_NEON,
                        contentColor = Color.Black,
                        disabledContainerColor = ACCENT_NEON.copy(alpha = 0.2f),
                        disabledContentColor = Color.Black.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Save",
                        fontFamily = FONT_MONO,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
