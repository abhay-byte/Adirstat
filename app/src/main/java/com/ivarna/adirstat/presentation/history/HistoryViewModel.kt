package com.ivarna.adirstat.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivarna.adirstat.data.local.db.ScanHistoryDao
import com.ivarna.adirstat.data.local.db.ScanHistoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanHistoryItem(
    val id: Long,
    val partitionName: String,
    val partitionPath: String,
    val scanDate: Long,
    val totalBytes: Long,
    val freeBytes: Long,
    val fileCount: Int,
    val folderCount: Int,
    val changeFromPrevious: String? = null
)

data class HistoryUiState(
    val isLoading: Boolean = true,
    val history: List<ScanHistoryItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val scanHistoryDao: ScanHistoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                scanHistoryDao.getAllScans().collect { entities ->
                    val items = entities.mapIndexed { index, entity ->
                        val previous = if (index < entities.size - 1) entities[index + 1] else null
                        val change = if (previous != null) {
                            val diff = entity.usedBytes - previous.usedBytes
                            val sign = if (diff >= 0) "+" else ""
                            "$sign${formatSize(diff)}"
                        } else null
                        
                        ScanHistoryItem(
                            id = entity.id,
                            partitionName = entity.partitionName,
                            partitionPath = entity.partitionPath,
                            scanDate = entity.scanDate,
                            totalBytes = entity.totalBytes,
                            freeBytes = entity.freeBytes,
                            fileCount = entity.fileCount.toInt(),
                            folderCount = entity.folderCount.toInt(),
                            changeFromPrevious = change
                        )
                    }
                    _uiState.update { it.copy(isLoading = false, history = items) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun loadScan(scanId: Long) {
        viewModelScope.launch {
            // Load a specific scan from history
        }
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes >= 1_000_000_000 -> String.format("%.1f GB", bytes / 1_000_000_000.0)
            bytes >= 1_000_000 -> String.format("%.1f MB", bytes / 1_000_000.0)
            bytes >= 1_000 -> String.format("%.1f KB", bytes / 1_000.0)
            else -> "$bytes B"
        }
    }
}
