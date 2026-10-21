package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas riwayat aktivitas langkah kaki SMARTSTEP.
 * Sesuai spesifikasi proyek: TIDAK mengandung atribut kalori.
 */
@Entity(tableName = "step_history")
data class StepRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String,      // Contoh: "21 Okt 2026"
    val timeString: String,      // Contoh: "07:30 - 08:15 WIB"
    val timestamp: Long,         // Epoch millis untuk pengurutan & filter
    val steps: Int,              // Jumlah langkah
    val distanceMeters: Double,  // Jarak tempuh dalam meter
    val durationSeconds: Long    // Durasi aktivitas dalam detik
)

/**
 * Status koneksi perangkat IoT dan modul eksternal.
 */
enum class ConnectionState {
    DISCONNECTED, // Belum Terhubung
    CONNECTING,   // Menghubungkan
    CONNECTED,    // Terhubung (LIVE)
    DEMO          // Mode Simulasi DEMO
}

/**
 * Status modul Computer Vision.
 */
enum class CvStatusState {
    INACTIVE,     // Computer Vision belum aktif
    CONNECTING,   // Memeriksa Backend CV
    ACTIVE        // Aktif & Terhubung
}

/**
 * Model data monitoring waktu-nyata (Real-time).
 * Sesuai spesifikasi proyek Teknik Elektro:
 * steps, targetSteps, distanceMeters, durationSeconds, deviceStatus, updatedAt.
 */
data class RealtimeMonitoringData(
    val steps: Int = 0,
    val targetSteps: Int = 6000,
    val distanceMeters: Double = 0.0,
    val durationSeconds: Long = 0L,
    val deviceStatus: ConnectionState = ConnectionState.DISCONNECTED,
    val firebaseStatus: ConnectionState = ConnectionState.DISCONNECTED,
    val cvStatus: CvStatusState = CvStatusState.INACTIVE,
    val cvSteps: Int = 0,
    val cvBackendUrl: String = "http://192.168.1.100:8000",
    val isDemoMode: Boolean = true,
    val lastUpdated: String = "Belum ada pembaruan"
)
