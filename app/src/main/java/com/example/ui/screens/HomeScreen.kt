package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StepViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: StepViewModel,
    modifier: Modifier = Modifier
) {
    val monitoringData by viewModel.monitoringData.collectAsState()
    val isAutoSimulating by viewModel.isAutoSimulating.collectAsState()

    val percentage = if (monitoringData.targetSteps > 0) {
        (monitoringData.steps.toFloat() / monitoringData.targetSteps.toFloat()) * 100f
    } else 0f
    val remainingSteps = (monitoringData.targetSteps - monitoringData.steps).coerceAtLeast(0)

    val todayFormatted = remember {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        sdf.format(Date())
    }

    val distanceKmString = String.format(Locale("id", "ID"), "%.2f", monitoringData.distanceMeters / 1000.0)
    val durationMinutes = monitoringData.durationSeconds / 60

    var showSaveSnackbar by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BlushBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .align(Alignment.TopCenter)
                .testTag("home_screen_content"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Top Navbar Card (Logo, DEMO badge, Status ESP32, Firebase)
            item {
                SmartStepTopNavHeader(
                    isDemoMode = monitoringData.isDemoMode,
                    esp32State = monitoringData.deviceStatus,
                    firebaseState = monitoringData.firebaseStatus,
                    onToggleMode = { viewModel.toggleMode(it) }
                )
            }

            // 2. Greeting Header Section: "Ringkasan Aktivitas" + Date Pill
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(PinkAccentLight, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrackChanges,
                                contentDescription = null,
                                tint = PinkAccentMedium,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Ringkasan Aktivitas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkPrimary,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Selamat datang di SMARTSTEP! Tetap semangat untuk hari yang lebih sehat.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextDarkSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Date Pill with calendar icon
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CleanWhite,
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderPink)),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = TextDarkSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = todayFormatted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextDarkPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TextDarkMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // 3. Metric Cards Grid (Jumlah Langkah, Target Langkah, Jarak Tempuh, Durasi)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ReferenceMetricCard(
                            title = "Jumlah Langkah",
                            value = String.format(Locale("id", "ID"), "%,d", monitoringData.steps),
                            unit = "langkah",
                            icon = Icons.Default.DirectionsWalk,
                            modifier = Modifier.weight(1f)
                        )
                        ReferenceMetricCard(
                            title = "Target Langkah",
                            value = String.format(Locale("id", "ID"), "%,d", monitoringData.targetSteps),
                            unit = "langkah / hari",
                            icon = Icons.Default.TrackChanges,
                            showProgressBar = true,
                            progressPercentage = percentage,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ReferenceMetricCard(
                            title = "Jarak Tempuh",
                            value = distanceKmString,
                            unit = "km",
                            icon = Icons.Default.DirectionsWalk,
                            modifier = Modifier.weight(1f)
                        )
                        ReferenceMetricCard(
                            title = "Durasi Aktivitas",
                            value = "$durationMinutes",
                            unit = "menit",
                            icon = Icons.Default.Schedule,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4. Middle Section: Progres Target Harian (Donut Ring)
            item {
                ReferenceCircularProgressCard(
                    currentSteps = monitoringData.steps,
                    targetSteps = monitoringData.targetSteps,
                    percentage = percentage,
                    remainingSteps = remainingSteps
                )
            }

            // 5. Line Chart: Grafik Aktivitas Langkah (7 Hari Terakhir)
            item {
                ReferenceStepLineChart()
            }

            // 6. Status Sistem Card
            item {
                ReferenceSystemStatusCard(
                    esp32State = monitoringData.deviceStatus,
                    firebaseState = monitoringData.firebaseStatus
                )
            }

            // 7. Cara Menggunakan SMARTSTEP
            item {
                ReferenceGuideSection()
            }

            // 8. Simulation / Demo Controls Bar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("simulation_control_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanWhite),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderPink)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pengujian Simulasi Langkah (Mode DEMO)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = PastelPillBackground
                            ) {
                                Text(
                                    text = if (isAutoSimulating) "Simulasi Berjalan" else "Standby",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelPillText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.addSteps(10) },
                                modifier = Modifier.weight(1f).height(40.dp).testTag("btn_add_10_steps"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PastelPillBackground, contentColor = PastelPillText)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+10", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { viewModel.addSteps(100) },
                                modifier = Modifier.weight(1f).height(40.dp).testTag("btn_add_50_steps"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PastelPillBackground, contentColor = PastelPillText)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+100", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { viewModel.toggleAutoSimulation() },
                                modifier = Modifier.weight(1.3f).height(40.dp).testTag("btn_toggle_simulation"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAutoSimulating) PinkAccentMedium else RaspberryPrimary,
                                    contentColor = CleanWhite
                                )
                            ) {
                                Icon(
                                    imageVector = if (isAutoSimulating) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAutoSimulating) "Jeda" else "Auto Sim",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.saveSessionToHistory()
                                    showSaveSnackbar = true
                                },
                                modifier = Modifier.weight(1f).height(38.dp).testTag("btn_save_session"),
                                shape = RoundedCornerShape(10.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderPink))
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = PinkAccentMedium)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Simpan Sesi", fontSize = 11.sp, color = TextDarkPrimary)
                            }
                            OutlinedButton(
                                onClick = { viewModel.resetSession() },
                                modifier = Modifier.weight(1f).height(38.dp).testTag("btn_reset_session"),
                                shape = RoundedCornerShape(10.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderPink))
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextDarkMuted)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset Langkah", fontSize = 11.sp, color = TextDarkPrimary)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (showSaveSnackbar) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { showSaveSnackbar = false }) {
                        Text("TUTUP", color = CleanWhite)
                    }
                },
                containerColor = RaspberryPrimary
            ) {
                Text("Sesi langkah berhasil dicatat ke Riwayat!", color = CleanWhite)
            }
        }
    }
}
