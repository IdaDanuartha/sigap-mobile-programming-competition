package id.sigap.app.domain.usecase

import id.sigap.app.core.util.SenderLabels
import id.sigap.app.domain.detection.Redactor
import id.sigap.app.domain.detection.RuleEngine
import id.sigap.app.domain.model.AlertNotifier
import id.sigap.app.domain.model.MessageInput
import id.sigap.app.domain.model.RiskLevel
import id.sigap.app.domain.model.ScanHistoryRepository
import id.sigap.app.domain.model.ScanResult
import id.sigap.app.domain.model.Sensitivity

/** Runs the rule engine, stores a redacted result, and alerts the user when risk is not safe. */
class AnalyzeMessageUseCase(
    private val engine: RuleEngine,
    private val redactor: Redactor,
    private val history: ScanHistoryRepository,
    private val notifier: AlertNotifier,
    private val sensitivityProvider: () -> Sensitivity = { Sensitivity.BALANCED },
) {
    /** Set [notify] to false for manual checks, where the result is shown directly. */
    suspend operator fun invoke(
        input: MessageInput,
        sensitivity: Sensitivity = sensitivityProvider(),
        notify: Boolean = true,
    ): ScanResult {
        val verdict = engine.analyze(input, sensitivity)
        val senderKey = input.senderLabel.takeIf { it.isNotBlank() }?.let(SenderLabels::hash)
        val draft = ScanResult(
            id = 0,
            timestamp = input.timestamp,
            sourcePackage = input.sourcePackage,
            senderKind = input.senderKind,
            level = verdict.level,
            score = verdict.score,
            category = verdict.category,
            reasons = verdict.reasons,
            redactedSnippet = redactor.snippet(input.text),
            checkedOnDevice = true,
            senderKey = senderKey,
        )
        val saved = draft.copy(id = history.save(draft))
        if (notify && saved.level != RiskLevel.SAFE) notifier.notify(saved, senderKey.orEmpty())
        return saved
    }

    companion object {
        /** Source package used for results created by manual checks. */
        const val MANUAL_SOURCE = "manual"
    }
}

