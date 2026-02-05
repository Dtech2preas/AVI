package com.example.aviatorbot

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var etTarget: EditText
    private lateinit var swSmartStrategy: Switch
    private lateinit var etLossStreak: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        etTarget = findViewById(R.id.et_target)
        swSmartStrategy = findViewById(R.id.sw_smart_strategy)
        etLossStreak = findViewById(R.id.et_loss_streak)
        btnSave = findViewById(R.id.btn_save)

        loadSettings()

        btnSave.setOnClickListener {
            saveSettings()
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("AviatorPrefs", Context.MODE_PRIVATE)
        val target = prefs.getFloat("target_multiplier", 1.20f)
        val smart = prefs.getBoolean("smart_strategy", false)
        val streak = prefs.getInt("max_loss_streak", 5)

        etTarget.setText(target.toString())
        swSmartStrategy.isChecked = smart
        etLossStreak.setText(streak.toString())
    }

    private fun saveSettings() {
        val prefs = getSharedPreferences("AviatorPrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        try {
            val targetStr = etTarget.text.toString()
            val target = if (targetStr.isNotEmpty()) targetStr.toFloat() else 1.20f

            val streakStr = etLossStreak.text.toString()
            val streak = if (streakStr.isNotEmpty()) streakStr.toInt() else 5

            editor.putFloat("target_multiplier", target)
            editor.putBoolean("smart_strategy", swSmartStrategy.isChecked)
            editor.putInt("max_loss_streak", streak)
            editor.apply()

            // Update Strategy Engine
            StrategyEngine.updateConfig(target.toDouble(), swSmartStrategy.isChecked)
            StrategyEngine.setMaxLossStreak(streak)

            Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
        }
    }
}
