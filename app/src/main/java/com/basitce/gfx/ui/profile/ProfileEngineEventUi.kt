package com.basitce.gfx.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.basitce.gfx.R
import com.basitce.gfx.core.core_engine.profile.ProfileEngineEvent

@Composable
fun ProfileEngineEvent.displayName(): String {
    return when (this) {
        is ProfileEngineEvent.Started -> stringResource(R.string.event_started)
        is ProfileEngineEvent.Validating -> stringResource(R.string.event_validating)
        is ProfileEngineEvent.ResolvingPaths -> stringResource(R.string.event_resolving_paths)
        is ProfileEngineEvent.SecurityCheck -> stringResource(R.string.event_security_check)
        is ProfileEngineEvent.CapabilityCheck -> stringResource(R.string.event_capability_check)
        is ProfileEngineEvent.PathProbing -> stringResource(R.string.event_path_probing)
        is ProfileEngineEvent.Preparing -> stringResource(R.string.event_preparing)
        is ProfileEngineEvent.ForceStopping -> stringResource(R.string.event_force_stopping)
        is ProfileEngineEvent.CreatingRemoteBackup -> stringResource(R.string.event_creating_remote_backup)
        is ProfileEngineEvent.PullingFile -> stringResource(R.string.event_pulling_file)
        is ProfileEngineEvent.CreatingLocalBackup -> stringResource(R.string.event_creating_local_backup)
        is ProfileEngineEvent.ParsingConfig -> stringResource(R.string.event_parsing_config)
        is ProfileEngineEvent.ApplyingPatches -> stringResource(R.string.event_applying_patches)
        is ProfileEngineEvent.SerializingConfig -> stringResource(R.string.event_serializing_config)
        is ProfileEngineEvent.PushingFile -> stringResource(R.string.event_pushing_file)
        is ProfileEngineEvent.RestoringMetadata -> stringResource(R.string.event_restoring_metadata)
        is ProfileEngineEvent.Verifying -> stringResource(R.string.event_verifying)
        is ProfileEngineEvent.RollingBack -> stringResource(R.string.event_rolling_back)
        is ProfileEngineEvent.CleaningBackups -> stringResource(R.string.event_cleaning_backups)
        is ProfileEngineEvent.LaunchingGame -> stringResource(R.string.event_launching_game)
        is ProfileEngineEvent.Log -> message
        is ProfileEngineEvent.Completed -> stringResource(R.string.event_completed)
    }
}
