package framework.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import framework.arena.RobotState

/**
 * Übersichtliche Tabelle aller Roboter: Team, ID, aktuelle HP und Status.
 * Sortierung: lebende Roboter zuerst (nach HP absteigend), zerstörte Roboter danach.
 *
 * Bewusst eine normale Column statt LazyColumn: bei nur wenigen Robotern (max. ~7-8)
 * würde eine LazyColumn ohne Höhenbegrenzung standardmäßig die gesamte verfügbare
 * Höhe beanspruchen und dem darunterliegenden LogPanel (weight(1f)) den Platz wegnehmen.
 */
@Composable
fun Scoreboard(robots: List<RobotState>, winnerId: String? = null, modifier: Modifier = Modifier) {
    val sorted = robots.sortedWith(compareByDescending<RobotState> { it.alive }.thenByDescending { it.health })

    Column(modifier = modifier) {
        Text("Scoreboard", fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text("Team", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
            Text("HP", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text("Status", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
        }
        Divider()
        for (robot in sorted) {
            val isWinner = robot.id == winnerId
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                val color = if (isWinner) Color.Red else Color.Unspecified
                val weight = if (isWinner) FontWeight.Bold else FontWeight.Normal
                Text("${robot.teamName} (${robot.id})", modifier = Modifier.weight(2f), color = color, fontWeight = weight)
                Text("${robot.health}", modifier = Modifier.weight(1f), color = color, fontWeight = weight)
                Text(if (robot.alive) "Lebt" else "Zerstört", modifier = Modifier.weight(1.5f), color = color, fontWeight = weight)
            }
        }
    }
}
