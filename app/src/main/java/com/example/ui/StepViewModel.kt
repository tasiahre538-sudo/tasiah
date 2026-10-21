package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.RealtimeMonitoringData
import com.example.data.model.StepRecord
import com.example.data.repository.StepRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HistoryFilter {
    ALL,
    TODAY,
    LAST_7_DAYS,
    LAST_30_DAYS
}

class StepViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StepRepository(application)

    val monitoringData: StateFlow<RealtimeMonitoringData> = repository.monitoringData

    private val _historyFilter = MutableStateFlow(HistoryFilter.ALL)
    val historyFilter: StateFlow<HistoryFilter> = _historyFilter.asStateFlow()

    private val _isAutoSimulating = MutableStateFlow(false)
    val isAutoSimulating: StateFlow<Boolean> = _isAutoSimulating.asStateFlow()

    private val _cvFeedbackMessage = MutableStateFlow<String?>(null)
    val cvFeedbackMessage: StateFlow<String?> = _cvFeedbackMessage.asStateFlow()

    val filteredHistoryList: StateFlow<List<StepRecord>> = combine(
        repository.allHistory,
        _historyFilter
    ) { list, filter ->
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        when (filter) {
            HistoryFilter.ALL -> list
            HistoryFilter.TODAY -> list.filter { it.timestamp >= (now - oneDay) }
            HistoryFilter.LAST_7_DAYS -> list.filter { it.timestamp >= (now - 7 * oneDay) }
            HistoryFilter.LAST_30_DAYS -> list.filter { it.timestamp >= (now - 30 * oneDay) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setTargetSteps(target: Int) {
        repository.setTargetSteps(target)
    }

    fun setCvBackendUrl(url: String) {
        repository.setCvBackendUrl(url)
    }

    fun toggleMode(isDemo: Boolean) {
        if (!isDemo && _isAutoSimulating.value) {
            stopAutoSimulation()
        }
        repository.toggleMode(isDemo)
    }

    fun addSteps(amount: Int) {
        repository.addSteps(amount)
    }

    fun toggleAutoSimulation() {
        if (_isAutoSimulating.value) {
            stopAutoSimulation()
        } else {
            startAutoSimulation()
        }
    }

    fun startAutoSimulation() {
        _isAutoSimulating.value = true
        repository.startSimulation()
    }

    fun stopAutoSimulation() {
        _isAutoSimulating.value = false
        repository.stopSimulation()
    }

    fun resetSession() {
        repository.resetCurrentSession()
    }

    fun saveSessionToHistory() {
        viewModelScope.launch {
            repository.saveCurrentSessionToHistory()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun checkCvBackend() {
        viewModelScope.launch {
            val result = repository.checkCvBackend()
            result.onSuccess { msg ->
                _cvFeedbackMessage.value = msg
            }.onFailure { err ->
                _cvFeedbackMessage.value = "Koneksi CV Gagal: ${err.localizedMessage ?: "Tidak dapat tersambung"}"
            }
        }
    }

    fun clearCvFeedback() {
        _cvFeedbackMessage.value = null
    }

    fun setFilter(filter: HistoryFilter) {
        _historyFilter.value = filter
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopSimulation()
    }
}
