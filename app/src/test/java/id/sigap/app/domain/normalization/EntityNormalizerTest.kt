package id.sigap.app.domain.normalization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class EntityNormalizerTest {
    private val normalizer = EntityNormalizer()

    @Test
    fun normalizesLocalIndonesianPhoneToE164AndMasksDisplay() {
        val entity = normalizer.normalize(EntityType.PHONE, "0812-3456-7890")
        assertNotNull(entity)
        assertEquals("+6281234567890", entity!!.canonical)
        assertEquals("+62 812 **** 7890", entity.maskedDisplay)
    }

    @Test
    fun normalizesInternationalPhoneToSameHashAsLocal() {
        val local = normalizer.normalize(EntityType.PHONE, "0812-3456-7890")!!
        val intl = normalizer.normalize(EntityType.PHONE, "+62 812 3456 7890")!!
        assertEquals(local.canonical, intl.canonical)
        assertEquals(local.sha256, intl.sha256)
    }

    @Test
    fun normalizesBankAccountToDigitsOnly() {
        val entity = normalizer.normalize(EntityType.BANK_ACCOUNT, "1234-5678-9012")
        assertNotNull(entity)
        assertEquals("123456789012", entity!!.canonical)
        assertEquals("123 **** 9012", entity.maskedDisplay)
    }

    @Test
    fun normalizesLinkToRegistrableDomain() {
        val entity = normalizer.normalize(EntityType.LINK, "https://login.bca-verif.xyz/masuk?id=1")
        assertNotNull(entity)
        assertEquals("bca-verif.xyz", entity!!.canonical)
        assertEquals("bca-verif.xyz", entity.maskedDisplay)
    }

    @Test
    fun rejectsShortOrInvalidInputs() {
        assertNull(normalizer.normalize(EntityType.PHONE, "123"))
        assertNull(normalizer.normalize(EntityType.BANK_ACCOUNT, "12"))
        assertNull(normalizer.normalize(EntityType.LINK, "notalink"))
    }
}
