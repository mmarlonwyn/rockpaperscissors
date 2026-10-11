package com.example.rockpaperscissors

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

const val N = 5
const val TARGET = N / 2 + 1          //bwt menange

enum class Rps(val emoji: String, val label: String) {
    ROCK("✊", "Rock"),
    PAPER("✋", "Paper"),
    SCISSOR("✌️", "Scissor");

    fun beats(other: Rps): Boolean =
        (this == ROCK && other == SCISSOR) ||
                (this == PAPER && other == ROCK) ||
                (this == SCISSOR && other == PAPER)
}

enum class State { INITIAL, PICK, REVEAL, FINISHED }

enum class Result(val text: String) {
    WIN("You Win!"), LOSE("You Lose"), DRAW("Draw")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme { RpsApp() }
        }
    }
}

@Composable
fun RpsApp() {
    var state by rememberSaveable { mutableStateOf(State.INITIAL) }
    var yourScore by rememberSaveable { mutableIntStateOf(0) }
    var cpuScore by rememberSaveable { mutableIntStateOf(0) }
    var bestScore by rememberSaveable { mutableIntStateOf(0) }   // selisih terbaik
    var playerPick by rememberSaveable { mutableStateOf<Rps?>(null) }
    var cpuPick by rememberSaveable { mutableStateOf<Rps?>(null) }
    var result by rememberSaveable { mutableStateOf<Result?>(null) }
    var order by remember { mutableStateOf(Rps.values().toList().shuffled()) }

   //700ms lnjt
    LaunchedEffect(state) {
        if (state == State.REVEAL) {
            delay(700)
            if (yourScore >= TARGET || cpuScore >= TARGET) {
                bestScore = maxOf(bestScore, yourScore - cpuScore)
                state = State.FINISHED
            } else {
                playerPick = null       // reset state per ronde
                cpuPick = null
                result = null
                order = Rps.values().toList().shuffled()
                state = State.PICK
            }
        }
    }

    fun pick(p: Rps) {
        val c = Rps.values().random()
        val r = when {
            p == c -> Result.DRAW
            p.beats(c) -> Result.WIN
            else -> Result.LOSE
        }
        playerPick = p
        cpuPick = c
        result = r
        if (r == Result.WIN) yourScore++
        if (r == Result.LOSE) cpuScore++
        state = State.REVEAL
    }

    fun restart() {
        yourScore = 0
        cpuScore = 0
        playerPick = null
        cpuPick = null
        result = null
        order = Rps.values().toList().shuffled()
        state = State.PICK
    }

    fun exit() {
        yourScore = 0
        cpuScore = 0
        playerPick = null
        cpuPick = null
        result = null
        state = State.INITIAL
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Score: you $yourScore, computer $cpuScore" },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("🧑 $yourScore — $cpuScore 🤖", fontSize = 16.sp)
            Text("Best of $N", fontSize = 14.sp)
        }
        Spacer(Modifier.height(48.dp))

        when (state) {
            State.INITIAL -> {
                Text("Rock • Paper • Scissors", fontSize = 24.sp)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { restart() }) { Text("Start") }
            }

            State.PICK -> {
                Text("Pick your move!", fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                Text("❔ VS ❔", fontSize = 36.sp)
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    order.forEach { move ->
                        Button(
                            onClick = { pick(move) },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { contentDescription = "Pick ${move.label}" },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 12.dp)
                        ) {
                            Text("${move.emoji} ${move.label}", fontSize = 13.sp, maxLines = 1)
                        }
                    }
                }
            }

            State.REVEAL -> {
                Text("${playerPick?.emoji} VS ${cpuPick?.emoji}", fontSize = 40.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    result?.text ?: "",
                    fontSize = 18.sp,
                    modifier = Modifier.semantics { contentDescription = "Round result: ${result?.text}" }
                )
            }

            State.FINISHED -> {
                val win = yourScore > cpuScore
                Text(
                    if (win) "You Win the Match!" else "You Lose the Match",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics {
                        contentDescription = if (win) "You win the match" else "You lose the match"
                    }
                )
                Spacer(Modifier.height(8.dp))
                Text("Final: 🧑 $yourScore - $cpuScore 🤖", fontSize = 16.sp)
                Text("Best Score: $bestScore", fontSize = 14.sp)
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { restart() }) { Text("Restart") }
                    Button(onClick = { exit() }) { Text("Exit") }
                }
            }
        }
    }
}