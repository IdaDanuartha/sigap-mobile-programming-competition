package id.sigap.app.domain.emergency

import id.sigap.app.domain.model.ScamCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EmergencyTest {
    private val checklist = BuildChecklistUseCase()
    private val report = BuildReportDraftUseCase()

    @Test
    fun installedAppChecklistIsOrderedAndEndsWithReport() {
        val steps = checklist(setOf(Situation.INSTALLED_APP))
        assertEquals(EmergencyStep.AIRPLANE_MODE, steps.first())
        assertEquals(EmergencyStep.REPORT_IASC, steps.last())
        assertTrue(steps.indexOf(EmergencyStep.UNINSTALL_APP) < steps.indexOf(EmergencyStep.CONTACT_BANK))
    }

    @Test
    fun combinedSituationsAreDeduplicated() {
        val steps = checklist(setOf(Situation.INSTALLED_APP, Situation.SHARED_CREDENTIAL, Situation.TRANSFERRED_MONEY))
        assertEquals(steps.size, steps.toSet().size)
        assertEquals(1, steps.count { it == EmergencyStep.CONTACT_BANK })
    }

    @Test
    fun emptySelectionStillOffersReporting() {
        assertEquals(listOf(EmergencyStep.REPORT_IASC), checklist(emptySet()))
    }

    @Test
    fun categoryPreselectsSituation() {
        assertEquals(Situation.INSTALLED_APP, situationFor(ScamCategory.APK_MALWARE))
        assertEquals(Situation.SHARED_CREDENTIAL, situationFor(ScamCategory.OTP_THEFT))
        assertEquals(Situation.OPENED_LINK, situationFor(null))
    }

    @Test
    fun reportDraftIncludesFilledFieldsOnly() {
        val text = report(
            ReportFields(
                dateTime = "11 November 2026 19.30",
                scammerContact = "0812 3456 7890",
                amountLost = "500.000",
                description = "Saya diminta mengunduh file undangan.",
                evidence = setOf(Evidence.SCREENSHOTS),
            ),
        )
        assertTrue(text.contains("Nomor/rekening pelaku: 0812 3456 7890"))
        assertTrue(text.contains("Jumlah kerugian: Rp 500.000"))
        assertTrue(text.contains("Bukti yang tersedia: tangkapan layar"))
        assertTrue(!text.contains("Referensi transaksi"))
    }

    @Test
    fun chronologyWorksWithoutOptionalFields() {
        val text = report(ReportFields())
        assertTrue(text.contains("saya menerima pesan melalui WhatsApp."))
    }
}
