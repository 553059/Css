package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_sessions")
data class GameSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gameType: String, // "Roulette", "Blackjack", "Slots"
    val timestamp: Long = System.currentTimeMillis(),
    val outcome: String, // "WIN", "LOSS", "PUSH"
    val betAmount: Double,
    val winAmount: Double,
    val details: String // Extra information like card totals, roulette numbers, or spin result
)
