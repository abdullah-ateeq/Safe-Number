package com.example.safenumber.data.generator

import com.example.safenumber.data.model.Difficulty
import com.example.safenumber.data.model.NumberPattern
import com.example.safenumber.data.model.PatternType
import kotlin.random.Random

object PatternGenerator {

    private val lastSequences = ArrayDeque<List<Int>>()
    private const val MAX_HISTORY = 10

    /**
     * Generates a validated pattern appropriate for the given difficulty.
     */
    fun generatePattern(
        difficulty: Difficulty,
        previousType: PatternType? = null
    ): NumberPattern {
        var attempts = 0
        while (attempts < 50) {
            attempts++
            val type = selectPatternType(difficulty, previousType)
            val pattern = createPatternForType(type, difficulty)
            if (validatePattern(pattern) && !lastSequences.contains(pattern.sequence)) {
                // Record history to prevent identical consecutive sequences
                if (lastSequences.size >= MAX_HISTORY) {
                    lastSequences.removeFirst()
                }
                lastSequences.addLast(pattern.sequence)
                return pattern
            }
        }
        // Fallback guaranteed valid pattern
        return fallbackPattern(difficulty)
    }

    private fun selectPatternType(
        difficulty: Difficulty,
        previousType: PatternType?
    ): PatternType {
        val allowedTypes = when (difficulty) {
            Difficulty.EASY -> listOf(
                PatternType.ADDITION,
                PatternType.ADDITION,
                PatternType.SUBTRACTION,
                PatternType.SUBTRACTION,
                PatternType.REPEATING_DIFFERENCE
            )
            Difficulty.MEDIUM -> listOf(
                PatternType.ADDITION,
                PatternType.SUBTRACTION,
                PatternType.MULTIPLICATION,
                PatternType.ALTERNATING,
                PatternType.INCREASING_DIFFERENCE,
                PatternType.REPEATING_DIFFERENCE
            )
            Difficulty.HARD -> listOf(
                PatternType.MULTIPLICATION,
                PatternType.ALTERNATING,
                PatternType.INCREASING_DIFFERENCE,
                PatternType.REPEATING_DIFFERENCE,
                PatternType.ALTERNATING
            )
        }

        // Avoid repeating the same pattern type directly if other types are available
        val filtered = allowedTypes.filter { it != previousType }
        val pool = if (filtered.isNotEmpty()) filtered else allowedTypes
        return pool.random()
    }

    private fun createPatternForType(
        type: PatternType,
        difficulty: Difficulty
    ): NumberPattern {
        val length = when (difficulty) {
            Difficulty.EASY -> 4
            Difficulty.MEDIUM -> if (Random.nextBoolean()) 4 else 5
            Difficulty.HARD -> if (Random.nextBoolean()) 5 else 6
        }

        return when (type) {
            PatternType.ADDITION -> generateAdditionPattern(length, difficulty)
            PatternType.SUBTRACTION -> generateSubtractionPattern(length, difficulty)
            PatternType.MULTIPLICATION -> generateMultiplicationPattern(length, difficulty)
            PatternType.ALTERNATING -> generateAlternatingPattern(length, difficulty)
            PatternType.INCREASING_DIFFERENCE -> generateIncreasingDiffPattern(length, difficulty)
            PatternType.REPEATING_DIFFERENCE -> generateRepeatingDiffPattern(length, difficulty)
        }
    }

    private fun generateAdditionPattern(length: Int, difficulty: Difficulty): NumberPattern {
        val possibleSteps = when (difficulty) {
            Difficulty.EASY -> listOf(2, 3, 4, 5, 6, 7, 10)
            Difficulty.MEDIUM -> listOf(3, 4, 6, 7, 8, 9, 11, 12, 15)
            Difficulty.HARD -> listOf(7, 8, 9, 12, 13, 14, 16, 17, 19, 25)
        }
        val step = possibleSteps.random()
        val start = when (difficulty) {
            Difficulty.EASY -> Random.nextInt(1, 20)
            Difficulty.MEDIUM -> Random.nextInt(5, 50)
            Difficulty.HARD -> Random.nextInt(10, 100)
        }

        val sequence = (0 until length).map { start + it * step }
        val danger = start + length * step
        val rule = "+$step each step"

        return NumberPattern(
            type = PatternType.ADDITION,
            sequence = sequence,
            dangerousNumber = danger,
            difficulty = difficulty,
            ruleDescription = rule
        )
    }

    private fun generateSubtractionPattern(length: Int, difficulty: Difficulty): NumberPattern {
        val possibleSteps = when (difficulty) {
            Difficulty.EASY -> listOf(2, 3, 4, 5, 6, 10)
            Difficulty.MEDIUM -> listOf(3, 4, 5, 6, 7, 8, 9, 12)
            Difficulty.HARD -> listOf(6, 7, 8, 9, 11, 13, 15, 18)
        }
        val step = possibleSteps.random()
        val minBuffer = step * (length + 1)
        val start = when (difficulty) {
            Difficulty.EASY -> minBuffer + Random.nextInt(2, 25)
            Difficulty.MEDIUM -> minBuffer + Random.nextInt(5, 50)
            Difficulty.HARD -> minBuffer + Random.nextInt(10, 80)
        }

        val sequence = (0 until length).map { start - it * step }
        val danger = start - length * step
        val rule = "-$step each step"

        return NumberPattern(
            type = PatternType.SUBTRACTION,
            sequence = sequence,
            dangerousNumber = danger,
            difficulty = difficulty,
            ruleDescription = rule
        )
    }

    private fun generateMultiplicationPattern(length: Int, difficulty: Difficulty): NumberPattern {
        val multiplier = if (difficulty == Difficulty.HARD && Random.nextFloat() < 0.3f) 3 else 2
        // To keep numbers from exploding:
        val maxSafeStart = if (multiplier == 3) {
            if (length >= 5) 1 else 2
        } else {
            when {
                length >= 6 -> 2
                length >= 5 -> 3
                else -> 4
            }
        }
        val start = Random.nextInt(1, maxSafeStart + 1)

        val fullList = mutableListOf<Int>()
        var cur = start
        for (i in 0..length) {
            fullList.add(cur)
            cur *= multiplier
        }

        val sequence = fullList.take(length)
        val danger = fullList.last()
        val rule = "×$multiplier each step"

        return NumberPattern(
            type = PatternType.MULTIPLICATION,
            sequence = sequence,
            dangerousNumber = danger,
            difficulty = difficulty,
            ruleDescription = rule
        )
    }

    private fun generateAlternatingPattern(length: Int, difficulty: Difficulty): NumberPattern {
        val pairs = listOf(
            Pair(3, 2),
            Pair(4, 2),
            Pair(5, 3),
            Pair(4, 1),
            Pair(6, 2),
            Pair(5, 2),
            Pair(7, 3),
            Pair(6, 4)
        )
        val (addStep, subStep) = pairs.random()
        val start = when (difficulty) {
            Difficulty.EASY -> Random.nextInt(2, 15)
            Difficulty.MEDIUM -> Random.nextInt(5, 30)
            Difficulty.HARD -> Random.nextInt(10, 50)
        }

        val fullList = mutableListOf<Int>()
        var cur = start
        fullList.add(cur)

        for (i in 0 until length) {
            if (i % 2 == 0) {
                cur += addStep
            } else {
                cur -= subStep
            }
            fullList.add(cur)
        }

        val sequence = fullList.take(length)
        val danger = fullList.last()
        val rule = "+$addStep, -$subStep alternating"

        return NumberPattern(
            type = PatternType.ALTERNATING,
            sequence = sequence,
            dangerousNumber = danger,
            difficulty = difficulty,
            ruleDescription = rule
        )
    }

    private fun generateIncreasingDiffPattern(length: Int, difficulty: Difficulty): NumberPattern {
        val diffStep = if (difficulty == Difficulty.HARD && Random.nextBoolean()) 2 else 1
        val initialDiff = if (diffStep == 1) Random.nextInt(1, 4) else 2
        val start = Random.nextInt(1, 20)

        val fullList = mutableListOf<Int>()
        var cur = start
        fullList.add(cur)
        var currentDiff = initialDiff

        for (i in 0 until length) {
            cur += currentDiff
            fullList.add(cur)
            currentDiff += diffStep
        }

        val sequence = fullList.take(length)
        val danger = fullList.last()
        val rule = "Differences increase by +$diffStep"

        return NumberPattern(
            type = PatternType.INCREASING_DIFFERENCE,
            sequence = sequence,
            dangerousNumber = danger,
            difficulty = difficulty,
            ruleDescription = rule
        )
    }

    private fun generateRepeatingDiffPattern(length: Int, difficulty: Difficulty): NumberPattern {
        val pairs = listOf(
            Pair(3, 1),
            Pair(4, 2),
            Pair(5, 2),
            Pair(6, 3),
            Pair(5, 1),
            Pair(7, 2)
        )
        val (stepA, stepB) = pairs.random()
        val start = Random.nextInt(2, 25)

        val fullList = mutableListOf<Int>()
        var cur = start
        fullList.add(cur)

        for (i in 0 until length) {
            if (i % 2 == 0) {
                cur += stepA
            } else {
                cur -= stepB
            }
            fullList.add(cur)
        }

        val sequence = fullList.take(length)
        val danger = fullList.last()
        val rule = "+$stepA, -$stepB repeating"

        return NumberPattern(
            type = PatternType.REPEATING_DIFFERENCE,
            sequence = sequence,
            dangerousNumber = danger,
            difficulty = difficulty,
            ruleDescription = rule
        )
    }

    /**
     * Validates whether a pattern conforms to fairness and boundary constraints.
     */
    fun validatePattern(pattern: NumberPattern): Boolean {
        val seq = pattern.sequence
        val danger = pattern.dangerousNumber

        // Must have at least 4 visible values
        if (seq.size < 4) return false

        // Positive in Easy
        if (pattern.difficulty == Difficulty.EASY) {
            if (seq.any { it <= 0 } || danger <= 0) return false
        }

        // Must not explode in size
        if (seq.any { it > 10000 } || danger > 15000) return false

        // Sequence should not have all identical values
        if (seq.distinct().size == 1) return false

        // For multiplication, no zero or negative
        if (pattern.type == PatternType.MULTIPLICATION) {
            if (seq.any { it <= 0 } || danger <= 0) return false
        }

        return true
    }

    fun clearHistory() {
        lastSequences.clear()
    }

    private fun fallbackPattern(difficulty: Difficulty): NumberPattern {
        return NumberPattern(
            type = PatternType.ADDITION,
            sequence = listOf(3, 6, 9, 12),
            dangerousNumber = 15,
            difficulty = difficulty,
            ruleDescription = "+3 each step"
        )
    }
}
