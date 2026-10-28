package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActionType
import com.example.model.AutomationRule
import com.example.model.TriggerType
import com.example.ui.MainViewModel

@Composable
fun AutomationsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val rules by viewModel.automationEngine.rules.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var selectedTrigger by remember { mutableStateOf(TriggerType.SCHEDULED_TIME) }
    var selectedAction by remember { mutableStateOf(ActionType.SPEAK_ANNOUNCEMENT) }
    var actionParam by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = primaryColor,
                contentColor = Color.Black,
                modifier = Modifier.testTag("add_automation_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Rule")
            }
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize().testTag("automations_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "AUTONOMOUS ENGINE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = primaryColor
                    )
                    Text(
                        text = "Real-time background triggers, routines and device reactions",
                        fontSize = 12.sp,
                        color = onSurfaceColor.copy(alpha = 0.6f)
                    )
                }
            }

            items(rules) { rule ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    border = CardDefaults.outlinedCardBorder().copy(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            if (rule.isEnabled) listOf(primaryColor.copy(alpha = 0.6f), Color.Transparent)
                            else listOf(Color.Gray.copy(alpha = 0.3f), Color.Transparent)
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = rule.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = onSurfaceColor
                            )

                            Switch(
                                checked = rule.isEnabled,
                                onCheckedChange = { viewModel.automationEngine.toggleRule(rule.id) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = primaryColor
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "TRIGGER: ${rule.triggerType.label}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = primaryColor
                        )

                        Text(
                            text = "ACTION: ${rule.actionType.label}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = onSurfaceColor.copy(alpha = 0.8f)
                        )

                        if (rule.actionParam.isNotEmpty()) {
                            Text(
                                text = "\"${rule.actionParam}\"",
                                fontSize = 12.sp,
                                color = onSurfaceColor.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.automationEngine.executeRule(rule) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Test Run",
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Run", fontSize = 11.sp, color = primaryColor)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { viewModel.automationEngine.deleteRule(rule.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFFF5252).copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Add Rule Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("New Automation Rule", color = primaryColor) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Rule Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Select Trigger:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    TriggerType.values().take(4).forEach { trigger ->
                        FilterChip(
                            selected = selectedTrigger == trigger,
                            onClick = { selectedTrigger = trigger },
                            label = { Text(trigger.label, fontSize = 11.sp) },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Select Action:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    ActionType.values().forEach { action ->
                        FilterChip(
                            selected = selectedAction == action,
                            onClick = { selectedAction = action },
                            label = { Text(action.label, fontSize = 11.sp) },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    if (selectedAction == ActionType.SPEAK_ANNOUNCEMENT) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = actionParam,
                            onValueChange = { actionParam = it },
                            label = { Text("Spoken announcement message") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (selectedAction == ActionType.OPEN_APP) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = actionParam,
                            onValueChange = { actionParam = it },
                            label = { Text("App name (e.g. YouTube)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            viewModel.automationEngine.addRule(
                                AutomationRule(
                                    title = newTitle,
                                    triggerType = selectedTrigger,
                                    actionType = selectedAction,
                                    actionParam = actionParam
                                )
                            )
                            showAddDialog = false
                            newTitle = ""
                            actionParam = ""
                        }
                    }
                ) {
                    Text("Add Rule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
