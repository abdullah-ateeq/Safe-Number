package com.example.safenumber.data.model

data class NumberPattern(
    val type: PatternType,
    val sequence: List<Int>,
    val dangerousNumber: Int,
    val difficulty: Difficulty,
    val ruleDescription: String = ""
)
