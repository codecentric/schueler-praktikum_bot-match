package framework.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import framework.arena.Action
import framework.arena.Direction
import framework.arena.GameEngine
import framework.arena.MatchStatus
import framework.arena.RobotBrain
import framework.arena.RobotState
import framework.arena.Sensors
import framework.arena.ShotEvent
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------------
// Fallback-Test-Bots: NUR zum Smoke-Testen der UI, bevor die "echten" Bots aus
// BotRegistry verfügbar sind. Werden in App() nur verwendet, falls die von
// außen übergebene availableBrains-Liste weniger als 2 Einträge enthält.
// ---------------------------------------------------------------------------

/** Bewegt sich jeden Tick in eine zufällige Richtung. */
private class RandomWalkerTestBrain : RobotBrain {
    override val name = "TestBot-Random"
    override fun decide(sensors: Sensors): Action {
        return Action.Move(Direction.entries.random())
    }
}

/** Schießt jeden Tick stur nach Osten, bewegt sich nie. */
private class FixedShooterTestBrain : RobotBrain {
    override val name = "TestBot-Schütze"
    override fun decide(sensors: Sensors): Action {
        return Action.Shoot(Direction.EAST)
    }
}

/** Verfolgt den nächstgelegenen gegnerischen Roboter und schießt, wenn möglich in dessen Richtung. */
private class ChaserTestBrain : RobotBrain {
    override val name = "TestBot-Jäger"
    override fun decide(sensors: Sensors): Action {
        val target = sensors.others.minByOrNull {
            kotlin.math.abs(it.position.x - sensors.self.position.x) +
                kotlin.math.abs(it.position.y - sensors.self.position.y)
        } ?: return Action.Wait

        val dx = target.position.x - sensors.self.position.x
        val dy = target.position.y - sensors.self.position.y

        // Falls in gleicher Reihe/Spalte, direkt schießen, sonst näherkommen.
        return when {
            dx == 0 && dy < 0 -> Action.Shoot(Direction.NORTH)
            dx == 0 && dy > 0 -> Action.Shoot(Direction.SOUTH)
            dy == 0 && dx > 0 -> Action.Shoot(Direction.EAST)
            dy == 0 && dx < 0 -> Action.Shoot(Direction.WEST)
            kotlin.math.abs(dx) > kotlin.math.abs(dy) -> Action.Move(if (dx > 0) Direction.EAST else Direction.WEST)
            else -> Action.Move(if (dy > 0) Direction.SOUTH else Direction.NORTH)
        }
    }
}

/** Liefert 3 einfache Fallback-Bots für den Smoke-Test der UI. */
private fun fallbackTestBrains(): List<RobotBrain> = listOf(
    RandomWalkerTestBrain(),
    FixedShooterTestBrain(),
    ChaserTestBrain()
)

/**
 * Baut den Sieg-Eintrag fürs Log als ASCII-Art-Banner, damit der Gewinner
 * zwischen den vielen Tick-Zeilen sofort ins Auge fällt statt in einer
 * normalen Textzeile unterzugehen. Ein einziger mehrzeiliger Log-Eintrag
 * (nicht mehrere einzelne), damit LogPanel ihn als zusammenhängenden Block
 * an einer Position anzeigt statt die Zeilen einzeln umzusortieren.
 */
private fun winBanner(winnerName: String, ticksPlayed: Int): String {
    val text = "$winnerName GEWINNT!"
    val border = "*".repeat(text.length + 8)
    return """
        |$border
        |*   $text   *
        |$border
        |Match beendet nach $ticksPlayed Ticks.
    """.trimMargin()
}

/**
 * Einstiegspunkt der UI. [availableBrains] wird von außen (Main.kt, später via
 * BotRegistry) übergeben. Falls davon weniger als 2 Einträge kommen (z.B.
 * solange die echten Bots noch nicht fertig sind), greifen wir auf 3 einfache
 * eigene Test-Bots zurück, damit die App trotzdem sofort startbar/testbar ist.
 */
@Composable
fun App(availableBrains: List<RobotBrain>) {
    val brains = remember(availableBrains) {
        if (availableBrains.size < 2) fallbackTestBrains() else availableBrains
    }
    val clipboardManager = LocalClipboardManager.current

    var selectedBrainIndices by remember { mutableStateOf(brains.indices.toSet()) }
    var tickIntervalMs by remember { mutableStateOf(300) }
    var isRunning by remember { mutableStateOf(false) }
    // Wird bei jedem Reset erhöht, damit der laufende LaunchedEffect der
    // Tick-Schleife sauber abgebrochen und mit frischem Zustand neu gestartet
    // wird, statt mit stale State (alte Engine-Referenz) weiterzulaufen.
    var matchGeneration by remember { mutableStateOf(0) }

    val engine = remember(matchGeneration) { GameEngine() }
    var robots by remember(matchGeneration) { mutableStateOf<List<RobotState>>(emptyList()) }
    var shots by remember(matchGeneration) { mutableStateOf<List<ShotEvent>>(emptyList()) }
    var logEntries by remember(matchGeneration) { mutableStateOf<List<String>>(emptyList()) }
    var winnerId by remember(matchGeneration) { mutableStateOf<String?>(null) }

    // Startet das Match einmalig für jede Engine-Generation mit den aktuell
    // ausgewählten Brains.
    LaunchedEffect(matchGeneration) {
        val selected = selectedBrainIndices.sorted().map { brains[it] }
        engine.startMatch(selected)
        robots = engine.currentStates()
    }

    // Die eigentliche Tick-Schleife: läuft solange isRunning true ist und das
    // Match noch nicht beendet wurde. matchGeneration ist Teil des Keys, damit
    // ein Reset diese Coroutine sauber neu startet statt mit alter Engine
    // weiterzumachen.
    LaunchedEffect(isRunning, tickIntervalMs, matchGeneration) {
        // Schuss-Linien werden am Ende jedes Ticks kurz ausgeblendet (blankMs), damit
        // bei mehreren Schüssen in Folge in dieselbe Richtung sichtbar bleibt, dass es
        // pro Tick ein neuer Schuss ist, statt einer durchgehenden Linie.
        val blankMs = (tickIntervalMs / 3).coerceAtMost(80)
        while (isRunning && engine.currentStatus() == MatchStatus.RUNNING) {
            delay((tickIntervalMs - blankMs).toLong())
            val status = engine.step(onLog = { message ->
                logEntries = (logEntries + message).takeLast(500)
            })
            robots = engine.currentStates()
            shots = engine.lastShots()
            if (shots.isNotEmpty()) SoundPlayer.playShot()
            if (status == MatchStatus.FINISHED) {
                isRunning = false
                val result = engine.result()
                val summary = when {
                    result == null -> "Match beendet."
                    result.isDraw -> "Match beendet nach ${result.ticksPlayed} Ticks: Unentschieden."
                    else -> {
                        val winnerState = result.finalStates.find { it.id == result.winnerId }
                        winnerId = result.winnerId
                        winBanner(winnerState?.teamName ?: result.winnerId ?: "?", result.ticksPlayed)
                    }
                }
                logEntries = (logEntries + summary).takeLast(500)
            }
            delay(blankMs.toLong())
            shots = emptyList()
        }
    }

    DisposableEffect(engine) {
        onDispose { engine.shutdown() }
    }

    Row(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        ArenaCanvas(
            robots = robots,
            arenaWidth = 10,
            arenaHeight = 10,
            shots = shots,
            winnerId = winnerId,
            // fillMaxHeight().aspectRatio(1f) statt weight(1f): das Quadrat bekommt so
            // exakt seine eigene Breite (an der Höhe orientiert) statt eines vollen
            // weight-Slots, dessen leerer Rest sonst als große Lücke vor der
            // Steuerungsspalte sichtbar wäre.
            modifier = Modifier.fillMaxHeight().aspectRatio(1f)
        )

        Spacer(modifier = Modifier.width(48.dp))

        Column(
            modifier = Modifier.width(360.dp).fillMaxHeight().padding(end = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            ) {
                Controls(
                    availableBrains = brains,
                    selectedBrainIndices = selectedBrainIndices,
                    onSelectedBrainIndicesChange = { selectedBrainIndices = it },
                    isRunning = isRunning,
                    onIsRunningChange = { isRunning = it },
                    tickIntervalMs = tickIntervalMs,
                    onTickIntervalMsChange = { tickIntervalMs = it },
                    onReset = {
                        isRunning = false
                        matchGeneration++
                    },
                    onCopyLog = { clipboardManager.setText(AnnotatedString(logEntries.joinToString("\n"))) }
                )
                Scoreboard(
                    robots = robots,
                    winnerId = winnerId,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            LogPanel(
                logEntries = logEntries,
                modifier = Modifier.weight(1f).padding(top = 12.dp)
            )
        }
    }
}
