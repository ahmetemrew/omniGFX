package com.basitce.gfx.core.sync

interface ProfileCloudApi {

    suspend fun listProfiles(): List<ProfileCloudDto>

    suspend fun fetchProfile(profileId: String): ProfileCloudDto?

    suspend fun publishProfile(dto: ProfileCloudDto): ProfileCloudDto

    suspend fun deleteProfile(profileId: String)
}
