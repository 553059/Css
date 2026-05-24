package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_thoughts")
data class AIPensamiento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val gameContext: String, // e.g. "Blackjack Count: +4" or "Roulette Red Wave"
    val thoughtText: String,
    val calculatedRiskRating: String, // "LOW", "MEDIUM", "HIGH"
    val suggestions: String // Bulleted strategies
)
