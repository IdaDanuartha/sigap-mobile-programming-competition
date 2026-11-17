package id.sigap.app.domain.detection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RedactorTest {
    private val redactor = Redactor()

    @Test
    fun redactsPhoneEmailAndAccountNumbers() {
        val out = redactor.redact("Hubungi 0812-3456-7890 atau a.b@mail.com, rek 1234567890123")
        assertTrue(out.contains(Redactor.TOKEN_PHONE))
        assertTrue(out.contains(Redactor.TOKEN_EMAIL))
        assertTrue(out.contains(Redactor.TOKEN_NUMBER))
        assertFalse(out.contains("3456"))
        assertFalse(out.contains("mail.com"))
    }

    @Test
    fun redactsInternationalPhone() {
        assertTrue(redactor.redact("Call +62 812 3456 7890 now").contains(Redactor.TOKEN_PHONE))
    }

    @Test
    fun redactsHonorificNames() {
        assertEquals("Halo [NAME], paket Anda tiba", redactor.redact("Halo Pak Budi, paket Anda tiba"))
    }

    @Test
    fun keepsLinksForHighlighting() {
        assertTrue(redactor.redact("Buka https://bit.ly/abc").contains("https://bit.ly/abc"))
    }

    @Test
    fun snippetIsTruncatedToLimit() {
        val snippet = redactor.snippet("kata ".repeat(100))
        assertEquals(DetectionConfig.SNIPPET_MAX_CHARS, snippet.length)
        assertTrue(snippet.endsWith("\u2026"))
    }

    @Test
    fun shortTextIsUnchangedBesidesWhitespace() {
        assertEquals("a b", redactor.snippet("a   \n b"))
    }
}
