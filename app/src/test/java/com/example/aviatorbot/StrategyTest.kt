package com.example.aviatorbot

import org.junit.Test
import org.junit.Assert.*

class StrategyTest {

    @Test
    fun testStrategyLogic() {
        StrategyEngine.clear()
        StrategyEngine.updateConfig(1.20, true) // Enable smart strategy

        // Test "Cold Streak" logic
        // 3 consecutive lows < 1.20 -> Expect rebound (Target 2.0)
        StrategyEngine.onRoundCrash(1.10)
        StrategyEngine.onRoundCrash(1.05)
        StrategyEngine.onRoundCrash(1.15)

        assertEquals(2.0, StrategyEngine.getNextTarget(), 0.01)

        // Test normal reset
        StrategyEngine.onRoundCrash(5.00) // High win
        assertEquals(1.20, StrategyEngine.getNextTarget(), 0.01) // Back to base
    }

    @Test
    fun testLossStreak() {
        StrategyEngine.clear()
        StrategyEngine.setMaxLossStreak(3)
        StrategyEngine.updateConfig(2.00, false) // Base target 2.00

        // 3 Losses (Crash < 1.20 which is definitely < 2.00)
        // Note: StrategyEngine logic for loss is strictly < 1.20 for streak calculation
        StrategyEngine.onRoundCrash(1.10)
        StrategyEngine.onRoundCrash(1.10)
        StrategyEngine.onRoundCrash(1.10)

        // Should stop betting
        assertFalse("Should stop betting after max loss streak", StrategyEngine.shouldBet())

        // Add a win (Crash > 1.20)
        StrategyEngine.onRoundCrash(2.50)
        assertTrue("Should resume betting after a win", StrategyEngine.shouldBet())
    }
}
