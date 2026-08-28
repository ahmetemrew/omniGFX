package com.basitce.gfx.core.core_engine.profile

import com.basitce.gfx.core.core_engine.verification.VerifyResult

sealed class ProfileEngineEvent {

    object Started : ProfileEngineEvent()

    object Validating : ProfileEngineEvent()

    object ResolvingPaths : ProfileEngineEvent()

    object SecurityCheck : ProfileEngineEvent()

    object CapabilityCheck : ProfileEngineEvent()

    object PathProbing : ProfileEngineEvent()

    object Preparing : ProfileEngineEvent()

    object ForceStopping : ProfileEngineEvent()

    object CreatingRemoteBackup : ProfileEngineEvent()

    object PullingFile : ProfileEngineEvent()

    object CreatingLocalBackup : ProfileEngineEvent()

    object ParsingConfig : ProfileEngineEvent()

    object ApplyingPatches : ProfileEngineEvent()

    object SerializingConfig : ProfileEngineEvent()

    object PushingFile : ProfileEngineEvent()

    object RestoringMetadata : ProfileEngineEvent()

    object Verifying : ProfileEngineEvent()

    object RollingBack : ProfileEngineEvent()

    object CleaningBackups : ProfileEngineEvent()

    object LaunchingGame : ProfileEngineEvent()

    data class Log(
        val message: String
    ) : ProfileEngineEvent()

    data class Completed(
        val result: ProfileApplyResult
    ) : ProfileEngineEvent()
}
