package com.example.wordle.data

import android.content.Context
import kotlinx.serialization.json.Json

class WordRepository(private val context: Context) {
	private val json = Json

	suspend fun loadDictionary(): Set<String> {
		val text = context.assets.open("dictionary.json").bufferedReader().use { it.readText() }
		return json.decodeFromString<List<String>>(text).map { it.lowercase() }.toSet()
	}

	suspend fun loadTargets(): List<String> {
		val text = context.assets.open("targetWords.json").bufferedReader().use { it.readText() }
		return json.decodeFromString<List<String>>(text).map { it.lowercase() }
	}
} 