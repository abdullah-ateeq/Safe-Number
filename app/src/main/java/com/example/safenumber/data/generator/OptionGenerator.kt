package com.example.safenumber.data.generator

import com.example.safenumber.data.model.Difficulty
import com.example.safenumber.data.model.NumberPattern
import com.example.safenumber.data.model.PatternType
import kotlin.math.abs
import kotlin.random.Random

object OptionGenerator {

    /**
     * Generates a list of distinct options containing [dangerousNumber] exactly once,
     * along with [optionCount] - 1 safe distractors that are plausible yet do not form
     * an alternate valid continuation.
     */
    fun generateOptions(
        dangerousNumber: Int,
        pattern: NumberPattern,
        optionCount: Int
    ): List<Int> {
        val options = mutableSetOf<Int>()
        options.add(dangerousNumber)

        val invalidContinuations = determineInvalidContinuations(pattern)

        // Potential offsets relative to dangerous number
        val offsetPool = listOf(
            -1, 1, -2, 2, -3, 3, -4, 4, -5, 5, -6, 6, -7, 7, -8, 8, -10, 10
        ).shuffled(Random)

        for (offset in offsetPool) {
            if (options.size >= optionCount) break
            val candidate = dangerousNumber + offset

            if (isValidSafeCandidate(candidate, dangerousNumber, pattern, invalidContinuations, options)) {
                options.add(candidate)
            }
        }

        // If still need more, generate close bounds
        var delta = 1
        while (options.size < optionCount && delta < 50) {
            val candidateA = dangerousNumber + delta
            if (isValidSafeCandidate(candidateA, dangerousNumber, pattern, invalidContinuations, options)) {
                options.add(candidateA)
            }
            if (options.size >= optionCount) break

            val candidateB = dangerousNumber - delta
            if (isValidSafeCandidate(candidateB, dangerousNumber, pattern, invalidContinuations, options)) {
                options.add(candidateB)
            }
            delta++
        }

        return options.toList().shuffled()
    }

    private fun determineInvalidContinuations(pattern: NumberPattern): Set<Int> {
        val invalid = mutableSetOf<Int>()
        val seq = pattern.sequence
        val danger = pattern.dangerousNumber

        if (seq.size >= 2) {
            val lastDiff = danger - seq.last()
            // Next continuation after danger (e.g., danger + step)
            invalid.add(danger + lastDiff)
            // Sequence items themselves should not be options if we want to avoid confusing backwards repetition
            invalid.addAll(seq)
        }

        // For multiplication, e.g. danger * 2
        if (pattern.type == PatternType.MULTIPLICATION && seq.size >= 2) {
            val ratio = if (seq[0] != 0) seq[1] / seq[0] else 2
            invalid.add(danger * ratio)
        }

        return invalid
    }

    private fun isValidSafeCandidate(
        candidate: Int,
        dangerousNumber: Int,
        pattern: NumberPattern,
        invalidContinuations: Set<Int>,
        currentOptions: Set<Int>
    ): Boolean {
        if (candidate == dangerousNumber) return false
        if (currentOptions.contains(candidate)) return false
        if (invalidContinuations.contains(candidate)) return false

        // Keep positive for Easy mode
        if (pattern.difficulty == Difficulty.EASY && candidate <= 0) return false

        // In any mode, keep candidate > 0 unless pattern actually has negative values
        val minSeq = pattern.sequence.minOrNull() ?: 0
        if (minSeq >= 0 && candidate < 0) return false

        // Proximity constraint: do not generate absurd distractors
        val maxDiff = when (pattern.type) {
            PatternType.MULTIPLICATION -> (abs(dangerousNumber) * 0.6).toInt().coerceAtLeast(10)
            else -> 25
        }
        if (abs(candidate - dangerousNumber) > maxDiff) return false

        return true
    }
}
