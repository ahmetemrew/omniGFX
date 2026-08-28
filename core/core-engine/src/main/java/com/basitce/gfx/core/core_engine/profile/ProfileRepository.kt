package com.basitce.gfx.core.core_engine.profile

import android.content.Context
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
 * Basit, güvenli, JSON dosya tabanlı repository.
 *
 * Profiller app-private dizinde saklanır:
 * files/omnigfx/profiles.json
 */
@Singleton
class FileProfileRepository @Inject constructor(
    @ApplicationContext context: Context
) : ProfileRepository {

    private val mutex = Mutex()

    private val profilesFile = File(
        context.filesDir,
        "omnigfx/profiles.json"
    )

    override suspend fun getAllProfiles(): List<UserConfigProfile> {
        return mutex.withLock {
            withContext(Dispatchers.IO) {
                readProfiles()
            }
        }
    }

    override suspend fun getProfileById(id: String): UserConfigProfile? {
        return mutex.withLock {
            withContext(Dispatchers.IO) {
                readProfiles().firstOrNull { it.id == id }
            }
        }
    }

    override suspend fun saveProfile(profile: UserConfigProfile) {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                val profiles = readProfiles().toMutableList()

                profiles.removeAll { it.id == profile.id }
                profiles.add(profile)

                writeProfiles(profiles)
            }
        }
    }

    override suspend fun deleteProfile(id: String) {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                val profiles = readProfiles().toMutableList()
                profiles.removeAll { it.id == id }
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
            options = options
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
