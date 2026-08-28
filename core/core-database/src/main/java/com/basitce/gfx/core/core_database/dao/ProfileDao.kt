package com.basitce.gfx.core.core_database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.basitce.gfx.core.core_database.entity.ProfileEntity
import com.basitce.gfx.core.core_database.model.ProfileWithSchema
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE schemaId = :schemaId ORDER BY createdAt DESC")
    fun getProfilesForSchema(schemaId: String): Flow<List<ProfileEntity>>

    @Query("""
        SELECT p.*, s.gameId as gameId, g.name as gameName
        FROM profiles p
        INNER JOIN schemas s ON p.schemaId = s.id
        INNER JOIN games g ON s.gameId = g.id
        ORDER BY p.createdAt DESC
    """)
    fun getAllProfilesWithSchema(): Flow<List<ProfileWithSchema>>

    @Query("SELECT * FROM profiles WHERE id = :profileId")
    suspend fun getProfileById(profileId: String): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :profileId")
    suspend fun deleteProfile(profileId: String)
}
