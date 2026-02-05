package com.example.aviatorbot

class StrategyEngine(private val callback: Callback) {

    interface Callback {
        fun performClick(action: String) // "BET", "CASHOUT"
        fun log(message: String)
        fun onStatsUpdated(wins: Int, losses: Int, balance: Double)
    }

    var isLiveMode = false
    var isRunning = false

    private val history = mutableListOf<Double>()
    private var isBetPlaced = false
    private val targetMultiplier = 1.20 // Conservative strategy

    // Stats
    private var demoWins = 0
    private var demoLosses = 0
    private var demoBalance = 1000.0 // Virtual currency

    fun onGameStart() {
        if (!isRunning) return

        // Simple Strategy: If the last crash was very low, maybe we are due for a win?
        // Or just bet every round for now.
        val shouldBet = true

        if (shouldBet) {
            callback.log("Strategy: Decided to bet. Target: ${targetMultiplier}x")
            isBetPlaced = true

            if (isLiveMode) {
                callback.performClick("BET")
            } else {
                callback.log("Strategy [DEMO]: Virtual Bet placed.")
                demoBalance -= 10.0 // Virtual bet amount
            }
        }
    }

    fun onMultiplierUpdate(currentMultiplier: Double) {
        if (!isRunning || !isBetPlaced) return

        if (currentMultiplier >= targetMultiplier) {
            // Cashout trigger
            if (isLiveMode) {
                callback.performClick("CASHOUT")
                callback.log("Strategy [LIVE]: Cashing out at $currentMultiplier")
            } else {
                callback.log("Strategy [DEMO]: Virtual Cashout at $currentMultiplier. WIN!")
                demoWins++
                demoBalance += (10.0 * currentMultiplier)
            }
            isBetPlaced = false
            updateStats()
        }
    }

    fun onGameCrash(crashValue: Double) {
        history.add(crashValue)
        callback.log("Game Ended. Crash: ${crashValue}x")

        if (isBetPlaced) {
            // If we are here, we didn't cash out in time
            if (isLiveMode) {
                callback.log("Strategy [LIVE]: LOST (Crashed before target)")
            } else {
                callback.log("Strategy [DEMO]: Virtual LOSS.")
                demoLosses++
            }
            isBetPlaced = false
            updateStats()
        }
    }

    private fun updateStats() {
        callback.onStatsUpdated(demoWins, demoLosses, demoBalance)
    }

    fun resetStats() {
        demoWins = 0
        demoLosses = 0
        demoBalance = 1000.0
        history.clear()
        updateStats()
    }
}
