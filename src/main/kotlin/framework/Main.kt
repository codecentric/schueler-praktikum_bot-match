package framework

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import framework.arena.BotRegistry
import framework.ui.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Bot-Match",
        // Breite so gewählt, dass bei Fensterhöhe 900dp die Steuerungsspalte (fix 360dp
        // + Abstand 48dp) neben dem quadratischen Arena-Canvas (Seitenlänge = verfügbare
        // Höhe) noch vollständig sichtbar ist - sonst wird die Spalte rechts abgeschnitten.
        state = WindowState(width = 1320.dp, height = 900.dp)
    ) {
        App(availableBrains = BotRegistry.allBots())
    }
}
