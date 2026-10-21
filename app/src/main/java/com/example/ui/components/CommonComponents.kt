package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionState
import com.example.ui.theme.*

@Composable
fun SmartStepTopNavHeader(
    isDemoMode: Boolean,
    esp32State: ConnectionState,
    firebaseState: ConnectionState,
    onToggleMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_nav_header"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CleanWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderPink)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PinkAccentLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = "SMARTSTEP Logo",
                        tint = RaspberryPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "SMARTSTEP",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = RaspberryPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Monitoring Langkah Kaki",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDarkSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Status Pills (DEMO badge, ESP32, Firebase)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Solid DEMO Badge matching screenshot
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isDemoMode) StatusDemoBerry else StatusOnlineGreen,
                    modifier = Modifier.testTag("demo_badge")
                ) {
                    Text(
                        text = if (isDemoMode) "DEMO" else "LIVE",
                        color = CleanWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                // ESP32 Status Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CleanWhite,
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderPink)),
                    modifier = Modifier.testTag("esp32_top_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (esp32State == ConnectionState.CONNECTED) StatusOnlineGreen else StatusOfflineRed)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (esp32State == ConnectionState.CONNECTED) "ESP32 Aktif" else "ESP32 Offline",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDarkSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReferenceMetricCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    showProgressBar: Boolean = false,
    progressPercentage: Float = 0f,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("metric_card_${title.lowercase().replace(" ", "_")}"),
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
            // Icon & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(PinkAccentLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = PinkAccentMedium,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDarkSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Value & DEMO pill badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkPrimary,
                        fontSize = 22.sp
                    )
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDarkMuted,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PastelPillBackground
                ) {
                    Text(
                        text = "DEMO",
                        color = PastelPillText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            if (showProgressBar) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { (progressPercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = PinkAccentMedium,
                        trackColor = PinkAccentLight
                    )
                    Text(
                        text = "${progressPercentage.toInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun ReferenceCircularProgressCard(
    currentSteps: Int,
    targetSteps: Int,
    percentage: Float,
    remainingSteps: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (currentSteps.toFloat() / targetSteps.toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "circularProgress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("progres_target_harian_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CleanWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderPink)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsWalk,
                    contentDescription = null,
                    tint = PinkAccentMedium,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Progres Target Harian",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextDarkPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Soft Pink Donut Progress Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                Canvas(modifier = Modifier.size(150.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Track Ring
                    drawCircle(
                        color = PinkAccentLight,
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )

                    // Active Progress Ring
                    drawArc(
                        color = PinkAccentMedium,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${percentage.toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = TextDarkPrimary,
                        fontSize = 24.sp
                    )
                    Text(
                        text = "${String.format("%,d", currentSteps)} / ${String.format("%,d", targetSteps)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDarkMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "langkah",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDarkMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = CardBorderPink, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Sisa Langkah & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(PinkAccentLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            tint = PinkAccentMedium,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Column {
                        Text(text = "Sisa langkah", fontSize = 10.sp, color = TextDarkMuted)
                        Text(
                            text = "${String.format("%,d", remainingSteps)} langkah",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkPrimary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PastelPillBackground
                ) {
                    Text(
                        text = if (currentSteps == 0) "Belum mulai" else if (remainingSteps == 0) "Tercapai!" else "Sedang berjalan",
                        color = PastelPillText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ReferenceStepLineChart(
    modifier: Modifier = Modifier
) {
    // 7 Hari Terakhir
    val days = listOf("14 Okt", "15 Okt", "16 Okt", "17 Okt", "18 Okt", "19 Okt", "20 Okt")
    val steps = listOf(0, 0, 0, 0, 0, 0, 0) // baseline matching screenshot, or dynamic

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("grafik_aktivitas_langkah_card"),
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
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint = PinkAccentMedium,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Grafik Aktivitas Langkah",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PastelPillBackground
                ) {
                    Text(
                        text = "7 Hari Terakhir",
                        color = PastelPillText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PinkAccentMedium)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Langkah", fontSize = 10.sp, color = TextDarkMuted)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grid Line Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height - 20.dp.toPx()

                    // Horizontal Grid Lines (8.000, 6.000, 4.000, 2.000, 0)
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = h * (i.toFloat() / gridLines)
                        drawLine(
                            color = CardBorderPink.copy(alpha = 0.8f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Pink Baseline Path
                    val path = Path()
                    val points = days.size
                    for (i in 0 until points) {
                        val x = w * (i.toFloat() / (points - 1))
                        val y = h // at 0 steps baseline
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)

                        // Dot at baseline
                        drawCircle(
                            color = PinkAccentMedium,
                            radius = 3.5.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }

                    drawPath(
                        path = path,
                        color = PinkAccentMedium,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // X-Axis Labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    days.forEach { day ->
                        Text(
                            text = day,
                            fontSize = 9.sp,
                            color = TextDarkMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReferenceSystemStatusCard(
    esp32State: ConnectionState,
    firebaseState: ConnectionState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("status_sistem_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CleanWhite),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderPink)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Status Sistem",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextDarkPrimary
            )

            // ESP32 Status Item
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PinkAccentLight.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PinkAccentLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = PinkAccentMedium, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("ESP32", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextDarkPrimary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (esp32State == ConnectionState.CONNECTED) StatusOnlineGreen else StatusOfflineRed)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (esp32State == ConnectionState.CONNECTED) "Terhubung" else "Tidak Terhubung",
                                    fontSize = 11.sp,
                                    color = TextDarkSecondary
                                )
                            }
                        }
                    }

                    Surface(shape = RoundedCornerShape(10.dp), color = PastelPillBackground) {
                        Text("DEMO", color = PastelPillText, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            // Firebase Status Item
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PinkAccentLight.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PinkAccentLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Whatshot, contentDescription = null, tint = Color(0xFFFF7043), modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Firebase", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextDarkPrimary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (firebaseState == ConnectionState.CONNECTED) StatusOnlineGreen else StatusOfflineRed)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (firebaseState == ConnectionState.CONNECTED) "Terhubung" else "Tidak Terhubung",
                                    fontSize = 11.sp,
                                    color = TextDarkSecondary
                                )
                            }
                        }
                    }

                    Surface(shape = RoundedCornerShape(10.dp), color = PastelPillBackground) {
                        Text("DEMO", color = PastelPillText, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            // Project Authors Banner matching screenshot
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SoftCardGradientStart,
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderPink)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "SMARTSTEP",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = RaspberryPrimary
                    )
                    Text(
                        text = "Sistem monitoring langkah kaki berbasis IoT dan Computer Vision.",
                        fontSize = 10.sp,
                        color = TextDarkSecondary,
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pembuat:\n1. Bunga Gloria Paembonan\n2. Tasiah",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDarkPrimary,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ReferenceGuideSection(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cara_menggunakan_smartstep_card"),
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
            Text(
                text = "Cara Menggunakan SMARTSTEP",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextDarkPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))

            val steps = listOf(
                Pair("1", "Login / Register" to "Masuk atau buat akun untuk mulai menggunakan aplikasi."),
                Pair("2", "Hubungkan Perangkat" to "Pastikan ESP32 terhubung dengan Wi-Fi dan Firebase."),
                Pair("3", "Lihat Data" to "Pantau jumlah langkah, jarak, dan durasi aktivitas."),
                Pair("4", "Cek Riwayat" to "Lihat riwayat aktivitas harian Anda."),
                Pair("5", "Atur Target" to "Tentukan target langkah harian sesuai kebutuhan.")
            )

            steps.forEach { (number, info) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(PastelPillBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = number, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PastelPillText)
                    }
                    Column {
                        Text(text = info.first, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkPrimary)
                        Text(text = info.second, fontSize = 11.sp, color = TextDarkSecondary, lineHeight = 15.sp)
                    }
                }
            }
        }
    }
}
