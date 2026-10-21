package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Whatshot
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
import com.example.data.model.ConnectionState
import com.example.data.model.CvStatusState
import com.example.ui.StepViewModel
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun MonitoringScreen(
    viewModel: StepViewModel,
    modifier: Modifier = Modifier
) {
    val monitoringData by viewModel.monitoringData.collectAsState()
    val cvFeedback by viewModel.cvFeedbackMessage.collectAsState()

    var showTargetDialog by remember { mutableStateOf(false) }
    var targetInputText by remember { mutableStateOf(monitoringData.targetSteps.toString()) }
    var backendUrlInput by remember { mutableStateOf(monitoringData.cvBackendUrl) }

    val percentage = if (monitoringData.targetSteps > 0) {
        (monitoringData.steps.toFloat() / monitoringData.targetSteps.toFloat()) * 100f
    } else 0f
    val remainingSteps = (monitoringData.targetSteps - monitoringData.steps).coerceAtLeast(0)

    val distanceDisplay = if (monitoringData.distanceMeters >= 1000) {
        String.format(Locale("id", "ID"), "%.2f km", monitoringData.distanceMeters / 1000.0)
    } else {
        "${monitoringData.distanceMeters.toInt()} meter"
    }

    val hours = monitoringData.durationSeconds / 3600
    val minutes = (monitoringData.durationSeconds % 3600) / 60
    val seconds = monitoringData.durationSeconds % 60
    val durationDisplay = if (hours > 0) {
        "${hours} jam ${minutes} menit"
    } else {
        "${minutes} menit ${seconds} detik"
    }

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
                .testTag("monitoring_screen_content"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Screen
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Monitoring Waktu-Nyata",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDarkPrimary,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Pembaruan: ${monitoringData.lastUpdated}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDarkMuted,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(
                        onClick = { viewModel.checkCvBackend() },
                        modifier = Modifier.testTag("btn_refresh_monitoring")
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Perbarui Data",
                            tint = RaspberryPrimary
                        )
                    }
                }
            }

            // Real-Time Step Hero Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("realtime_telemetry_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanWhite),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderPink)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(PinkAccentLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = PinkAccentMedium, modifier = Modifier.size(20.dp))
                                }
                                Text(
                                    text = "Jumlah Langkah Terkini",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextDarkSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PastelPillBackground
                            ) {
                                Text(
                                    text = String.format(Locale("id", "ID"), "%.1f%% Target", percentage),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelPillText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = String.format("%,d", monitoringData.steps),
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 40.sp),
                            fontWeight = FontWeight.Black,
                            color = RaspberryPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { (percentage / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PinkAccentMedium,
                            trackColor = PinkAccentLight
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = CardBorderPink, thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Sisa Langkah", style = MaterialTheme.typography.bodySmall, color = TextDarkMuted, fontSize = 11.sp)
                                Text(
                                    text = if (remainingSteps > 0) "${String.format("%,d", remainingSteps)} lagi" else "Target Tercapai!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remainingSteps > 0) TextDarkPrimary else StatusOnlineGreen
                                )
                            }
                            Column {
                                Text("Jarak Tempuh", style = MaterialTheme.typography.bodySmall, color = TextDarkMuted, fontSize = 11.sp)
                                Text(
                                    text = distanceDisplay,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkPrimary
                                )
                            }
                            Column {
                                Text("Durasi", style = MaterialTheme.typography.bodySmall, color = TextDarkMuted, fontSize = 11.sp)
                                Text(
                                    text = durationDisplay,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Pengaturan Target Langkah Harian Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("target_setting_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanWhite),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderPink)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = PinkAccentMedium)
                                Text(
                                    text = "Target Langkah Harian",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkPrimary
                                )
                            }
                            TextButton(
                                onClick = {
                                    targetInputText = monitoringData.targetSteps.toString()
                                    showTargetDialog = true
                                },
                                modifier = Modifier.testTag("btn_custom_target")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = RaspberryPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ubah", color = RaspberryPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Text(
                            text = "Target aktif saat ini: ${String.format("%,d", monitoringData.targetSteps)} langkah / hari",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDarkSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(4000, 6000, 8000, 10000).forEach { preset ->
                                val isSelected = monitoringData.targetSteps == preset
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setTargetSteps(preset) },
                                    label = { Text("${preset / 1000}k", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f).testTag("chip_target_$preset"),
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
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // BAGIAN COMPUTER VISION
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cv_section_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanWhite),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderPink)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(PinkAccentLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = "Computer Vision",
                                        tint = PinkAccentMedium,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Computer Vision",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDarkPrimary
                                    )
                                    Text(
                                        text = "OpenCV & MediaPipe Pose",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextDarkMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            val (badgeBg, badgeFg, badgeText) = when (monitoringData.cvStatus) {
                                CvStatusState.ACTIVE -> Triple(Color(0xFFE8F5E9), StatusOnlineGreen, "CV Aktif")
                                CvStatusState.CONNECTING -> Triple(PastelPillBackground, PastelPillText, "Menghubungkan...")
                                CvStatusState.INACTIVE -> Triple(PinkAccentLight, StatusOfflineRed, "CV Belum Aktif")
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = badgeBg,
                                modifier = Modifier.testTag("cv_status_badge")
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeFg,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Modul Computer Vision berjalan di laptop menggunakan kamera webcam untuk mendeteksi pose kaki melalui MediaPipe Pose.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDarkSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = backendUrlInput,
                            onValueChange = {
                                backendUrlInput = it
                                viewModel.setCvBackendUrl(it)
                            },
                            label = { Text("Alamat Backend CV (FastAPI)") },
                            placeholder = { Text("http://192.168.1.100:8000") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("cv_backend_url_field"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PinkAccentMedium,
                                unfocusedBorderColor = CardBorderPink
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.checkCvBackend() },
                            modifier = Modifier.fillMaxWidth().testTag("btn_test_cv_connection"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary, contentColor = CleanWhite)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Uji Koneksi CV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (cvFeedback != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (monitoringData.cvStatus == CvStatusState.ACTIVE) Color(0xFFE8F5E9) else PinkAccentLight,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (monitoringData.cvStatus == CvStatusState.ACTIVE) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (monitoringData.cvStatus == CvStatusState.ACTIVE) StatusOnlineGreen else StatusOfflineRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = cvFeedback.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (monitoringData.cvStatus == CvStatusState.ACTIVE) StatusOnlineGreen else StatusOfflineRed,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (showTargetDialog) {
            AlertDialog(
                onDismissRequest = { showTargetDialog = false },
                title = { Text("Atur Target Langkah", fontWeight = FontWeight.Bold, color = TextDarkPrimary) },
                text = {
                    Column {
                        Text(
                            "Masukkan target langkah harian yang diinginkan:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDarkSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = targetInputText,
                            onValueChange = { targetInputText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Target Langkah") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val parsed = targetInputText.toIntOrNull() ?: 8000
                            viewModel.setTargetSteps(parsed)
                            showTargetDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary, contentColor = CleanWhite)
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTargetDialog = false }) {
                        Text("Batal", color = TextDarkSecondary)
                    }
                }
            )
        }
    }
}
