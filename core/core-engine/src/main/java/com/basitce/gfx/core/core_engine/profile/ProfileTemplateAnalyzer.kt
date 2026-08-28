package com.basitce.gfx.core.core_engine.profile

import javax.inject.Inject
import javax.inject.Singleton

enum class ProfileRiskLevel {
    SAFE,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class FindingSeverity {
    INFO,
    WARNING,
    HIGH,
    CRITICAL
}

data class ProfileSecurityFinding(
    val severity: FindingSeverity,
    val message: String
)

data class ProfileTemplateAnalysis(
    val score: Int,
    val riskLevel: ProfileRiskLevel,
    val findings: List<ProfileSecurityFinding>,
    val canImport: Boolean
)

/**
 * Import edilen profil template'lerini güvenlik ve kalite açısından analiz eder.
 */
@Singleton
class ProfileTemplateAnalyzer @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val pathResolver: ProfilePathResolver,
    private val securityScanner: ProfileSecurityScanner
) {

    suspend fun analyzeJson(json: String): ProfileTemplateAnalysis {
        val profile = profileRepository.importProfileFromString(json)
        return analyzeProfile(profile)
    }

    suspend fun analyzeProfile(
        profile: UserConfigProfile
    ): ProfileTemplateAnalysis {
        val findings = mutableListOf<ProfileSecurityFinding>()

        val candidates = pathResolver.resolveCandidates(profile)

        val securityReport = securityScanner.scan(
            profile = profile,
            candidatePaths = candidates
        )

        securityReport.errors.forEach { error ->
            findings.add(
                ProfileSecurityFinding(
                    severity = FindingSeverity.CRITICAL,
                    message = error
                )
            )
        }

        securityReport.warnings.forEach { warning ->
            findings.add(
                ProfileSecurityFinding(
                    severity = FindingSeverity.WARNING,
                    message = warning
                )
            )
        }

        if (profile.options.allowUnsafeSystemPaths) {
            findings.add(
                ProfileSecurityFinding(
                    severity = FindingSeverity.HIGH,
                    message = "Profil unsafe system path'lere izin veriyor."
                )
            )
        }

        if (profile.packageName.isNullOrBlank()) {
            findings.add(
                ProfileSecurityFinding(
                    severity = FindingSeverity.INFO,
                    message = "Profilde package name tanımlı değil."
                )
            )
        }

        if (profile.patches.size > 100) {
            findings.add(
                ProfileSecurityFinding(
                    severity = FindingSeverity.WARNING,
                    message = "Profil çok fazla patch içeriyor: ${profile.patches.size}"
                )
            )
        } else if (profile.patches.size > 30) {
            findings.add(
                ProfileSecurityFinding(
                    severity = FindingSeverity.INFO,
                    message = "Profil geniş patch listesi içeriyor: ${profile.patches.size}"
                )
            )
        }

        if (profile.targetPathTemplate.contains("/system/")) {
            findings.add(
                ProfileSecurityFinding(
                    severity = FindingSeverity.CRITICAL,
                    message = "Target path template /system içeriyor."
                )
            )
        }

        if (profile.targetPathTemplate.contains("/data/system")) {
            findings.add(
                ProfileSecurityFinding(
                    severity = FindingSeverity.CRITICAL,
                    message = "Target path template /data/system içeriyor."
                )
            )
        }

        var score = 100

        findings.forEach { finding ->
            score -= when (finding.severity) {
                FindingSeverity.INFO -> 2
                FindingSeverity.WARNING -> 5
                FindingSeverity.HIGH -> 15
                FindingSeverity.CRITICAL -> 30
            }
        }

        score = score.coerceIn(0, 100)

        val hasCritical = findings.any {
            it.severity == FindingSeverity.CRITICAL
        }

        val riskLevel = when {
            hasCritical || score < 30 -> ProfileRiskLevel.CRITICAL
            score < 50 -> ProfileRiskLevel.HIGH
            score < 70 -> ProfileRiskLevel.MEDIUM
            score < 90 -> ProfileRiskLevel.LOW
            else -> ProfileRiskLevel.SAFE
        }

        val canImport = securityReport.allowed && !hasCritical

        return ProfileTemplateAnalysis(
            score = score,
            riskLevel = riskLevel,
            findings = findings,
            canImport = canImport
        )
    }
}
