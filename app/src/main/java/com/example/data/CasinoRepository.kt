package com.example.data

import kotlinx.coroutines.flow.Flow

class CasinoRepository(private val casinoDao: CasinoDao) {

    val allSessions: Flow<List<GameSession>> = casinoDao.getAllSessions()
    val allBonuses: Flow<List<ActiveBonus>> = casinoDao.getAllBonuses()
    val recentThoughts: Flow<List<AIPensamiento>> = casinoDao.getRecentThoughts()

    fun getSessionsByGame(gameType: String): Flow<List<GameSession>> =
        casinoDao.getSessionsByGame(gameType)

    suspend fun insertSession(session: GameSession) {
        casinoDao.insertSession(session)
    }

    suspend fun deleteSession(session: GameSession) {
        casinoDao.deleteSession(session)
    }

    suspend fun deleteAllSessions() {
        casinoDao.deleteAllSessions()
    }

    suspend fun insertBonus(bonus: ActiveBonus) {
        casinoDao.insertBonus(bonus)
    }

    suspend fun updateBonus(bonus: ActiveBonus) {
        casinoDao.updateBonus(bonus)
    }

    suspend fun deleteBonus(bonus: ActiveBonus) {
        casinoDao.deleteBonus(bonus)
    }

    suspend fun insertThought(thought: AIPensamiento) {
        casinoDao.insertThought(thought)
    }

    suspend fun deleteAllThoughts() {
        casinoDao.deleteAllThoughts()
    }
}
