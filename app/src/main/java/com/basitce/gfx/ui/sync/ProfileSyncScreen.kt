package com.basitce.gfx.ui.sync

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.basitce.gfx.core.core_engine.profile.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProfileTransferViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ProfileRepository,
    private val analyzer: ProfileTemplateAnalyzer
) : ViewModel() {
    val busy = MutableStateFlow(false)
    val message = MutableStateFlow<String?>(null)

    fun exportTo(uri: Uri) = runOperation {
        val profiles = repository.getAllProfiles()
        val array = JSONArray()
        profiles.forEach { array.put(JSONObject(repository.exportProfileAsString(it))) }
        val document = JSONObject().put("format", "omnigfx-profiles").put("version", 1).put("profiles", array)
        context.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter()?.use { it.write(document.toString(2)) }
            ?: error("Dosya açılamadı.")
        "${profiles.size} profil dışa aktarıldı."
    }

    fun importFrom(uri: Uri) = runOperation {
        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (output.size() <= 10 * 1024 * 1024) {
                val count = input.read(buffer)
                if (count < 0) break
                output.write(buffer, 0, count)
            }
            output.toByteArray() }
            ?: error("Dosya okunamadı.")
        require(bytes.size <= 10 * 1024 * 1024) { "Dosya en fazla 10 MB olabilir." }
        val root = JSONObject(bytes.toString(Charsets.UTF_8))
        val array = if (root.optString("format") == "omnigfx-profiles") {
            require(root.getInt("version") == 1) { "Desteklenmeyen aktarım sürümü." }
            root.getJSONArray("profiles")
        } else JSONArray().put(root)
        val profiles = (0 until array.length()).map { index ->
            val profile = repository.importProfileFromString(array.getJSONObject(index).toString())
            require(profile.name.isNotBlank()) { "Profil adı boş olamaz." }
            val analysis = analyzer.analyzeProfile(profile.copy(options = profile.options.copy(allowUnsafeSystemPaths = false)))
            require(analysis.canImport) { "${profile.name}: ${analysis.findings.joinToString { it.message }}" }
            profile.copy(id = UUID.randomUUID().toString())
        }
        profiles.forEach { repository.saveProfile(it) }
        "${profiles.size} profil içe aktarıldı. Profiller sekmesinden uygulayabilirsiniz."
    }

    private fun runOperation(block: suspend () -> String) {
        if (busy.value) return
        busy.value = true
        message.value = null
        viewModelScope.launch {
            try { message.value = withContext(Dispatchers.IO) { block() } }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { message.value = e.message ?: "Aktarım tamamlanamadı." }
            finally { busy.value = false }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSyncScreen(onBack: () -> Unit, viewModel: ProfileTransferViewModel = hiltViewModel()) {
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) {
        it?.let(viewModel::exportTo)
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        it?.let(viewModel::importFrom)
    }
    Scaffold(topBar = {
        TopAppBar(title = { Text("Profil Aktarımı") }, navigationIcon = { TextButton(onClick = onBack) { Text("Geri") } })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Profillerini JSON dosyası olarak yedekle veya başka bir cihaza taşı.", style = MaterialTheme.typography.titleMedium)
            Text("Aktarım cihazın dosya seçicisiyle yapılır. Bulut hesabı gerekmez. İçe aktarılan profiller yeni kopyalar olarak kaydedilir; oyun dosyalarına otomatik yazılmaz.")
            Button(onClick = { exporter.launch("OmniGFX-profiller.json") }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Profilleri Dışa Aktar") }
            OutlinedButton(onClick = { importer.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("JSON Dosyasından İçe Aktar") }
            if (busy) CircularProgressIndicator()
            message?.let { Text(it) }
        }
    }
}
