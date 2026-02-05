package com.example.aviatorbot

object StrategyEngine {
    private val history = mutableListOf<Double>()

    // Config
    const val TARGET_MULTIPLIER = 1.50

    fun clear() {
        history.clear()
    }

    fun onRoundCrash(multiplier: Double) {
        history.add(multiplier)
        if (history.size > 100) history.removeAt(0)
    }

    fun shouldBet(): Boolean {
        if (history.isEmpty()) return false

        // Strategy: "The 3 Lows"
        // If the last 3 rounds were very low (< 1.20x), bet now.
        val last3 = history.takeLast(3)
        if (last3.size == 3 && last3.all { it < 1.20 }) {
            return true
        }

        // Strategy: "After a Big Drop"
        // If last round was 1.00x (insta-crash), bet.
        if (history.last() == 1.00) return true

        return false
    }

    fun getStats(): String {
        val avg = if (history.isNotEmpty()) history.average() else 0.0
        return "History: ${history.size} rounds. Avg: %.2fx".format(avg)
    }
}
