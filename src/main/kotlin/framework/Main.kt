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
        state = WindowState(width = 1400.dp, height = 900.dp)
    ) {
        App(availableBrains = BotRegistry.allBots())
    }
}
