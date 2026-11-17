package id.sigap.app.domain.emergency

/** Channel the scam arrived through. */
enum class ReportChannel(val label: String) {
    WHATSAPP("WhatsApp"), SMS("SMS"), TELEGRAM("Telegram"), OTHER("Lainnya"),
}

/** Evidence the user says they have. */
enum class Evidence(val label: String) {
    SCREENSHOTS("tangkapan layar"), TRANSFER_RECEIPT("bukti transfer"), CHAT_EXPORT("ekspor chat"),
}

/** Inputs of the report form (S07c). Blank values are left out of the draft. */
data class ReportFields(
    val dateTime: String = "",
    val channel: ReportChannel = ReportChannel.WHATSAPP,
    val scammerContact: String = "",
    val amountLost: String = "",
    val transactionRef: String = "",
    val description: String = "",
    val evidence: Set<Evidence> = emptySet(),
)

/** Builds a structured Indonesian report text for the IASC form (the service is Indonesian). */
class BuildReportDraftUseCase {
    operator fun invoke(fields: ReportFields): String = buildString {
        appendLine("DRAF LAPORAN PENIPUAN")
        appendLine()
        line("Waktu kejadian", fields.dateTime)
        line("Saluran", fields.channel.label)
        line("Nomor/rekening pelaku", fields.scammerContact)
        line("Jumlah kerugian", fields.amountLost.takeIf { it.isNotBlank() }?.let { "Rp $it" }.orEmpty())
        line("Referensi transaksi", fields.transactionRef)
        appendLine()
        appendLine("Kronologi:")
        appendLine(chronology(fields))
        if (fields.evidence.isNotEmpty()) {
            appendLine()
            appendLine("Bukti yang tersedia: " + fields.evidence.joinToString(", ") { it.label })
        }
    }.trimEnd()

    private fun StringBuilder.line(label: String, value: String) {
        if (value.isNotBlank()) appendLine("$label: ${value.trim()}")
    }

    private fun chronology(f: ReportFields): String {
        val whenPart = f.dateTime.takeIf { it.isNotBlank() }?.let { "Pada $it, " }.orEmpty()
        val fromPart = f.scammerContact.takeIf { it.isNotBlank() }?.let { " dari $it" }.orEmpty()
        val intro = "${whenPart}saya menerima pesan melalui ${f.channel.label}$fromPart."
        return if (f.description.isBlank()) intro else "$intro ${f.description.trim()}"
    }
}
