package com.example.api

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

// Represents a scraped or downloaded feed item from a supported casino API
data class LiveCasinoEvent(
    val casinoName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val gameType: String,
    val result: String, // e.g. "R17" (Red 17) or "Player 19 vs Dealer 18"
    val payoutFactor: Double
)

object MockCasinoService {

    private val SUPPORTED_CASINOS = listOf(
        "BetRoyal Casino Live",
        "ApexSpin Platinum",
        "VegasShield Premium",
        "Novatrading Gaming API"
    )

    private val ROULETTE_NUMBERS = (0..36).toList()
    private val ROULETTE_COLORS = mapOf(
        0 to "Green",
        1 to "Red", 2 to "Black", 3 to "Red", 4 to "Black", 5 to "Red", 6 to "Black",
        7 to "Red", 8 to "Black", 9 to "Red", 10 to "Black", 11 to "Black", 12 to "Red",
        13 to "Black", 14 to "Red", 15 to "Black", 16 to "Red", 17 to "Black", 18 to "Red",
        19 to "Red", 20 to "Black", 21 to "Red", 22 to "Black", 23 to "Red", 24 to "Black",
        25 to "Red", 26 to "Black", 27 to "Red", 28 to "Black", 29 to "Black", 30 to "Red",
        31 to "Black", 32 to "Red", 33 to "Black", 34 to "Red", 35 to "Black", 36 to "Red"
    )

    // Simulates an active live scraping socket/API polling.
    // Emits new outcomes from casino sites in real-time.
    fun streamLiveCasinoOutcomes(): Flow<LiveCasinoEvent> = flow {
        while (true) {
            delay(8000 + Random.nextLong(4000)) // Outcoming event every 8-12 seconds
            val casino = SUPPORTED_CASINOS.random()
            val isRoulette = Random.nextBoolean()
            
            val event = if (isRoulette) {
                val num = ROULETTE_NUMBERS.random()
                val color = ROULETTE_COLORS[num] ?: "Green"
                LiveCasinoEvent(
                    casinoName = casino,
                    gameType = "Roulette",
                    result = "$color $num",
                    payoutFactor = if (num == 0) 35.0 else 1.0
                )
            } else {
                val dealerVal = Random.nextInt(17, 26) // 22+ means Bust
                val playerVal = Random.nextInt(15, 22)
                val outcome = when {
                    dealerVal > 21 -> "Jugador ($playerVal) vs Dealer (Bust $dealerVal)"
                    playerVal > dealerVal -> "Jugador ($playerVal) vs Dealer ($dealerVal)"
                    playerVal < dealerVal -> "Jugador ($playerVal) vs Dealer ($dealerVal) - Casa Gana"
                    else -> "Empate ($playerVal)"
                }
                LiveCasinoEvent(
                    casinoName = casino,
                    gameType = "Blackjack",
                    result = outcome,
                    payoutFactor = 1.5
                )
            }
            emit(event)
        }
    }

    // Example function representing direct HTTP requests to casinos (the requests.get python counterpart)
    suspend fun fetchDirectCasinoMetadata(casinoUrl: String): Map<String, Any> {
        delay(800) // Simulated connection load
        return mapOf(
            "url" to casinoUrl,
            "status" to "online",
            "ssl_secured" to true,
            "active_scrapers" to Random.nextInt(3, 8),
            "latency_ms" to Random.nextInt(45, 230),
            "payout_ratio" to 0.974
        )
    }
}
