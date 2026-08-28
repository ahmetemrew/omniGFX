package com.basitce.gfx.ui.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basitce.gfx.core.core_engine.shizuku.RemoteFileBrowser
import com.basitce.gfx.core.core_engine.shizuku.RemoteFileSystemEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RemoteBrowserUiState(
    val currentPath: String = "/",
    val entries: List<RemoteFileSystemEntry> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilePath: String? = null
)

@HiltViewModel
class RemoteBrowserViewModel @Inject constructor(
    private val remoteFileBrowser: RemoteFileBrowser
) : ViewModel() {

    private val _uiState = MutableStateFlow(RemoteBrowserUiState())
    val uiState = _uiState.asStateFlow()

    init {
        load("/")
    }

    fun load(path: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    currentPath = path,
                    selectedFilePath = null
                )
            }

            val result = remoteFileBrowser.list(path)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    entries = result.entries,
                    error = result.error,
                    currentPath = result.path
                )
            }
        }
    }

    fun goUp() {
        val parent = remoteFileBrowser.parentPath(_uiState.value.currentPath)

        if (parent != null) {
            load(parent)
        }
    }

    fun onEntryClick(entry: RemoteFileSystemEntry) {
        if (entry.isDirectory) {
            load(entry.path)
        } else {
            _uiState.update {
                it.copy(selectedFilePath = entry.path)
            }
        }
    }

    fun selectCurrentDirectory() {
        _uiState.update {
            it.copy(selectedFilePath = it.currentPath)
        }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(selectedFilePath = null)
        }
    }
}
