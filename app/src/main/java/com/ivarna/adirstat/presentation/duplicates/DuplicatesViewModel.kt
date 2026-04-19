package com.ivarna.adirstat.presentation.duplicates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivarna.adirstat.domain.model.FileNode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DuplicatesUiState(
    val isLoading: Boolean = true,
    val duplicateGroups: List<DuplicateGroup> = emptyList(),
    val totalWastedSpace: Long = 0,
    val error: String? = null
)

data class DuplicateGroup(
    val files: List<FileNode>,
    val fileSize: Long,
    val wastedSpace: Long
)

@HiltViewModel
class DuplicatesViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DuplicatesUiState())
    val uiState: StateFlow<DuplicatesUiState> = _uiState.asStateFlow()

    init {
        findDuplicates()
    }

    fun findDuplicates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // This would typically scan all files and find duplicates
                // For now, returning empty list until full implementation
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        duplicateGroups = emptyList(),
                        totalWastedSpace = 0
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteDuplicates(group: DuplicateGroup) {
        viewModelScope.launch {
            // Delete all except first
            group.files.drop(1).forEach { file ->
                if (file is FileNode.File) {
                    deleteFile(file)
                }
            }
            findDuplicates()
        }
    }

    fun deleteFile(file: FileNode.File) {
        viewModelScope.launch {
            try {
                // Delete file and refresh duplicates
                findDuplicates()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
