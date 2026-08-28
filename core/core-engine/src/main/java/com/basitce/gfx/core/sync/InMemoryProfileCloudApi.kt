package com.basitce.gfx.core.sync

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Geliştirme ve test için in-memory cloud simulator.
 *
 * Production'da Firebase/Supabase/REST implementasyonu ile değiştirilebilir.
 */
@Singleton
class InMemoryProfileCloudApi @Inject constructor() : ProfileCloudApi {

    private val mutex = Mutex()
    private val storage = mutableMapOf<String, ProfileCloudDto>()

    override suspend fun listProfiles(): List<ProfileCloudDto> {
        return mutex.withLock {
            storage.values.sortedByDescending { it.updatedAt }
        }
    }

    override suspend fun fetchProfile(profileId: String): ProfileCloudDto? {
        return mutex.withLock {
            storage[profileId]
        }
    }

    override suspend fun publishProfile(dto: ProfileCloudDto): ProfileCloudDto {
        return mutex.withLock {
            val existing = storage[dto.id]

            val newVersion = (existing?.version ?: 0L) + 1L

            val updated = dto.copy(
                version = newVersion,
                updatedAt = System.currentTimeMillis()
            )

            storage[updated.id] = updated

            updated
        }
    }

    override suspend fun deleteProfile(profileId: String) {
        mutex.withLock {
            storage.remove(profileId)
        }
    }
}
