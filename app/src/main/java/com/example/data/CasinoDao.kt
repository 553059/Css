package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CasinoDao {

    // --- GAME SESSIONS ---
    @Query("SELECT * FROM game_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<GameSession>>

    @Query("SELECT * FROM game_sessions WHERE gameType = :gameType ORDER BY timestamp DESC")
    fun getSessionsByGame(gameType: String): Flow<List<GameSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GameSession)

    @Delete
    suspend fun deleteSession(session: GameSession)

    @Query("DELETE FROM game_sessions")
    suspend fun deleteAllSessions()

    // --- ACTIVE BONUSES ---
    @Query("SELECT * FROM active_bonuses ORDER BY timestampAdded DESC")
    fun getAllBonuses(): Flow<List<ActiveBonus>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBonus(bonus: ActiveBonus)

    @Update
    suspend fun updateBonus(bonus: ActiveBonus)

    @Delete
    suspend fun deleteBonus(bonus: ActiveBonus)

    // --- AI THOUGHTS / IDEAS ---
    @Query("SELECT * FROM ai_thoughts ORDER BY timestamp DESC LIMIT 50")
    fun getRecentThoughts(): Flow<List<AIPensamiento>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThought(thought: AIPensamiento)

    @Query("DELETE FROM ai_thoughts")
    suspend fun deleteAllThoughts()
}
