package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StepRecord
import com.example.ui.HistoryFilter
import com.example.ui.StepViewModel
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: StepViewModel,
    modifier: Modifier = Modifier
) {
    val historyList by viewModel.filteredHistoryList.collectAsState()
    val activeFilter by viewModel.historyFilter.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BlushBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            // Header Screen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Riwayat Aktivitas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDarkPrimary,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "Catatan pemantauan langkah kaki",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDarkMuted,
                        fontSize = 11.sp
                    )
                }
                if (historyList.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.testTag("btn_clear_history")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Hapus Riwayat",
                            tint = TextDarkMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips Bar (Semua, Hari Ini, 7 Hari, 30 Hari)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    HistoryFilter.ALL to "Semua",
                    HistoryFilter.TODAY to "Hari Ini",
                    HistoryFilter.LAST_7_DAYS to "7 Hari",
                    HistoryFilter.LAST_30_DAYS to "30 Hari"
                )

                filters.forEach { (filterOption, label) ->
                    val isSelected = activeFilter == filterOption
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter(filterOption) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PastelPillBackground,
                            selectedLabelColor = PastelPillText,
                            containerColor = CleanWhite,
                            labelColor = TextDarkSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = CardBorderPink
                        ),
                        modifier = Modifier.testTag("filter_chip_${label.lowercase().replace(" ", "_")}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Daftar Riwayat atau State Kosong
            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("empty_history_state"),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CleanWhite),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderPink)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(PinkAccentLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = PinkAccentMedium,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Belum ada riwayat aktivitas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Data langkah harian dari sensor ESP32 atau simulasi yang disimpan akan muncul di sini.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextDarkSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.saveSessionToHistory() },
                                colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary, contentColor = CleanWhite),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_save_from_history")
                            ) {
                                Text("Simpan Sesi Sekarang", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("history_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(historyList, key = { it.id }) { record ->
                        HistoryItemCard(record = record)
                    }
                }
            }
        }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text("Hapus Semua Riwayat?", fontWeight = FontWeight.Bold) },
                text = { Text("Semua data riwayat langkah yang tersimpan akan dihapus secara permanen.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearHistory()
                            showClearDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusOfflineRed)
                    ) {
                        Text("Hapus", color = CleanWhite)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
fun HistoryItemCard(
    record: StepRecord,
    modifier: Modifier = Modifier
) {
    val distanceDisplay = if (record.distanceMeters >= 1000) {
        String.format(Locale("id", "ID"), "%.2f km", record.distanceMeters / 1000.0)
    } else {
        "${record.distanceMeters.toInt()} meter"
    }

    val minutes = record.durationSeconds / 60
    val durationDisplay = if (minutes >= 60) {
        "${minutes / 60}j ${minutes % 60}m"
    } else {
        "${minutes} Menit"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("history_item_${record.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CleanWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderPink)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PinkAccentLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint = PinkAccentMedium,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = record.dateString,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = record.timeString,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDarkMuted,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Jarak: $distanceDisplay",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDarkSecondary,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDarkMuted,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "Durasi: $durationDisplay",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDarkSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%,d", record.steps),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = RaspberryPrimary,
                    fontSize = 16.sp
                )
                Text(
                    text = "langkah",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDarkMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}
