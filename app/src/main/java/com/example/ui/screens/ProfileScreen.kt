package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StepViewModel
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: StepViewModel,
    modifier: Modifier = Modifier
) {
    val monitoringData by viewModel.monitoringData.collectAsState()

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
                .testTag("profile_screen_content"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_header_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanWhite),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderPink)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(PinkAccentLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsWalk,
                                contentDescription = null,
                                tint = RaspberryPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "SMARTSTEP",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = RaspberryPrimary,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "IoT & Computer Vision Footstep Monitor",
                            style = MaterialTheme.typography.bodySmall,
                            color = PinkAccentMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "SMARTSTEP adalah aplikasi monitoring langkah kaki yang dirancang untuk menampilkan jumlah langkah, target langkah harian, jarak tempuh, durasi aktivitas, riwayat aktivitas, dan status perangkat. Sistem dirancang untuk mendukung pembacaan data dari ESP32 melalui Firebase serta pengembangan Computer Vision menggunakan kamera.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDarkSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Tim Pembuat Card (Bunga Gloria Paembonan & Tasiah)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("author_card"),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = PinkAccentMedium, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tim Pengembang Proyek",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkPrimary
                            )
                        }
                        Text(
                            text = "Mahasiswa Teknik Elektro",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDarkMuted,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        AuthorItem(
                            number = "1",
                            name = "Bunga Gloria Paembonan",
                            role = "Pengembang Sistem IoT & Aplikasi SMARTSTEP"
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = CardBorderPink, thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        AuthorItem(
                            number = "2",
                            name = "Tasiah",
                            role = "Pengembang Sistem IoT & Computer Vision"
                        )
                    }
                }
            }

            // Mode DEMO vs LIVE Switcher
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mode_switch_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanWhite),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderPink)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (monitoringData.isDemoMode) "Mode: DEMO (Simulasi)" else "Mode: LIVE (Sensor Riil)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (monitoringData.isDemoMode) StatusDemoBerry else StatusOnlineGreen
                            )
                            Text(
                                text = if (monitoringData.isDemoMode) {
                                    "Data simulasi langkah untuk demonstrasi tanpa perangkat fisik."
                                } else {
                                    "Menunggu data dari sensor ESP32 / Firebase."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextDarkSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = monitoringData.isDemoMode,
                            onCheckedChange = { viewModel.toggleMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CleanWhite,
                                checkedTrackColor = RaspberryPrimary,
                                uncheckedThumbColor = TextDarkMuted,
                                uncheckedTrackColor = PinkAccentLight
                            ),
                            modifier = Modifier.testTag("switch_demo_mode")
                        )
                    }
                }
            }

            // Spesifikasi Sistem
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("device_specs_card"),
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
                        Text(
                            text = "Informasi Perangkat & Sistem",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        SpecItem(icon = Icons.Default.Memory, title = "Mikrokontroler", desc = "ESP32 DevKit V1 (Wi-Fi 2.4 GHz)")
                        SpecItem(icon = Icons.Default.Sensors, title = "Sensor Langkah", desc = "MPU6050 (Akselerometer 3-Axis) / Sensor Getar")
                        SpecItem(icon = Icons.Default.CameraAlt, title = "Computer Vision", desc = "OpenCV + MediaPipe Pose (Webcam Python)")
                        SpecItem(icon = Icons.Default.CloudDone, title = "Cloud Database", desc = "Firebase Cloud Firestore")
                        SpecItem(icon = Icons.Default.PhoneAndroid, title = "Platform Aplikasi", desc = "Android Native (Jetpack Compose) / Web")
                    }
                }
            }

            // Motivational Footer Banner matching screenshot
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SoftCardGradientStart,
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderPink)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PinkAccentLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = RaspberryPrimary, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Langkah kecil untuk perubahan besar ♥",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = RaspberryPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dibuat oleh Bunga Gloria Paembonan & Tasiah",
                            fontSize = 11.sp,
                            color = TextDarkSecondary
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun AuthorItem(number: String, name: String, role: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(PastelPillBackground),
            contentAlignment = Alignment.Center
        ) {
            Text(text = number, fontWeight = FontWeight.Bold, color = PastelPillText, fontSize = 12.sp)
        }
        Column {
            Text(text = name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextDarkPrimary)
            Text(text = role, style = MaterialTheme.typography.bodySmall, color = TextDarkSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun SpecItem(icon: ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PinkAccentLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = PinkAccentMedium, modifier = Modifier.size(16.dp))
        }
        Column {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = TextDarkMuted, fontSize = 10.sp)
            Text(text = desc, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = TextDarkPrimary, fontSize = 11.sp)
        }
    }
}
