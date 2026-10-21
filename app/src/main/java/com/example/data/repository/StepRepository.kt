package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDatabase
import com.example.data.model.ConnectionState
import com.example.data.model.CvStatusState
import com.example.data.model.RealtimeMonitoringData
import com.example.data.model.StepRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class StepRepository(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val stepDao = db.stepDao()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("smartstep_prefs", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    private val scope = CoroutineScope(Dispatchers.IO)
    private var simulationJob: Job? = null
    private var cvPollingJob: Job? = null

    private val _monitoringData = MutableStateFlow(
        RealtimeMonitoringData(
            steps = prefs.getInt(KEY_CURRENT_STEPS, 3420),
            targetSteps = prefs.getInt(KEY_TARGET_STEPS, 6000),
            distanceMeters = prefs.getFloat(KEY_CURRENT_DISTANCE, 2565.0f).toDouble(),
            durationSeconds = prefs.getLong(KEY_CURRENT_DURATION, 1840L),
            deviceStatus = ConnectionState.DEMO,
            firebaseStatus = ConnectionState.DEMO,
            cvStatus = CvStatusState.INACTIVE,
            cvSteps = 0,
            cvBackendUrl = prefs.getString(KEY_CV_URL, "http://192.168.1.100:8000") ?: "http://192.168.1.100:8000",
            isDemoMode = prefs.getBoolean(KEY_IS_DEMO, true),
            lastUpdated = getFormattedCurrentTime()
        )
    )
    val monitoringData: StateFlow<RealtimeMonitoringData> = _monitoringData.asStateFlow()

    val allHistory: Flow<List<StepRecord>> = stepDao.getAllRecords()

    init {
        scope.launch {
            if (stepDao.getCount() == 0) {
                seedInitialHistory()
            }
        }
    }

    fun setTargetSteps(newTarget: Int) {
        val validTarget = newTarget.coerceAtLeast(500)
        prefs.edit().putInt(KEY_TARGET_STEPS, validTarget).apply()
        _monitoringData.update { it.copy(targetSteps = validTarget) }
    }

    fun setCvBackendUrl(url: String) {
        val trimmed = url.trim()
        prefs.edit().putString(KEY_CV_URL, trimmed).apply()
        _monitoringData.update { it.copy(cvBackendUrl = trimmed) }
    }

    fun toggleMode(isDemo: Boolean) {
        prefs.edit().putBoolean(KEY_IS_DEMO, isDemo).apply()
        if (isDemo) {
            _monitoringData.update {
                it.copy(
                    isDemoMode = true,
                    deviceStatus = ConnectionState.DEMO,
                    firebaseStatus = ConnectionState.DEMO
                )
            }
        } else {
            // In LIVE mode: unless real data comes in, status is DISCONNECTED / WAITING
            stopSimulation()
            _monitoringData.update {
                it.copy(
                    isDemoMode = false,
                    deviceStatus = ConnectionState.DISCONNECTED,
                    firebaseStatus = ConnectionState.DISCONNECTED
                )
            }
        }
    }

    /**
     * Menambahkan langkah secara manual untuk simulasi/pengujian.
     */
    fun addSteps(addedSteps: Int) {
        _monitoringData.update { current ->
            val updatedSteps = (current.steps + addedSteps).coerceAtLeast(0)
            // Rumus jarak: rata-rata 1 langkah = 0.75 meter
            val updatedDistance = updatedSteps * 0.75
            // Estimasi penambahan durasi: ~1 detik per langkah
            val updatedDuration = current.durationSeconds + addedSteps
            val nowTime = getFormattedCurrentTime()

            prefs.edit()
                .putInt(KEY_CURRENT_STEPS, updatedSteps)
                .putFloat(KEY_CURRENT_DISTANCE, updatedDistance.toFloat())
                .putLong(KEY_CURRENT_DURATION, updatedDuration)
                .apply()

            current.copy(
                steps = updatedSteps,
                distanceMeters = updatedDistance,
                durationSeconds = updatedDuration,
                lastUpdated = nowTime
            )
        }
    }

    /**
     * Memulai simulasi langkah otomatis (Mode Demo)
     */
    fun startSimulation() {
        if (simulationJob?.isActive == true) return
        simulationJob = scope.launch {
            while (isActive) {
                delay(2000L)
                if (_monitoringData.value.isDemoMode) {
                    addSteps(2)
                }
            }
        }
    }

    fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
    }

    fun isSimulationRunning(): Boolean = simulationJob?.isActive == true

    /**
     * Reset sesi langkah hari ini
     */
    fun resetCurrentSession() {
        _monitoringData.update {
            it.copy(
                steps = 0,
                distanceMeters = 0.0,
                durationSeconds = 0L,
                lastUpdated = getFormattedCurrentTime()
            )
        }
        prefs.edit()
            .putInt(KEY_CURRENT_STEPS, 0)
            .putFloat(KEY_CURRENT_DISTANCE, 0f)
            .putLong(KEY_CURRENT_DURATION, 0L)
            .apply()
    }

    /**
     * Simpan sesi saat ini ke riwayat
     */
    suspend fun saveCurrentSessionToHistory() {
        val current = _monitoringData.value
        if (current.steps <= 0) return

        val now = System.currentTimeMillis()
        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val timeFmt = SimpleDateFormat("HH:mm 'WIB'", Locale("id", "ID"))
        val dateStr = dateFmt.format(Date(now))
        val timeStr = timeFmt.format(Date(now))

        val record = StepRecord(
            dateString = dateStr,
            timeString = timeStr,
            timestamp = now,
            steps = current.steps,
            distanceMeters = current.distanceMeters,
            durationSeconds = current.durationSeconds
        )

        withContext(Dispatchers.IO) {
            stepDao.insertRecord(record)
        }
    }

    suspend fun clearHistory() {
        withContext(Dispatchers.IO) {
            stepDao.clearAll()
        }
    }

    /**
     * Memeriksa status dan mengambil data dari backend Computer Vision (FastAPI / Flask)
     * Endpoint API: GET /api/steps atau GET /api/status
     */
    suspend fun checkCvBackend(): Result<String> = withContext(Dispatchers.IO) {
        val url = _monitoringData.value.cvBackendUrl.trimEnd('/')
        _monitoringData.update { it.copy(cvStatus = CvStatusState.CONNECTING) }
        try {
            val request = Request.Builder()
                .url("$url/api/steps")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string().orEmpty()
                    val json = JSONObject(bodyString)
                    val cvSteps = json.optInt("steps", 0)
                    val statusMsg = json.optString("status", "Active")

                    _monitoringData.update {
                        it.copy(
                            cvStatus = CvStatusState.ACTIVE,
                            cvSteps = cvSteps,
                            lastUpdated = getFormattedCurrentTime()
                        )
                    }
                    Result.success("Terhubung ke Backend CV: $cvSteps langkah terdeteksi ($statusMsg)")
                } else {
                    _monitoringData.update { it.copy(cvStatus = CvStatusState.INACTIVE) }
                    Result.failure(Exception("HTTP ${response.code}: Backend merespon dengan error"))
                }
            }
        } catch (e: Exception) {
            _monitoringData.update { it.copy(cvStatus = CvStatusState.INACTIVE) }
            Result.failure(e)
        }
    }

    private suspend fun seedInitialHistory() {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

        // Contoh riwayat realistis (TIDAK ADA KALORI)
        val initialList = listOf(
            StepRecord(
                dateString = dateFmt.format(Date(now - oneDayMillis)),
                timeString = "06:45 - 07:35 WIB",
                timestamp = now - oneDayMillis,
                steps = 5240,
                distanceMeters = 3930.0,
                durationSeconds = 3000L
            ),
            StepRecord(
                dateString = dateFmt.format(Date(now - 2 * oneDayMillis)),
                timeString = "16:20 - 17:10 WIB",
                timestamp = now - 2 * oneDayMillis,
                steps = 6120,
                distanceMeters = 4590.0,
                durationSeconds = 3000L
            ),
            StepRecord(
                dateString = dateFmt.format(Date(now - 3 * oneDayMillis)),
                timeString = "07:00 - 07:40 WIB",
                timestamp = now - 3 * oneDayMillis,
                steps = 4100,
                distanceMeters = 3075.0,
                durationSeconds = 2400L
            ),
            StepRecord(
                dateString = dateFmt.format(Date(now - 4 * oneDayMillis)),
                timeString = "17:00 - 18:00 WIB",
                timestamp = now - 4 * oneDayMillis,
                steps = 6850,
                distanceMeters = 5137.5,
                durationSeconds = 3600L
            ),
            StepRecord(
                dateString = dateFmt.format(Date(now - 5 * oneDayMillis)),
                timeString = "06:30 - 07:15 WIB",
                timestamp = now - 5 * oneDayMillis,
                steps = 3890,
                distanceMeters = 2917.5,
                durationSeconds = 2700L
            )
        )
        for (item in initialList) {
            stepDao.insertRecord(item)
        }
    }

    private fun getFormattedCurrentTime(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss 'WIB'", Locale("id", "ID"))
        return sdf.format(Date())
    }

    companion object {
        private const val KEY_CURRENT_STEPS = "key_current_steps"
        private const val KEY_TARGET_STEPS = "key_target_steps"
        private const val KEY_CURRENT_DISTANCE = "key_current_distance"
        private const val KEY_CURRENT_DURATION = "key_current_duration"
        private const val KEY_CV_URL = "key_cv_url"
        private const val KEY_IS_DEMO = "key_is_demo"
    }
}
