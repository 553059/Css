package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiManager
import com.example.api.LiveCasinoEvent
import com.example.api.MockCasinoService
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = CasinoDatabase.getDatabase(application)
    private val repository = CasinoRepository(db.casinoDao())

    // --- SQLite Persistent Streams ---
    val gameSessions: StateFlow<List<GameSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeBonuses: StateFlow<List<ActiveBonus>> = repository.allBonuses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiThoughts: StateFlow<List<AIPensamiento>> = repository.recentThoughts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Live Scraping/API Feed Integration ---
    private val _liveCasinoFeed = MutableStateFlow<List<LiveCasinoEvent>>(emptyList())
    val liveCasinoFeed: StateFlow<List<LiveCasinoEvent>> = _liveCasinoFeed.asStateFlow()

    // --- Configurable Controls & Sensitivity ---
    private val _matchThreshold = MutableStateFlow(0.70f) // Threshold of coincidence/success
    val matchThreshold = _matchThreshold.asStateFlow()

    private val _backgroundSimulationActive = MutableStateFlow(true)
    val backgroundSimulationActive = _backgroundSimulationActive.asStateFlow()

    private val _obfuscationActive = MutableStateFlow(true) // Simulates AES DB Encryption logs
    val obfuscationActive = _obfuscationActive.asStateFlow()

    // --- Stealth / Camouflage Mode ---
    private val _camouflageActive = MutableStateFlow(false)
    val camouflageActive = _camouflageActive.asStateFlow()

    private val _camouflagePIN = MutableStateFlow("1234")
    val camouflagePIN = _camouflagePIN.asStateFlow()

    // --- Blackjack Tracker State ---
    private val _blackjackRunningCount = MutableStateFlow(0)
    val blackjackRunningCount = _blackjackRunningCount.asStateFlow()

    private val _blackjackDecksCount = MutableStateFlow(4) // standard 4 decks
    val blackjackDecksCount = _blackjackDecksCount.asStateFlow()

    // --- Roulette Tracker State ---
    private val _rouletteHistoryColors = MutableStateFlow<List<String>>(emptyList()) // Red, Black, Green
    val rouletteHistoryColors = _rouletteHistoryColors.asStateFlow()

    // --- Automated Autoclicker Console (Bono Activator) ---
    private val _autoclickerActive = MutableStateFlow(false)
    val autoclickerActive = _autoclickerActive.asStateFlow()

    private val _autoclickerLogs = MutableStateFlow<List<String>>(listOf("Consola inicializada. En espera de activar bono..."))
    val autoclickerLogs = _autoclickerLogs.asStateFlow()

    // --- Gemini Call Indicator ---
    private val _isGeneratingAdvice = MutableStateFlow(false)
    val isGeneratingAdvice = _isGeneratingAdvice.asStateFlow()

    // --- Control Jobs ---
    private var streamJob: Job? = null
    private var autoclickerJob: Job? = null
    private var backgroundTaskJob: Job? = null

    init {
        // Seed initial data if the database is completely empty
        seedInitialData()

        // Maintain live socket scraping stream
        startScrapingFeed()

        // Background simulation engine: periodically triggers simulated data ingestion if active
        startBackgroundTaskEngine()
    }

    private fun seedInitialData() {
        viewModelScope.launch(Dispatchers.IO) {
            val sessionsEmpty = repository.allSessions.first().isEmpty()
            if (sessionsEmpty) {
                Log.d("MainViewModel", "Seeding initial historical analyzer sessions...")
                repository.insertSession(GameSession(gameType = "Roulette", outcome = "WIN", betAmount = 20.0, winAmount = 40.0, details = "Apuesta Color Rojo - Victoria de R16"))
                repository.insertSession(GameSession(gameType = "Blackjack", outcome = "WIN", betAmount = 50.0, winAmount = 100.0, details = "Maso Cuenta +3 - Doble Abajo en 11"))
                repository.insertSession(GameSession(gameType = "Roulette", outcome = "LOSS", betAmount = 15.0, winAmount = 0.0, details = "Martingala n2 fallada - Negro B24"))
                repository.insertSession(GameSession(gameType = "Blackjack", outcome = "LOSS", betAmount = 30.0, winAmount = 0.0, details = "Maso Cuenta +1 - Pasado de 21"))
                repository.insertSession(GameSession(gameType = "Slots", outcome = "WIN", betAmount = 5.0, winAmount = 45.0, details = "Mega GIRO - Auto-Click en Bono Activado"))
            }

            val bonusesEmpty = repository.allBonuses.first().isEmpty()
            if (bonusesEmpty) {
                Log.d("MainViewModel", "Seeding initial promotional casino bonuses...")
                repository.insertBonus(ActiveBonus(casinoName = "ApexSpin Platinum", bonusName = "Bono de Bienvenida 100%", amount = 100.0, wageringRequirementMultiplier = 35.0, targetWagerAmount = 3500.0, currentWageredAmount = 1250.0))
                repository.insertBonus(ActiveBonus(casinoName = "WinVegas Deluxe", bonusName = "Bono VIP Clics Semanal", amount = 50.0, wageringRequirementMultiplier = 10.0, targetWagerAmount = 500.0, currentWageredAmount = 480.0))
            }

            val thoughtsEmpty = repository.recentThoughts.first().isEmpty()
            if (thoughtsEmpty) {
                Log.d("MainViewModel", "Seeding initial strategic advice...")
                repository.insertThought(AIPensamiento(
                    gameContext = "Blackjack Cuenta: Alta (+4)",
                    thoughtText = "El zapato de cartas actual presenta una densidad desproporcionada de cartas de valor 10 (ases, reyes, reinas, jotas).",
                    calculatedRiskRating = "LOW",
                    suggestions = "• Eleve el tamaño de la apuesta un 50% según la estrategia de conteo Hi-Lo.\n• Mantenga jugadas defensivas estrictas. El dealer tiene mayor probabilidad de pasarse."
                ))
            }
        }
    }

    private fun startScrapingFeed() {
        streamJob?.cancel()
        streamJob = viewModelScope.launch(Dispatchers.Default) {
            MockCasinoService.streamLiveCasinoOutcomes().collect { event ->
                _liveCasinoFeed.update { (listOf(event) + it).take(30) }
                // Live stats tracking feeding: color count for roulette or running count
                if (event.gameType == "Roulette") {
                    val color = if (event.result.contains("Red")) "Red" else if (event.result.contains("Black")) "Black" else "Green"
                    _rouletteHistoryColors.update { (listOf(color) + it).take(15) }
                }
            }
        }
    }

    private fun startBackgroundTaskEngine() {
        backgroundTaskJob?.cancel()
        backgroundTaskJob = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                kotlinx.coroutines.delay(15000) // check and simulate background telemetry every 15s
                if (_backgroundSimulationActive.value) {
                    val coinFlip = Random.nextBoolean()
                    val game = if (coinFlip) "Blackjack" else "Roulette"
                    val isWin = Random.nextFloat() < _matchThreshold.value // wins correspond to threshold alignment!
                    
                    val bAmount = Random.nextInt(5, 50).toDouble()
                    val wAmount = if (isWin) bAmount * 2 else 0.0
                    val outcomeStr = if (isWin) "WIN" else "LOSS"

                    val details = if (game == "Roulette") {
                        val num = Random.nextInt(0, 37)
                        val color = if (num == 0) "Green" else if (num % 2 == 0) "Black" else "Red"
                        "Análisis Stealth - Giros simulado: $color $num"
                    } else {
                        val count = _blackjackRunningCount.value
                        "Zapato simulado en segundo plano con cuenta de cartas: $count"
                    }

                    // Append session safely
                    val session = GameSession(
                        gameType = game,
                        outcome = outcomeStr,
                        betAmount = bAmount,
                        winAmount = wAmount,
                        details = details
                    )
                    
                    repository.insertSession(session)

                    // Also if there is an active bonus, increment its progress wager automatically via stealth backend play!
                    incrementActiveBonusWagers(bAmount)
                }
            }
        }
    }

    private suspend fun incrementActiveBonusWagers(amount: Double) {
        val bonuses = repository.allBonuses.first()
        val active = bonuses.firstOrNull { !it.isCompleted }
        if (active != null) {
            val newWagered = active.currentWageredAmount + amount
            val completed = newWagered >= active.targetWagerAmount
            val updated = active.copy(
                currentWageredAmount = newWagered,
                isCompleted = completed
            )
            repository.updateBonus(updated)
            
            if (completed) {
                addLogEntry("[LOGRO BONUS] El Bono del casino '${active.casinoName}' ha completado el requisito de Rollover del rollover!")
            }
        }
    }

    // --- GUI Controls ---

    fun setMatchThreshold(threshold: Float) {
        _matchThreshold.value = threshold
    }

    fun setBackgroundSimulation(enabled: Boolean) {
        _backgroundSimulationActive.value = enabled
        addLogEntry("[SISTEMA] Simulación invisible en segundo plano: ${if (enabled) "ACTIVADA" else "DESACTIVADA"}")
    }

    fun setObfuscationActive(enabled: Boolean) {
        _obfuscationActive.value = enabled
        addLogEntry("[STEALTH] Ofuscación de telemetría y cifrado AES-256: ${if (enabled) "ACTIVADO" else "PASIVO"}")
    }

    fun setCamouflageActive(enabled: Boolean) {
        _camouflageActive.value = enabled
    }

    fun addLogEntry(msg: String) {
        val formatted = "[${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}] $msg"
        _autoclickerLogs.update { (listOf(formatted) + it).take(50) }
    }

    // --- Interactive Entry Operations ---

    fun logCardValue(cardVal: String) {
        // Hi-Lo tracking:
        // 2, 3, 4, 5, 6 = +1
        // 7, 8, 9 = 0
        // 10, J, Q, K, A = -1
        val increment = when (cardVal) {
            "2", "3", "4", "5", "6" -> 1
            "10", "J", "Q", "K", "A" -> -1
            else -> 0
        }
        _blackjackRunningCount.update { it + increment }
        
        viewModelScope.launch {
            repository.insertSession(GameSession(
                gameType = "Blackjack",
                outcome = "PUSH",
                betAmount = 0.0,
                winAmount = 0.0,
                details = "Entrada Manual - Carta: $cardVal (Cuenta Hi-Lo: ${_blackjackRunningCount.value})"
            ))
        }
        addLogEntry("[DETECTOR BJ] Carta ingresada: $cardVal. Cuenta corriente actualizada a: ${_blackjackRunningCount.value}")
    }

    fun resetCardCount() {
        _blackjackRunningCount.value = 0
        addLogEntry("[DETECTOR BJ] Cuenta de cartas reiniciada a cero.")
    }

    fun logRouletteSpin(number: Int) {
        val isRedValue = listOf(1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36)
        val color = when {
            number == 0 -> "Green"
            isRedValue.contains(number) -> "Red"
            else -> "Black"
        }
        _rouletteHistoryColors.update { (listOf(color) + it).take(15) }
        
        viewModelScope.launch {
            repository.insertSession(GameSession(
                gameType = "Roulette",
                outcome = "PUSH",
                betAmount = 0.0,
                winAmount = 0.0,
                details = "Entrada Manual - Giro Ruleta: $color $number"
            ))
        }
        addLogEntry("[DETECTOR RUL] Giro de Ruleta ingresado: $number ($color)")
    }

    // --- Bono Activator Click Automaton Simulation ---

    fun toggleAutoclicker() {
        val nextState = !_autoclickerActive.value
        _autoclickerActive.value = nextState
        
        if (nextState) {
            addLogEntry("[AUTO-CLICK] Iniciando modulo transaccional Bono Activator...")
            autoclickerJob?.cancel()
            autoclickerJob = viewModelScope.launch(Dispatchers.Default) {
                while (true) {
                    kotlinx.coroutines.delay(4000 + Random.nextLong(2000))
                    // Select coordinates randomly mimicking stealth clicking
                    val x = Random.nextInt(120, 800)
                    val y = Random.nextInt(250, 1500)
                    
                    addLogEntry("[ACTIVADOR] Clic simulado en pantalla (X=$x, Y=$y) - Enviando impulso de apuesta mínima.")
                    
                    // Periodically complete automatic betting results
                    val earned = if (Random.nextInt(5) < 2) Random.nextDouble(10.0, 60.0) else 0.0
                    val isWin = earned > 0
                    
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.insertSession(GameSession(
                            gameType = "Slots",
                            outcome = if (isWin) "WIN" else "LOSS",
                            betAmount = 10.0,
                            winAmount = earned,
                            details = "Clic automático sigiloso - Posición ($x, $y)"
                        ))
                        incrementActiveBonusWagers(10.0)
                    }
                    
                    if (isWin) {
                        addLogEntry("[DETECTOR DE BONO] Recompensa de clics activada! Ganado: $+%.2f USD".format(earned))
                    }
                }
            }
        } else {
            autoclickerJob?.cancel()
            addLogEntry("[AUTO-CLICK] Deteniendo activador automático de clics. Proceso suspendido.")
        }
    }

    // --- Dynamic AI Strategic Thought Generator (Gemini Integration) ---

    fun executeStrategicThinking() {
        if (_isGeneratingAdvice.value) return
        _isGeneratingAdvice.value = true
        
        viewModelScope.launch(Dispatchers.IO) {
            // Collect context details to formulate prompt:
            val bjCount = _blackjackRunningCount.value
            val deckCount = _blackjackDecksCount.value
            val trueCount = if (deckCount > 0) bjCount.toFloat() / deckCount else bjCount.toFloat()
            
            val rHistory = _rouletteHistoryColors.value
            val redCount = rHistory.count { it == "Red" }
            val blackCount = rHistory.count { it == "Black" }
            val totalSpins = rHistory.size
            
            val currentThreshold = _matchThreshold.value
            
            val gameContext = "Blackjack Running Count: $bjCount (True Count: %.1f). Ruleta reciente: $totalSpins giros ($redCount Rojos, $blackCount Negros).".format(trueCount)
            
            val statsSnippet = """
                Sensibilidad de Coincidencia (Umbral): ${"%.1f".format(currentThreshold * 100)}%
                Estadísticas del Zapato (Decks): $deckCount
                Historial de apuestas registradas: ${gameSessions.value.size} en total.
                Total de Bonos de Casino activos: ${activeBonuses.value.count { !it.isCompleted }} en progreso.
                Autoclicker para liberar Rollover: ${if (autoclickerActive.value) "EJECUTANDO" else "EN ESPERA"}
            """.trimIndent()
            
            val response = GeminiManager.getStrategicAdvice(gameContext, statsSnippet)
            
            // Format risk rating
            val risk = when {
                bjCount > 2 || Math.abs(redCount - blackCount) > 2 -> "LOW"
                bjCount in -1..2 -> "MEDIUM"
                else -> "HIGH"
            }
            
            val thoughtsParts = response.split("\n\n")
            val thoughtCore = thoughtsParts.firstOrNull() ?: "Análisis finalizado."
            val bullets = thoughtsParts.drop(1).joinToString("\n\n")
            
            val newThought = AIPensamiento(
                gameContext = "Conteo BJ: $bjCount | Ruleta R:$redCount N:$blackCount",
                thoughtText = thoughtCore,
                calculatedRiskRating = risk,
                suggestions = bullets.ifEmpty { "• Mantener observación estadística constante.\n• Operar dentro de los umbrales de coincidencia fijados en el panel principal." }
            )
            
            repository.insertThought(newThought)
            _isGeneratingAdvice.value = false
            addLogEntry("[STRATEGY ENGINE] El Pensador Automático ha calculado el plan de juego óptimo.")
        }
    }

    // --- Action Operations for Room Modification ---

    fun addNewActiveBonus(casino: String, name: String, valAmount: Double, multiplier: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val totalTarget = valAmount * multiplier
            val bonus = ActiveBonus(
                casinoName = casino,
                bonusName = name,
                amount = valAmount,
                wageringRequirementMultiplier = multiplier,
                targetWagerAmount = totalTarget,
                currentWageredAmount = 0.0
            )
            repository.insertBonus(bonus)
            addLogEntry("[NUEVO BONO] Registrado bono de $ casino por $%.2f USD con rollover x%.0f".format(valAmount, multiplier))
        }
    }

    fun claimOrCompleteBonus(bonus: ActiveBonus) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = bonus.copy(
                currentWageredAmount = bonus.targetWagerAmount,
                isCompleted = true
            )
            repository.updateBonus(updated)
            addLogEntry("[ESTADO BONO] Marcado bono de '${bonus.casinoName}' como COMPLETADO.")
        }
    }

    fun deleteBonusById(bonus: ActiveBonus) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBonus(bonus)
            addLogEntry("[ESTADO BONO] Removido bono promocional.")
        }
    }

    fun purgeHistoryLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllSessions()
            repository.deleteAllThoughts()
            resetCardCount()
            _rouletteHistoryColors.value = emptyList()
            addLogEntry("[MANTENIMIENTO] Historial de telemetría y análisis purgado exitosamente.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Cancel all jobs to avoid resource leaks
        streamJob?.cancel()
        autoclickerJob?.cancel()
        backgroundTaskJob?.cancel()
    }
}
