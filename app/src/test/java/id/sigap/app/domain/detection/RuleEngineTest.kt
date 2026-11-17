package id.sigap.app.domain.detection

import id.sigap.app.domain.model.MessageInput
import id.sigap.app.domain.model.ReasonCode
import id.sigap.app.domain.model.RiskLevel
import id.sigap.app.domain.model.ScamCategory
import id.sigap.app.domain.model.SenderKind
import id.sigap.app.domain.model.Sensitivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleEngineTest {
    private val engine = RuleEngine()

    private fun msg(text: String, kind: SenderKind = SenderKind.UNKNOWN) =
        MessageInput("com.whatsapp", "+62 812 0000 0000", text, 0L, kind)

    @Test
    fun apkInvitationFromUnknownSenderIsDanger() {
        val v = engine.analyze(msg("Undangan Pernikahan.apk"))
        assertEquals(RiskLevel.DANGER, v.level)
        assertEquals(ReasonCode.APK_FILE, v.reasons.first())
        assertEquals(ScamCategory.FAKE_INVITATION, v.category)
        assertTrue(v.hardDanger)
    }

    @Test
    fun credentialRequestWithUrgencyIsHardDanger() {
        val v = engine.analyze(msg("Segera kirim kode OTP Anda, akun akan diblokir hari ini"))
        assertEquals(RiskLevel.DANGER, v.level)
        assertTrue(v.hardDanger)
    }

    @Test
    fun legitimateOtpIsSafe() {
        val v = engine.analyze(msg("Kode OTP Anda 482913. JANGAN BAGIKAN kode ini ke siapa pun.", SenderKind.SHORTCODE))
        assertEquals(RiskLevel.SAFE, v.level)
    }

    @Test
    fun otpWithLinkIsNotTreatedAsLegitimate() {
        val v = engine.analyze(msg("Kode OTP Anda 482913. Jangan bagikan. Klik http://bca-login.xyz/verif"))
        assertTrue(v.level != RiskLevel.SAFE)
    }

    @Test
    fun lookalikeBankDomainWithRiskyTldIsFlagged() {
        val v = engine.analyze(msg("Akun bank Anda bermasalah, cek di http://bca-login.xyz sekarang"))
        assertTrue(ReasonCode.LOOKALIKE_DOMAIN in v.reasons)
        assertTrue(ReasonCode.RISKY_TLD in v.reasons)
        assertEquals(RiskLevel.DANGER, v.level)
    }

    @Test
    fun officialBankDomainIsNotLookalike() {
        val v = engine.analyze(msg("Info terbaru di https://www.bca.co.id"))
        assertTrue(ReasonCode.LOOKALIKE_DOMAIN !in v.reasons)
    }

    @Test
    fun ipAddressLinkIsFlagged() {
        val v = engine.analyze(msg("Lihat foto paket kamu http://103.45.67.8/resi"))
        assertTrue(ReasonCode.IP_LINK in v.reasons)
        assertEquals(ScamCategory.FAKE_COURIER, v.category)
    }

    @Test
    fun shortenerIsFlagged() {
        val v = engine.analyze(msg("Cek ini https://bit.ly/3abcde"))
        assertTrue(ReasonCode.SHORTENED_LINK in v.reasons)
    }

    @Test
    fun ordinaryChatIsSafe() {
        val v = engine.analyze(msg("Halo, besok jadi ketemu jam 10 ya?"))
        assertEquals(RiskLevel.SAFE, v.level)
        assertTrue(v.reasons.isEmpty())
    }

    @Test
    fun sensitivityShiftsThresholds() {
        val text = "Selamat anda terpilih mendapat hadiah, hubungi kami"
        val balanced = engine.analyze(msg(text), Sensitivity.BALANCED)
        val high = engine.analyze(msg(text), Sensitivity.HIGH)
        assertEquals(balanced.score, high.score)
        assertTrue(high.level.ordinal >= balanced.level.ordinal)
    }

    @Test
    fun fileNameIsNotParsedAsUrl() {
        assertTrue(UrlAnalyzer().extract("Lihat Surat Tilang.apk dan foto.jpg").isEmpty())
    }

    @Test
    fun registrableDomainHandlesCoId() {
        val a = UrlAnalyzer()
        assertEquals("bca.co.id", a.registrableDomain("www.bca.co.id"))
        assertEquals("example.com", a.registrableDomain("a.b.example.com"))
    }
}
