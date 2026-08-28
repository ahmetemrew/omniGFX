package com.basitce.gfx.core.core_engine.workflow

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Workflow draft'larını ve çekilen dosyaları kalıcı olarak saklar.
 *
 * Dizin yapısı:
 * files/omnigfx/workflow/drafts.json   → draft metadata
 * files/omnigfx/workflow/files/        → çekilen dosya içerikleri
 */
@Singleton
class WorkflowCache @Inject constructor(
    @ApplicationContext context: Context
) {
    private val mutex = Mutex()
    private val baseDir = File(context.filesDir, "omnigfx/workflow").apply { mkdirs() }
    private val filesDir = File(baseDir, "files").apply { mkdirs() }
    private val draftsFile = File(baseDir, "drafts.json")

    suspend fun getAllDrafts(): List<WorkflowDraft> {
        return mutex.withLock {
            withContext(Dispatchers.IO) { readDrafts() }
        }
    }

    suspend fun getDraftById(id: String): WorkflowDraft? {
        return mutex.withLock {
            withContext(Dispatchers.IO) {
                readDrafts().firstOrNull { it.id == id }
            }
        }
    }

    suspend fun saveDraft(draft: WorkflowDraft) {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                val drafts = readDrafts().toMutableList()
                drafts.removeAll { it.id == draft.id }
                drafts.add(draft)
                writeDrafts(drafts)

                // Dosya içeriğini de kaydet
                val content = draft.modifiedContent ?: draft.originalContent
                val file = File(filesDir, draft.localFileName)
                file.writeText(content, Charsets.UTF_8)
            }
        }
    }

    suspend fun updateDraftContent(id: String, newContent: String) {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                val drafts = readDrafts().toMutableList()
                val index = drafts.indexOfFirst { it.id == id }
                if (index != -1) {
                    val updated = drafts[index].copy(
                        modifiedContent = newContent,
                        updatedAt = System.currentTimeMillis()
                    )
                    drafts[index] = updated
                    writeDrafts(drafts)

                    val file = File(filesDir, drafts[index].localFileName)
                    file.writeText(newContent, Charsets.UTF_8)
                }
            }
        }
    }

    suspend fun deleteDraft(id: String) {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                val drafts = readDrafts().toMutableList()
                val draft = drafts.firstOrNull { it.id == id }
                drafts.removeAll { it.id == id }
                writeDrafts(drafts)

                draft?.let {
                    File(filesDir, it.localFileName).delete()
                }
            }
        }
    }

    fun getLocalFile(draft: WorkflowDraft): File {
        return File(filesDir, draft.localFileName)
    }

    private fun readDrafts(): List<WorkflowDraft> {
        if (!draftsFile.exists()) return emptyList()
        return try {
            val content = draftsFile.readText(Charsets.UTF_8)
            if (content.isBlank()) return emptyList()
            val jsonArray = JSONArray(content)
            val drafts = mutableListOf<WorkflowDraft>()
            for (i in 0 until jsonArray.length()) {
                drafts.add(jsonToDraft(jsonArray.getJSONObject(i)))
            }
            drafts.sortedByDescending { it.updatedAt }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun writeDrafts(drafts: List<WorkflowDraft>) {
        try {
            val jsonArray = JSONArray()
            drafts.forEach { jsonArray.put(draftToJson(it)) }
            val tempFile = File(baseDir, "drafts.json.tmp")
            tempFile.writeText(jsonArray.toString(2), Charsets.UTF_8)
            if (!tempFile.renameTo(draftsFile)) {
                tempFile.copyTo(draftsFile, overwrite = true)
                tempFile.delete()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
        }
    }

    private fun draftToJson(draft: WorkflowDraft): JSONObject {
        val json = JSONObject()
        json.put("id", draft.id)
        json.put("remotePath", draft.remotePath)
        json.put("fileName", draft.fileName)
        json.put("localFileName", draft.localFileName)
        json.put("detectedFormat", draft.detectedFormat.name)
        json.put("originalContent", draft.originalContent)
        draft.modifiedContent?.let { json.put("modifiedContent", it) }
        draft.metadata?.let { meta ->
            val metaJson = JSONObject()
            meta.uid?.let { metaJson.put("uid", it) }
            meta.gid?.let { metaJson.put("gid", it) }
            meta.mode?.let { metaJson.put("mode", it) }
            meta.seContext?.let { metaJson.put("seContext", it) }
            json.put("metadata", metaJson)
        }
        json.put("createdAt", draft.createdAt)
        json.put("updatedAt", draft.updatedAt)
        return json
    }

    private fun jsonToDraft(json: JSONObject): WorkflowDraft {
        val metaJson = json.optJSONObject("metadata")
        return WorkflowDraft(
            id = json.getString("id"),
            remotePath = json.getString("remotePath"),
            fileName = json.getString("fileName"),
            localFileName = json.getString("localFileName"),
            detectedFormat = try {
                DetectedFormat.valueOf(json.optString("detectedFormat", "UNKNOWN"))
            } catch (_: Exception) {
                DetectedFormat.UNKNOWN
            },
            originalContent = json.optString("originalContent", ""),
            modifiedContent = if (json.has("modifiedContent")) {
                json.getString("modifiedContent")
            } else null,
            metadata = metaJson?.let {
                com.basitce.gfx.core.core_engine.FileMetadata(
                    uid = if (it.has("uid")) it.getInt("uid") else null,
                    gid = if (it.has("gid")) it.getInt("gid") else null,
                    mode = if (it.has("mode")) it.getString("mode") else null,
                    seContext = if (it.has("seContext")) it.getString("seContext") else null
                )
            },
            createdAt = json.optLong("createdAt", 0),
            updatedAt = json.optLong("updatedAt", 0)
        )
    }
}
