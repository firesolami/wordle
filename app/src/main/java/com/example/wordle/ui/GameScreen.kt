package com.example.wordle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.RowScope
import androidx.compose.ui.unit.sp
import com.example.wordle.core.LetterResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel) {
	val state by viewModel.state.collectAsState()

	Scaffold(
		topBar = {
			CenterAlignedTopAppBar(
				title = { Text("Wordle", fontWeight = FontWeight.Bold) },
				actions = {
					TextButton(onClick = { viewModel.onRestart() }) { Text("Restart") }
				}
			)
		},
		bottomBar = {
			Keyboard(
				onKey = { viewModel.onKey(it) },
				onEnter = { viewModel.onEnter() },
				onBackspace = { viewModel.onBackspace() },
				hints = state.keyboardHints
			)
		}
	) { padding ->
		Box(Modifier.fillMaxSize().padding(padding)) {
			if (state.isLoading) {
				Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
			} else {
				Column(
					modifier = Modifier
						.fillMaxSize()
						.padding(12.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.SpaceBetween
				) {
					Grid(state)
					if (state.message != null) MessageBar(state.message!!)
				}
			}
		}
	}
}

@Composable
private fun Grid(state: GameState) {
	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		state.rows.forEachIndexed { index, row ->
			Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				val text = when {
					row.results != null -> row.guess
					index == state.rows.indexOfFirst { it.results == null } -> state.currentInput.padEnd(state.wordLength)
					else -> "".padEnd(state.wordLength)
				}

				for (i in 0 until state.wordLength) {
					val c = text.getOrNull(i)?.takeIf { it != ' ' }?.uppercase() ?: ""
					val bg = when (row.results?.getOrNull(i)) {
						LetterResult.CORRECT -> Color(0xFF6AAA64)
						LetterResult.PRESENT -> Color(0xFFC9B458)
						LetterResult.ABSENT, null -> Color(0xFF787C7E).copy(alpha = 0.25f)
					}
					Box(
						modifier = Modifier
							.size(48.dp)
							.background(bg)
							.border(1.dp, Color(0xFF3A3A3C)),
						contentAlignment = Alignment.Center
					) {
						Text(c, style = MaterialTheme.typography.titleLarge)
					}
				}
			}
		}
	}
}

@Composable
private fun MessageBar(message: String) {
	Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
		Text(
			text = message,
			modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
			textAlign = TextAlign.Center
		)
	}
}

@Composable
private fun Keyboard(onKey: (Char) -> Unit, onEnter: () -> Unit, onBackspace: () -> Unit, hints: Map<Char, LetterResult>) {
	Column(
		Modifier.fillMaxWidth().padding(8.dp),
		verticalArrangement = Arrangement.spacedBy(6.dp)
	) {
		val rows = listOf(
			"qwertyuiop",
			"asdfghjkl",
			"zxcvbnm"
		)
		rows.forEachIndexed { idx, r ->
			Row(
				Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally)
			) {
				if (idx == 2) {
					KeyboardKey("ENTER", 64.dp) { onEnter() }
				}
				r.forEach { ch ->
					val color = when (hints[ch]) {
						LetterResult.CORRECT -> Color(0xFF6AAA64)
						LetterResult.PRESENT -> Color(0xFFC9B458)
						LetterResult.ABSENT -> Color(0xFF3A3A3C)
						null -> Color(0xFF606062)
					}
					KeyboardKey(ch.uppercase(), 36.dp, color) { onKey(ch) }
				}
				if (idx == 2) {
					KeyboardKey("⌫", 64.dp) { onBackspace() }
				}
			}
		}
	}
}

@Composable
private fun RowScope.KeyboardKey(
	label: String,
	width: Dp,
	color: Color = Color(0xFF787C7E),
	onClick: () -> Unit
) {
	Button(
		onClick = onClick,
		modifier = Modifier
			.width(width)
			.height(48.dp),
		colors = ButtonDefaults.buttonColors(
			containerColor = color,
			contentColor = Color.White
		),
		contentPadding = PaddingValues(0.dp)
	) {
		Text(
			text = label,
			fontSize = 14.sp,
			fontWeight = FontWeight.Bold,
			textAlign = TextAlign.Center
		)
	}
}