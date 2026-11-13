package com.basitce.gfx.core.core_engine.profile

import android.content.Context
import androidx.room.withTransaction
import com.basitce.gfx.core.core_database.AppDatabase
import com.basitce.gfx.core.core_database.entity.GameEntity
import com.basitce.gfx.core.core_database.entity.SchemaEntity
import com.basitce.gfx.core.core_database.entity.ProfileEntity
import kotlinx.coroutines.flow.first
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

class ProfileRepositoryException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Kullanıcı tanımlı config profillerini saklayan repository.
 */
interface ProfileRepository {

    suspend fun getAllProfiles(): List<UserConfigProfile>

    suspend fun getProfileById(id: String): UserConfigProfile?

    suspend fun saveProfile(profile: UserConfigProfile)

    suspend fun deleteProfile(id: String)

    suspend fun exportProfileAsString(profile: UserConfigProfile): String

    suspend fun importProfileFromString(jsonString: String): UserConfigProfile
}

/**
 * Room-backed profile repository with compatibility for legacy JSON profiles.
 * The shared database powers the wizard, backup browser and JSON transfer.
 */
@Singleton
class FileProfileRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val database: AppDatabase
) : ProfileRepository {

    private val mutex = Mutex()

    private val profilesFile = File(
        context.filesDir,
        "omnigfx/profiles.json"
    )

    override suspend fun getAllProfiles(): List<UserConfigProfile> {
        return mutex.withLock {
            withContext(Dispatchers.IO) {
                val legacy = readProfiles()
                val room = database.profileDao().getAllProfilesWithSchema().first().map { item ->
                    val schema = database.schemaDao().getSchemaById(item.profile.schemaId)
                    val game = database.gameDao().getGameById(item.gameId)
                    val values = JSONObject(item.profile.userValuesJson)
                    UserConfigProfile(
                        id = item.profile.id, name = item.profile.name,
                        packageName = game?.packageName?.takeIf { it.isNotBlank() },
                        targetPathTemplate = item.profile.targetFilePath,
                        patches = values.keys().asSequence().map { key ->
                            val value = values.get(key)
                            ProfilePatch(key, if (value == JSONObject.NULL) null else value.toString(), when (value) {
                                is Number -> PatchValueType.NUMBER
                                is Boolean -> PatchValueType.BOOLEAN
                                JSONObject.NULL -> PatchValueType.NULL
                                else -> PatchValueType.STRING
                            })
                        }.toList(),
                        rawContent = item.profile.rawContent,
                        schemaJson = schema?.jsonSchema
                    )
                }
                (room + legacy).distinctBy { it.id }
            }
        }
    }

    override suspend fun getProfileById(id: String): UserConfigProfile? {
        return getAllProfiles().firstOrNull { it.id == id }
    }

    override suspend fun saveProfile(profile: UserConfigProfile) {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                val profiles = readProfiles().toMutableList()

                profiles.removeAll { it.id == profile.id }
                profiles.add(profile)

                val target = ProfilePathResolver().resolveCandidates(profile).firstOrNull() ?: error("Hedef yol çözümlenemedi")
                require(target.startsWith("/")) { "Geçerli hedef dosya yolu gerekli." }
                database.withTransaction {
                    val games = database.gameDao().getAllGames().first()
                    val packageName = profile.packageName.orEmpty()
                    val gameId = games.firstOrNull { it.packageName == packageName }?.id ?: UUID.randomUUID().toString()
                    if (games.none { it.id == gameId }) database.gameDao().insertGame(
                        GameEntity(gameId, packageName, packageName.ifBlank { "İçe aktarılan profiller" }, "", true, System.currentTimeMillis())
                    )
                    val schemaId = UUID.randomUUID().toString()
                    val schema = profile.schemaJson ?: JSONObject().put("targetFile", target)
                        .put("parser", profile.format.name).put("uiComponents", JSONArray()).toString()
                    database.schemaDao().insertSchema(SchemaEntity(schemaId, gameId, 2, schema))
                    val values = JSONObject()
                    profile.patches.forEach { patch ->
                        val value: Any = when (patch.valueType) {
                            PatchValueType.BOOLEAN -> patch.value.toBoolean()
                            PatchValueType.NUMBER -> patch.value?.toDoubleOrNull() ?: error("Geçersiz sayı")
                            PatchValueType.NULL -> JSONObject.NULL
                            else -> patch.value.orEmpty()
                        }
                        values.put(patch.path, value)
                    }
                    database.profileDao().insertProfile(ProfileEntity(profile.id, schemaId, profile.name,
                        values.toString(), profile.rawContent != null, profile.rawContent, target))
                }
                // Room is canonical; remove a migrated legacy entry to avoid resurrecting deleted profiles.
                writeProfiles(profiles.filterNot { it.id == profile.id })
            }
        }
    }

    override suspend fun deleteProfile(id: String) {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                val profiles = readProfiles().toMutableList()
                profiles.removeAll { it.id == id }
                database.profileDao().deleteProfile(id)
                writeProfiles(profiles)
            }
        }
    }

    override suspend fun exportProfileAsString(
        profile: UserConfigProfile
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                profile.toJson().toString(2)
            } catch (e: Exception) {
                throw ProfileRepositoryException("Profil export edilemedi.", e)
            }
        }
    }

    override suspend fun importProfileFromString(
        jsonString: String
    ): UserConfigProfile {
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject(jsonString)
                jsonToProfile(json)
            } catch (e: Exception) {
                throw ProfileRepositoryException("Profil import edilemedi.", e)
            }
        }
    }

    private fun readProfiles(): List<UserConfigProfile> {
        if (!profilesFile.exists()) return emptyList()

        return try {
            val content = profilesFile.readText(Charsets.UTF_8)

            if (content.isBlank()) return emptyList()

            val jsonArray = JSONArray(content)
            val profiles = mutableListOf<UserConfigProfile>()

            for (i in 0 until jsonArray.length()) {
                val profileJson = jsonArray.getJSONObject(i)
                profiles.add(jsonToProfile(profileJson))
            }

            profiles
        } catch (e: Exception) {
            throw ProfileRepositoryException("Profiller okunamadı.", e)
        }
    }

    private fun writeProfiles(profiles: List<UserConfigProfile>) {
        try {
            val jsonArray = JSONArray()

            profiles.forEach { profile ->
                jsonArray.put(profile.toJson())
            }

            profilesFile.parentFile?.mkdirs()

            val tempFile = File(
                profilesFile.parent,
                profilesFile.name + ".tmp"
            )

            tempFile.writeText(jsonArray.toString(2), Charsets.UTF_8)

            if (!tempFile.renameTo(profilesFile)) {
                tempFile.copyTo(profilesFile, overwrite = true)
                tempFile.delete()
            }
        } catch (e: Exception) {
            throw ProfileRepositoryException("Profiller yazılamadı.", e)
        }
    }

    private fun UserConfigProfile.toJson(): JSONObject {
        val json = JSONObject()

        json.put("id", id)
        json.put("name", name)
        rawContent?.let { json.put("rawContent", it) }
        schemaJson?.let { json.put("schemaJson", it) }

        packageName?.let {
            json.put("packageName", it)
        }

        json.put("targetPathTemplate", targetPathTemplate)
        json.put("format", format.name)
        json.put("options", options.toJson())

        val patchesArray = JSONArray()

        patches.forEach { patch ->
            patchesArray.put(patch.toJson())
        }

        json.put("patches", patchesArray)

        return json
    }

    private fun ProfilePatch.toJson(): JSONObject {
        val json = JSONObject()

        json.put("path", path)
        json.put("valueType", valueType.name)

        value?.let {
            json.put("value", it)
        }

        return json
    }

    private fun ProfileOptions.toJson(): JSONObject {
        val json = JSONObject()
    
        json.put("dryRun", dryRun)
        json.put("backupLocal", backupLocal)
        json.put("backupRemote", backupRemote)
        json.put("atomicReplace", atomicReplace)
        json.put("restoreSelinux", restoreSelinux)
        json.put("abortIfBackupFails", abortIfBackupFails)
        json.put("forceStopBeforeApply", forceStopBeforeApply)
        json.put("launchAfterApply", launchAfterApply)
        json.put("expertMode", expertMode)
        json.put("allowUnsafeSystemPaths", allowUnsafeSystemPaths)
    
        json.put("verifyAfterApply", verifyAfterApply)
        json.put("requireHashVerification", requireHashVerification)
        json.put("requireSizeVerification", requireSizeVerification)
        json.put("verifyMetadata", verifyMetadata)
        json.put("verifySelinux", verifySelinux)
    
        json.put("autoRollbackOnVerificationFailure", autoRollbackOnVerificationFailure)
        json.put("autoRollbackOnApplyFailure", autoRollbackOnApplyFailure)
    
        backupRetentionCount?.let {
            json.put("backupRetentionCount", it)
        }
    
        return json
    }

    private fun jsonToProfile(json: JSONObject): UserConfigProfile {
        val id = json.optString("id").ifBlank {
            UUID.randomUUID().toString()
        }

        val name = json.optString("name", "")

        val packageName = if (json.has("packageName") && !json.isNull("packageName")) {
            json.getString("packageName")
        } else {
            null
        }

        val targetPathTemplate = json.optString("targetPathTemplate", "")

        val format = runCatching {
            ConfigFormatHint.valueOf(json.optString("format", ConfigFormatHint.AUTO.name))
        }.getOrDefault(ConfigFormatHint.AUTO)

        val optionsJson = json.optJSONObject("options")
        val options = optionsJson.toProfileOptions()

        val patchesArray = json.optJSONArray("patches") ?: JSONArray()
        val patches = mutableListOf<ProfilePatch>()

        for (i in 0 until patchesArray.length()) {
            val patchJson = patchesArray.getJSONObject(i)
            patches.add(jsonToPatch(patchJson))
        }

        return UserConfigProfile(
            id = id,
            name = name,
            packageName = packageName,
            targetPathTemplate = targetPathTemplate,
            format = format,
            patches = patches,
            options = options,
            rawContent = if (json.has("rawContent") && !json.isNull("rawContent")) json.getString("rawContent") else null,
            schemaJson = if (json.has("schemaJson") && !json.isNull("schemaJson")) json.getString("schemaJson") else null
        )
    }

    private fun jsonToPatch(json: JSONObject): ProfilePatch {
        val path = json.optString("path", "")

        val value = if (json.has("value") && !json.isNull("value")) {
            json.getString("value")
        } else {
            null
        }

        val valueType = runCatching {
            PatchValueType.valueOf(json.optString("valueType", PatchValueType.STRING.name))
        }.getOrDefault(PatchValueType.STRING)

        return ProfilePatch(
            path = path,
            value = value,
            valueType = valueType
        )
    }

    private fun JSONObject?.toProfileOptions(): ProfileOptions {
        if (this == null) return ProfileOptions()
    
        val backupRetentionRaw = optInt("backupRetentionCount", 3)
    
        val backupRetentionCount = if (backupRetentionRaw <= 0) {
            null
        } else {
            backupRetentionRaw
        }
    
        return ProfileOptions(
            dryRun = optBoolean("dryRun", false),
            backupLocal = optBoolean("backupLocal", true),
            backupRemote = optBoolean("backupRemote", true),
            atomicReplace = optBoolean("atomicReplace", true),
            restoreSelinux = optBoolean("restoreSelinux", false),
            abortIfBackupFails = optBoolean("abortIfBackupFails", true),
            forceStopBeforeApply = optBoolean("forceStopBeforeApply", false),
            launchAfterApply = optBoolean("launchAfterApply", false),
            expertMode = optBoolean("expertMode", true),
            allowUnsafeSystemPaths = optBoolean("allowUnsafeSystemPaths", false),
    
            verifyAfterApply = optBoolean("verifyAfterApply", true),
            requireHashVerification = optBoolean("requireHashVerification", true),
            requireSizeVerification = optBoolean("requireSizeVerification", false),
            verifyMetadata = optBoolean("verifyMetadata", false),
            verifySelinux = optBoolean("verifySelinux", false),
    
            autoRollbackOnVerificationFailure = optBoolean(
                "autoRollbackOnVerificationFailure",
                true
            ),
            autoRollbackOnApplyFailure = optBoolean(
                "autoRollbackOnApplyFailure",
                false
            ),
    
            backupRetentionCount = backupRetentionCount
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: FileProfileRepository
    ): ProfileRepository
}
