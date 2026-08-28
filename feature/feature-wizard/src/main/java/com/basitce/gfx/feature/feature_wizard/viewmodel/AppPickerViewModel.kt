package com.basitce.gfx.feature.feature_wizard.viewmodel

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppPickerViewModel @Inject constructor(
    @ApplicationContext val context: Context
) : ViewModel() {

    private val _installedApps = MutableStateFlow<List<ApplicationInfo>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(true)

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val filteredApps: StateFlow<List<ApplicationInfo>> = combine(
        _installedApps,
        _searchQuery
    ) { apps, query ->
        if (query.isBlank()) {
            apps
        } else {
            val normalizedQuery = query.trim().lowercase()
            apps.filter {
                it.loadLabel(context.packageManager).toString().lowercase().contains(normalizedQuery) ||
                    it.packageName.lowercase().contains(normalizedQuery)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val pm = context.packageManager
            try {
                val allApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                val filtered = allApps
                    .filter { appInfo ->
                        // OmniGFX kendisini listelemesin
                        if (appInfo.packageName == context.packageName) return@filter false

                        val isUserApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                            (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                        val hasLaunchIntent = pm.getLaunchIntentForPackage(appInfo.packageName) != null

                        isUserApp || hasLaunchIntent
                    }
                    .sortedBy { it.loadLabel(pm).toString().lowercase() }

                if (filtered.isNotEmpty()) {
                    _installedApps.value = filtered
                } else {
                    // Fallback to launcher activities query
                    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                    }
                    val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
                    val apps = resolveInfos.mapNotNull { it.activityInfo?.applicationInfo }
                        .filter { it.packageName != context.packageName }
                        .distinctBy { it.packageName }
                        .sortedBy { it.loadLabel(pm).toString().lowercase() }
                    _installedApps.value = apps
                }
            } catch (e: Exception) {
                val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
                val apps = resolveInfos.mapNotNull { it.activityInfo?.applicationInfo }
                    .filter { it.packageName != context.packageName }
                    .distinctBy { it.packageName }
                    .sortedBy { it.loadLabel(pm).toString().lowercase() }
                _installedApps.value = apps
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
