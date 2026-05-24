package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_bonuses")
data class ActiveBonus(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val casinoName: String,
    val bonusName: String,
    val amount: Double,
    val wageringRequirementMultiplier: Double, // e.g. 35.0 for 35x
    val targetWagerAmount: Double, // amount * wageringRequirementMultiplier
    val currentWageredAmount: Double,
    val timestampAdded: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
) {
    val progressFraction: Float
        get() = if (targetWagerAmount > 0) {
            (currentWageredAmount / targetWagerAmount).coerceIn(0.0, 1.0).toFloat()
        } else {
            1f
        }

    val remainingWager: Double
        get() = (targetWagerAmount - currentWageredAmount).coerceAtLeast(0.0)
}
