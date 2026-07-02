package framework.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import framework.arena.RobotBrain

/**
 * Steuerleiste für Start/Pause/Reset, Geschwindigkeit und Teilnehmerauswahl.
 *
 * Reines State-Hoisting: alle Zustände werden von außen (App.kt) übergeben,
 * dieses Composable hält selbst keinen eigenen State.
 */
@Composable
fun Controls(
    availableBrains: List<RobotBrain>,
    selectedBrainIndices: Set<Int>,
    onSelectedBrainIndicesChange: (Set<Int>) -> Unit,
    isRunning: Boolean,
    onIsRunningChange: (Boolean) -> Unit,
    tickIntervalMs: Int,
    onTickIntervalMsChange: (Int) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canStart = selectedBrainIndices.size >= 2

    Column(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Text("Steuerung", style = androidx.compose.material.MaterialTheme.typography.h6)

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onIsRunningChange(!isRunning) },
                enabled = canStart || isRunning
            ) {
                Text(if (isRunning) "⏸ Pause" else "▶ Start")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onReset) {
                Text("Reset")
            }
        }

        if (!canStart) {
            Text("Bitte mindestens 2 Roboter auswählen, um zu starten.")
        }

        Text("Geschwindigkeit: ${tickIntervalMs}ms pro Tick")
        Slider(
            value = tickIntervalMs.toFloat(),
            onValueChange = { onTickIntervalMsChange(it.toInt()) },
            valueRange = 50f..1000f
        )

        Text("Teilnehmer:", style = androidx.compose.material.MaterialTheme.typography.subtitle1)
        availableBrains.forEachIndexed { index, brain ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = index in selectedBrainIndices,
                    onCheckedChange = { checked ->
                        val newSelection = if (checked) {
                            selectedBrainIndices + index
                        } else {
                            selectedBrainIndices - index
                        }
                        onSelectedBrainIndicesChange(newSelection)
                    },
                    enabled = !isRunning
                )
                Text(brain.name)
            }
        }
    }
}
