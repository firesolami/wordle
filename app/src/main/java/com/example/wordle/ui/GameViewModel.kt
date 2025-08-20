package com.example.wordle.ui
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordle.core.WordleEngine
import com.example.wordle.core.LetterResult
import com.example.wordle.data.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class GuessRow(val guess: String, val results: List<LetterResult>?)

data class GameState(
	val wordLength: Int = 5,
	val maxAttempts: Int = 6,
	val rows: List<GuessRow> = emptyList(),
	val currentInput: String = "",
	val isLoading: Boolean = true,
	val message: String? = null,
	val keyboardHints: Map<Char, LetterResult> = emptyMap(),
	val isWon: Boolean = false,
	val isLost: Boolean = false
)

class GameViewModel(app: Application) : AndroidViewModel(app) {
	private val repository = WordRepository(app)
	private val _state = MutableStateFlow(GameState())
	val state: StateFlow<GameState> = _state.asStateFlow()
	private var dictionary: Set<String> = emptySet()
	private var targets: List<String> = emptyList()
	private var target: String = ""

	init {
		viewModelScope.launch(Dispatchers.IO) {
			dictionary = repository.loadDictionary()
			targets = repository.loadTargets()
			target = targets[Random.nextInt(targets.size)]
			_state.value = GameState(wordLength = target.length, rows = List(6) { GuessRow("", null) }, isLoading = false)
		}
	}

	fun onKey(char: Char) {
		if (_state.value.isWon || _state.value.isLost) return
		if (_state.value.isLoading) return
		val s = _state.value
		if (char in 'a'..'z' && s.currentInput.length < s.wordLength) {
			_state.value = s.copy(currentInput = s.currentInput + char)
		}
	}

	fun onBackspace() {
		val s = _state.value
		if (s.currentInput.isNotEmpty()) {
			_state.value = s.copy(currentInput = s.currentInput.dropLast(1))
		}
	}

	fun onEnter() {
		val s = _state.value
		if (s.currentInput.length != s.wordLength) {
			flashMessage("Not enough letters")
			return
		}
		if (s.currentInput !in dictionary) {
			flashMessage("Not in dictionary")
			return
		}
		val evaluation = WordleEngine.evaluateGuess(s.currentInput, target)
		val updatedRows = s.rows.toMutableList()
		val rowIndex = updatedRows.indexOfFirst { it.results == null }
		updatedRows[rowIndex] = GuessRow(s.currentInput, evaluation.results)

		val newHints = s.keyboardHints.toMutableMap()
		for ((i, c) in s.currentInput.withIndex()) {
			val res = evaluation.results[i]
			val prev = newHints[c]
			if (prev == null || res.ordinal < prev.ordinal) {
				newHints[c] = res
			}
		}

		val isWin = s.currentInput == target
		val isLose = !isWin && rowIndex == s.maxAttempts - 1
		_state.value = s.copy(
			rows = updatedRows,
			currentInput = "",
			keyboardHints = newHints,
			isWon = isWin,
			isLost = isLose,
			message = if (isWin) "Great!" else if (isLose) target.uppercase() else null
		)
	}

	private fun flashMessage(msg: String) {
		_state.value = _state.value.copy(message = msg)
		viewModelScope.launch {
			kotlinx.coroutines.delay(1200)
			_state.value = _state.value.copy(message = null)
		}
	}

	fun onRestart() {
		viewModelScope.launch(Dispatchers.IO) {
			target = targets[Random.nextInt(targets.size)]
			_state.value = GameState(wordLength = target.length, rows = List(6) { GuessRow("", null) }, isLoading = false)
		}
	}
}