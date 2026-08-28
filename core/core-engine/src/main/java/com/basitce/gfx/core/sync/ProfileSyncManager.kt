package com.basitce.gfx.core.sync

import com.basitce.gfx.core.core_engine.profile.ProfileRepository
import com.basitce.gfx.core.core_engine.profile.UserConfigProfile
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local profile ile cloud profile arasında sync orchestration.
 *
 * Draft:
 * - publishLocalProfile: local profili cloud'a push eder
 * - importRemoteProfile: cloud profili local'e import eder
 *
 * Gelecek:
 * - version tracking
 * - conflict resolution
 * - delta sync
 * - profile ownership
 */
@Singleton
class ProfileSyncManager @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val profileCloudApi: ProfileCloudApi
) {

    suspend fun publishLocalProfile(profile: UserConfigProfile): ProfileCloudDto {
        val json = profileRepository.exportProfileAsString(profile)

        val dto = ProfileCloudDto(
            id = profile.id,
            name = profile.name,
            version = 0L,
            updatedAt = System.currentTimeMillis(),
            payloadJson = json
        )

        return profileCloudApi.publishProfile(dto)
    }

    suspend fun publishLocalProfileById(profileId: String): ProfileCloudDto? {
        val profile = profileRepository.getProfileById(profileId) ?: return null
        return publishLocalProfile(profile)
    }

    suspend fun importRemoteProfile(remoteProfileId: String): UserConfigProfile? {
        val dto = profileCloudApi.fetchProfile(remoteProfileId) ?: return null

        val importedProfile = profileRepository.importProfileFromString(dto.payloadJson)

        val profileToSave = importedProfile.copy(
            id = dto.id
        )

        profileRepository.saveProfile(profileToSave)

        return profileToSave
    }

    suspend fun refreshRemoteProfiles(): List<ProfileCloudDto> {
        return profileCloudApi.listProfiles()
    }

    suspend fun deleteRemoteProfile(remoteProfileId: String) {
        profileCloudApi.deleteProfile(remoteProfileId)
    }
}
