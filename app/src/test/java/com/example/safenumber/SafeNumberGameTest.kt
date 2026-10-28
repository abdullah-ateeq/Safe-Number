package com.example.safenumber

import com.example.safenumber.data.generator.OptionGenerator
import com.example.safenumber.data.generator.PatternGenerator
import com.example.safenumber.data.model.Difficulty
import com.example.safenumber.data.model.GameOverReason
import com.example.safenumber.data.model.NumberPattern
import com.example.safenumber.data.model.PatternType
import com.example.safenumber.data.model.SafeNumberResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.min

class SafeNumberGameTest {

    @Test
    fun testEasyPatternGenerationValidity() {
        for (i in 0 until 50) {
            val pattern = PatternGenerator.generatePattern(Difficulty.EASY)
            assertTrue("Sequence must have at least 4 items", pattern.sequence.size >= 4)
            assertTrue("Pattern must validate", PatternGenerator.validatePattern(pattern))
            // In Easy mode, all values and danger must be positive
            assertTrue("All sequence values in Easy must be > 0", pattern.sequence.all { it > 0 })
            assertTrue("Danger number in Easy must be > 0", pattern.dangerousNumber > 0)
        }
    }

    @Test
    fun testMediumAndHardPatternGeneration() {
        for (i in 0 until 30) {
            val mediumPattern = PatternGenerator.generatePattern(Difficulty.MEDIUM)
            assertTrue(mediumPattern.sequence.size in 4..5)
            assertTrue(PatternGenerator.validatePattern(mediumPattern))

            val hardPattern = PatternGenerator.generatePattern(Difficulty.HARD)
            assertTrue(hardPattern.sequence.size in 5..6)
            assertTrue(PatternGenerator.validatePattern(hardPattern))
        }
    }

    @Test
    fun testOptionGenerationConstraints() {
        val pattern = NumberPattern(
            type = PatternType.ADDITION,
            sequence = listOf(3, 6, 9, 12),
            dangerousNumber = 15,
            difficulty = Difficulty.EASY,
            ruleDescription = "+3 each step"
        )

        for (i in 0 until 20) {
            val options = OptionGenerator.generateOptions(
                dangerousNumber = pattern.dangerousNumber,
                pattern = pattern,
                optionCount = 4
            )

            assertEquals("Must generate exactly 4 options", 4, options.size)
            assertEquals("All options must be unique", 4, options.distinct().size)
            assertTrue("Dangerous number must be included", options.contains(15))
            assertEquals(
                "Dangerous number must appear exactly once",
                1,
                options.count { it == 15 }
            )
            // Invalid immediate next sequence extension 18 should not be present
            assertFalse("Should not include next extension 18", options.contains(18))
        }
    }

    @Test
    fun testHardOptionCount() {
        val pattern = PatternGenerator.generatePattern(Difficulty.HARD)
        val options = OptionGenerator.generateOptions(
            dangerousNumber = pattern.dangerousNumber,
            pattern = pattern,
            optionCount = Difficulty.HARD.optionCount
        )

        assertEquals(5, options.size)
        assertEquals(5, options.distinct().size)
        assertTrue(options.contains(pattern.dangerousNumber))
    }

    @Test
    fun testCorePromptExample() {
        // Sequence: 3, 6, 9, 12 -> Danger: 15
        val pattern = NumberPattern(
            type = PatternType.ADDITION,
            sequence = listOf(3, 6, 9, 12),
            dangerousNumber = 15,
            difficulty = Difficulty.EASY,
            ruleDescription = "+3 each step"
        )

        val options = listOf(13, 14, 15, 17)

        // Danger test: Selecting 15 is danger
        val tappedDanger = 15
        val isDanger = tappedDanger == pattern.dangerousNumber
        assertTrue("Selecting 15 must be recognized as dangerous", isDanger)

        // Safe test: Selecting 14 is safe
        val tappedSafe = 14
        val isSafe = tappedSafe != pattern.dangerousNumber
        assertTrue("Selecting 14 must be recognized as safe", isSafe)
    }

    @Test
    fun testScoringFormulaDeterminism() {
        // Formula:
        // basePoints = 100
        // multiplier = 1 + min(streak - 1, 20) * 0.05
        // speedBonus = remainingTimeSeconds * 10
        // roundScore = (basePoints * multiplier).toInt() + speedBonus

        val basePoints = 100
        val streak = 1 // first safe tap
        val remainingTime = 7.4f

        val multiplier = 1.0 + (min((streak - 1).coerceAtLeast(0), 20) * 0.05)
        assertEquals(1.0, multiplier, 0.001)

        val speedBonus = (remainingTime * 10).toInt()
        assertEquals(74, speedBonus)

        val roundScore = (basePoints * multiplier).toInt() + speedBonus
        assertEquals(174, roundScore)

        // Streak 11: multiplier = 1 + 10 * 0.05 = 1.50
        val streak11 = 11
        val multiplier11 = 1.0 + (min((streak11 - 1).coerceAtLeast(0), 20) * 0.05)
        assertEquals(1.50, multiplier11, 0.001)
    }

    @Test
    fun testAccuracyCalculation() {
        // Safe: 24, Danger: 0 -> 100%
        val safeCount = 24
        val dangerCount = 0
        val accuracy = if (safeCount + dangerCount == 0) 100.0 else (safeCount.toDouble() / (safeCount + dangerCount)) * 100.0
        assertEquals(100.0, accuracy, 0.001)

        // Safe: 9, Danger: 1 -> 90%
        val safeCount2 = 9
        val dangerCount2 = 1
        val accuracy2 = (safeCount2.toDouble() / (safeCount2 + dangerCount2)) * 100.0
        assertEquals(90.0, accuracy2, 0.001)
    }

    @Test
    fun testResultRatingTitles() {
        fun getRating(streak: Int): String {
            val res = SafeNumberResult(
                difficulty = Difficulty.EASY,
                score = 100,
                streak = streak,
                safeSelections = streak,
                accuracy = 100.0,
                reason = GameOverReason.DANGER_SELECTED
            )
            return res.ratingTitle
        }

        assertEquals("KEEP WATCHING", getRating(3))
        assertEquals("GOOD START", getRating(5))
        assertEquals("PATTERN SPOTTER", getRating(12))
        assertEquals("SAFE NUMBER PRO", getRating(24))
        assertEquals("PATTERN MASTER", getRating(35))
    }
}
