package com.example.wordle.core

enum class LetterResult { CORRECT, PRESENT, ABSENT }

data class Evaluation(val results: List<LetterResult>)

object WordleEngine {
	fun evaluateGuess(guess: String, target: String): Evaluation {
		val length = target.length
		val result = MutableList(length) { LetterResult.ABSENT }
		val remaining = IntArray(26)

		for (i in 0 until length) {
			if (guess[i] == target[i]) {
				result[i] = LetterResult.CORRECT
			} else {
				val idx = target[i] - 'a'
				if (idx in 0..25) remaining[idx]++
			}
		}

		for (i in 0 until length) {
			if (result[i] == LetterResult.CORRECT) continue
			val idx = guess[i] - 'a'
			if (idx in 0..25 && remaining[idx] > 0) {
				result[i] = LetterResult.PRESENT
				remaining[idx]--
			}
		}
		return Evaluation(result)
	}
}