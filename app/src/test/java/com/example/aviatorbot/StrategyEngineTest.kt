package com.example.aviatorbot

import org.junit.Test
import org.junit.Assert.*

class StrategyEngineTest {
    @Test
    fun testStrategyLogic() {
        StrategyEngine.clear()
        assertFalse(StrategyEngine.shouldBet())

        // Add 3 low values
        StrategyEngine.onRoundCrash(1.10)
        StrategyEngine.onRoundCrash(1.15)
        StrategyEngine.onRoundCrash(1.05)

        // Should bet now
        assertTrue(StrategyEngine.shouldBet())

        // Add a high value
        StrategyEngine.onRoundCrash(2.00)

        // Should not bet (pattern broken)
        assertFalse(StrategyEngine.shouldBet())
    }
}
