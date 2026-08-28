package com.basitce.gfx.core.core_database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.basitce.gfx.core.core_database.entity.SchemaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SchemaDao {

    @Query("SELECT * FROM schemas WHERE gameId = :gameId")
    fun getSchemasForGame(gameId: String): Flow<List<SchemaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchema(schema: SchemaEntity)
}
