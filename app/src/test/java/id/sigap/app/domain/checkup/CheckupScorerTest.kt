package id.sigap.app.domain.checkup

import org.junit.Assert.assertEquals
import org.junit.Test

class CheckupScorerTest {
    private fun item(weight: Int, passed: Boolean) = CheckupItem("x", CheckKind.AUTO, weight, passed)

    @Test
    fun allPassedIsFullScore() {
        assertEquals(100, CheckupScorer.score(listOf(item(30, true), item(20, true))))
    }

    @Test
    fun scoreIsWeighted() {
        assertEquals(50, CheckupScorer.score(listOf(item(30, true), item(20, false), item(10, false))))
    }

    @Test
    fun emptyIsFullScore() {
        assertEquals(100, CheckupScorer.score(emptyList()))
    }

    @Test
    fun labelThreshold() {
        assertEquals(CheckupLabel.GOOD, CheckupScorer.label(80))
        assertEquals(CheckupLabel.NEEDS_WORK, CheckupScorer.label(79))
    }
}

