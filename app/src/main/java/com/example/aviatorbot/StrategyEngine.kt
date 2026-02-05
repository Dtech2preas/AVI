package com.example.aviatorbot

import kotlin.math.pow
import kotlin.math.sqrt

object StrategyEngine {
    private val history = mutableListOf<Double>()

    // Config
    private var baseTargetMultiplier = 1.20
    private var isSmartStrategyEnabled = false
    private var maxLossStreak = 100 // Default high
    private var currentTarget = 1.20

    fun clear() {
        history.clear()
    }

    // Config methods
    fun updateConfig(target: Double, smartEnabled: Boolean) {
        baseTargetMultiplier = target
        isSmartStrategyEnabled = smartEnabled
        recalculateTarget() // Update immediately
    }

    fun setMaxLossStreak(streak: Int) {
        maxLossStreak = streak
    }

    fun onRoundCrash(multiplier: Double) {
        history.add(multiplier)
        if (history.size > 100) history.removeAt(0)

        recalculateTarget()
    }

    private fun recalculateTarget() {
        // Default
        currentTarget = baseTargetMultiplier

        if (isSmartStrategyEnabled && history.isNotEmpty()) {
            // Smart Logic: Dynamic Adjustment

            // 1. "The Rebound": If last 3 rounds were very low (< 1.20), the algorithm
            // often "pays out" soon. We risk a higher target.
            val last3 = history.takeLast(3)
            if (last3.size == 3 && last3.all { it < 1.20 }) {
                currentTarget = 2.0 // Aggressive
            }
            // 2. "The Stable Run": If variance is low, we can creep up.
            else {
                val last10 = history.takeLast(10)
                if (last10.size >= 5) {
                    val avg = last10.average()
                    val variance = last10.map { (it - avg).pow(2) }.average()

                    // If stable and average is decent
                    if (variance < 0.5 && avg > 1.5) {
                        currentTarget = 1.5
                    }
                }
            }
        }
    }

    fun getNextTarget(): Double {
        return currentTarget
    }

    fun shouldBet(): Boolean {
        if (history.isEmpty()) return true

        // Check for loss streak
        // We define a "Loss" as the crash occurring below our BASE target (since that's the minimum expectation)
        // or below 1.10 (immediate crash).

        var currentStreak = 0
        for (i in history.indices.reversed()) {
            // Strict loss check: did it crash < 1.20?
            if (history[i] < 1.20) {
                currentStreak++
            } else {
                break
            }
        }

        if (currentStreak >= maxLossStreak) {
             // Stop betting
             return false
        }

        return true
    }

    fun getStats(): String {
        val avg = if (history.isNotEmpty()) history.average() else 0.0
        val streak = calculateCurrentLossStreak()
        return "History: ${history.size} rounds. Avg: %.2fx. Streak: $streak. Next: %.2fx".format(avg, currentTarget)
    }

    private fun calculateCurrentLossStreak(): Int {
        var currentStreak = 0
        for (i in history.indices.reversed()) {
            if (history[i] < 1.20) {
                currentStreak++
            } else {
                break
            }
        }
        return currentStreak
    }

    // Accessor for testing
    fun getHistorySize() = history.size
}
