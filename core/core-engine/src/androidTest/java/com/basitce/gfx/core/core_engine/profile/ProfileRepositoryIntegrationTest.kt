package com.basitce.gfx.core.core_engine.profile

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.basitce.gfx.core.core_database.AppDatabase
import com.basitce.gfx.core.core_database.entity.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileRepositoryIntegrationTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: FileProfileRepository

    @Before fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = FileProfileRepository(context, database)
    }
    @After fun tearDown() { database.close() }

    @Test fun wizardProfilesExportWithRawContentAndSchema() = runBlocking {
        database.gameDao().insertGame(GameEntity("g", "com.test.game", "Test", "", true, 1))
        database.schemaDao().insertSchema(SchemaEntity("s", "g", 2, "{\"parser\":\"INI\"}"))
        database.profileDao().insertProfile(ProfileEntity("p", "s", "Raw", "{}", true, "fps=60\n", "/sdcard/test.ini"))
        val profile = repository.getProfileById("p")!!
        val imported = repository.importProfileFromString(repository.exportProfileAsString(profile))
        assertEquals("fps=60\n", imported.rawContent)
        assertEquals("{\"parser\":\"INI\"}", imported.schemaJson)
        assertEquals("com.test.game", imported.packageName)
    }

    @Test fun importedProfilesAppearInMainDatabaseAndDeletePermanently() = runBlocking {
        val profile = UserConfigProfile(id = "imported", name = "Imported", packageName = "com.test.game",
            targetPathTemplate = "{{externalFilesDir}}/config.ini", patches = listOf(ProfilePatch("Graphics/fps", "90")))
        repository.saveProfile(profile)
        val stored = database.profileDao().getAllProfilesWithSchema().first().single()
        assertEquals("Imported", stored.profile.name)
        assertEquals("/sdcard/Android/data/com.test.game/files/config.ini", stored.profile.targetFilePath)
        repository.deleteProfile(profile.id)
        assertNull(repository.getProfileById(profile.id))
    }

    @Test fun patchTypesSurviveDatabaseAndJsonRoundTrip() = runBlocking {
        val profile = UserConfigProfile(id = "typed", name = "Typed", packageName = "com.test.game",
            targetPathTemplate = "/sdcard/config.json", patches = listOf(
                ProfilePatch("$.fps", "90", PatchValueType.NUMBER),
                ProfilePatch("$.enabled", "true", PatchValueType.BOOLEAN),
                ProfilePatch("$.code", "01", PatchValueType.STRING),
                ProfilePatch("$.optional", null, PatchValueType.NULL)))
        repository.saveProfile(profile)
        val result = repository.importProfileFromString(repository.exportProfileAsString(repository.getProfileById("typed")!!))
        assertEquals(profile.patches.associate { it.path to it.valueType }, result.patches.associate { it.path to it.valueType })
        assertEquals("01", result.patches.single { it.path == "$.code" }.value)
    }
}
