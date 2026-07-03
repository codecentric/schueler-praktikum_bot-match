package framework.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import framework.arena.Direction
import framework.arena.RobotState
import framework.arena.ShotEvent
import kotlin.math.min

/**
 * Kürzel für die Beschriftung im Roboter-Kreis: bei "Team X - ..." der Buchstabe X,
 * sonst die Großbuchstaben aus dem CamelCase-Namen (z.B. "StillstandBot" -> "SB").
 */
private fun labelFor(teamName: String): String {
    val teamMatch = Regex("""^Team\s+([A-Za-z])\s*-""").find(teamName)
    if (teamMatch != null) return teamMatch.groupValues[1].uppercase()
    val upper = teamName.filter { it.isUpperCase() }
    return if (upper.length >= 2) upper.take(2) else teamName.take(2).uppercase()
}

/**
 * Feste Farbzuordnung für Spieler-Bots: Team A = Rot, Team B = Blau, Team C = Grün.
 * Beispiel-Bots des Dozenten (Namen ohne "Team X - "-Präfix) bekommen alle dieselbe
 * neutrale Farbe, um sie klar von den Schüler-Bots zu unterscheiden.
 */
private val TEAM_LETTER_COLORS = mapOf(
    "A" to Color.Red,
    "B" to Color.Blue,
    "C" to Color(0xFF2E7D32), // dunkelgrün, besser lesbar als reines Color.Green
)

private val EXAMPLE_BOT_COLOR = Color(0xFF757575) // neutrales Grau für alle Beispiel-Bots

private fun isExampleBot(teamName: String): Boolean = !teamName.startsWith("Team ")

private fun colorForTeam(teamName: String): Color {
    if (isExampleBot(teamName)) return EXAMPLE_BOT_COLOR
    val letter = Regex("""^Team\s+([A-Za-z])""").find(teamName)?.groupValues?.get(1)?.uppercase()
    return TEAM_LETTER_COLORS[letter] ?: Color.Gray
}

/**
 * Zeichnet das Arena-Raster inklusive aller Roboter.
 * Lebende Roboter: farbiger Kreis (Farbe nach Team) mit Healthbar darüber.
 * Tote Roboter: ausgegrauter Kreis mit X-Markierung, keine Healthbar.
 */
@Composable
fun ArenaCanvas(
    robots: List<RobotState>,
    arenaWidth: Int,
    arenaHeight: Int,
    shots: List<ShotEvent> = emptyList(),
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // BoxWithConstraints statt aspectRatio(): aspectRatio erzwingt ein Quadrat nach
    // der Breite und läuft über den verfügbaren Platz hinaus, sobald die Breite größer
    // als die Höhe ist (z.B. breites Fenster). Hier wird stattdessen die tatsächlich
    // verfügbare Breite/Höhe gemessen und das größtmögliche Quadrat gewählt, das noch
    // hineinpasst.
    BoxWithConstraints(modifier = modifier) {
        val squareSize = maxWidth.coerceAtMost(maxHeight)
        Canvas(modifier = Modifier.size(squareSize)) {
            val cellWidth = size.width / arenaWidth
            val cellHeight = size.height / arenaHeight
            val gridColor = Color(0xFFDDDDDD)

        // Eigener Arena-Hintergrund, damit sich das Spielfeld optisch vom Rest der App abhebt.
        drawRect(color = Color(0xFF1B2A1F), size = size)

        // Grid-Linien
        for (x in 0..arenaWidth) {
            val px = x * cellWidth
            drawLine(gridColor, Offset(px, 0f), Offset(px, size.height), strokeWidth = 1f)
        }
        for (y in 0..arenaHeight) {
            val py = y * cellHeight
            drawLine(gridColor, Offset(0f, py), Offset(size.width, py), strokeWidth = 1f)
        }

        val radius = min(cellWidth, cellHeight) * 0.35f

        // Schuss-Strahlen: Linie vom Schützen bis zum getroffenen Bot bzw. Arena-Rand
        // (falls kein Treffer). Bei Treffer zusätzlich eine kleine Explosion im Bot-Zentrum.
        val shotColor = Color(0xFFFFD54F)
        val explosionColor = Color(0xFFFF5722)
        val shotStrokeWidth = min(cellWidth, cellHeight) * 0.03f
        for (shot in shots) {
            val startX = (shot.fromPosition.x + 0.5f) * cellWidth
            val startY = (shot.fromPosition.y + 0.5f) * cellHeight
            val targetX = (shot.toPosition.x + 0.5f) * cellWidth
            val targetY = (shot.toPosition.y + 0.5f) * cellHeight

            // Bei Treffer vor dem Ziel-Kreisrand stoppen, sonst verdeckt der Bot-Kreis
            // (der danach gezeichnet wird) die Linie zur Explosion. Bei Fehlschuss
            // bis zur echten Arena-Wand zeichnen, nicht nur bis zur Zellenmitte.
            val (endX, endY) = when {
                shot.hitBot && shot.direction == Direction.NORTH -> targetX to targetY + radius
                shot.hitBot && shot.direction == Direction.SOUTH -> targetX to targetY - radius
                shot.hitBot && shot.direction == Direction.EAST -> targetX - radius to targetY
                shot.hitBot && shot.direction == Direction.WEST -> targetX + radius to targetY
                shot.direction == Direction.NORTH -> targetX to 0f
                shot.direction == Direction.SOUTH -> targetX to size.height
                shot.direction == Direction.EAST -> size.width to targetY
                else -> 0f to targetY
            }

            drawLine(
                color = shotColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = shotStrokeWidth,
                cap = StrokeCap.Round
            )
        }

        for (robot in robots) {
            val centerX = (robot.position.x + 0.5f) * cellWidth
            val centerY = (robot.position.y + 0.5f) * cellHeight
            val center = Offset(centerX, centerY)

            if (robot.alive) {
                val color = colorForTeam(robot.teamName)
                drawCircle(color = color, radius = radius, center = center)

                val label = labelFor(robot.teamName)
                val textLayout = textMeasurer.measure(
                    label,
                    style = TextStyle(color = Color.White, fontSize = (radius * 0.9f).toSp(), fontWeight = FontWeight.Bold)
                )
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(centerX - textLayout.size.width / 2f, centerY - textLayout.size.height / 2f)
                )

                // Healthbar direkt über dem Roboter: grün -> rot je nach HP-Anteil.
                val healthFraction = (robot.health / 100f).coerceIn(0f, 1f)
                val barWidth = cellWidth * 0.7f
                val barHeight = cellHeight * 0.1f
                val barLeft = centerX - barWidth / 2f
                val barTop = centerY - radius - barHeight - 3f

                // Hintergrund (dunkles Rot = "leer")
                drawRect(
                    color = Color(0xFF7A1414),
                    topLeft = Offset(barLeft, barTop),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                )
                // Vordergrund (grün = aktuelle HP)
                drawRect(
                    color = Color(0xFF2E7D32),
                    topLeft = Offset(barLeft, barTop),
                    size = androidx.compose.ui.geometry.Size(barWidth * healthFraction, barHeight)
                )
                // Dünner dunkler Rahmen um die gesamte Leiste für einen klaren Abschluss.
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(barLeft, barTop),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                    style = Stroke(width = 1f)
                )
            } else {
                // Toter Roboter: grauer, ausgegrauter Kreis mit X-Symbol.
                val grayColor = Color(0xFF9E9E9E)
                drawCircle(color = grayColor, radius = radius, center = center)
                val xExtent = radius * 0.6f
                val stroke = Stroke(width = radius * 0.25f, cap = StrokeCap.Round)
                drawLine(
                    color = Color.DarkGray,
                    start = Offset(centerX - xExtent, centerY - xExtent),
                    end = Offset(centerX + xExtent, centerY + xExtent),
                    strokeWidth = stroke.width,
                    cap = stroke.cap
                )
                drawLine(
                    color = Color.DarkGray,
                    start = Offset(centerX - xExtent, centerY + xExtent),
                    end = Offset(centerX + xExtent, centerY - xExtent),
                    strokeWidth = stroke.width,
                    cap = stroke.cap
                )
            }
        }

        // Explosion NACH den Bots gezeichnet, damit sie über dem getroffenen Bot
        // sichtbar bleibt statt vom Bot-Kreis verdeckt zu werden.
        val explosionRayCount = 8
        val explosionInnerRadius = radius * 0.3f
        val explosionOuterRadius = radius * 0.75f
        for (shot in shots.filter { it.hitBot }) {
            val centerX = (shot.toPosition.x + 0.5f) * cellWidth
            val centerY = (shot.toPosition.y + 0.5f) * cellHeight
            for (i in 0 until explosionRayCount) {
                val angle = 2 * Math.PI * i / explosionRayCount
                val cos = kotlin.math.cos(angle).toFloat()
                val sin = kotlin.math.sin(angle).toFloat()
                drawLine(
                    color = explosionColor,
                    start = Offset(centerX + explosionInnerRadius * cos, centerY + explosionInnerRadius * sin),
                    end = Offset(centerX + explosionOuterRadius * cos, centerY + explosionOuterRadius * sin),
                    strokeWidth = shotStrokeWidth * 1.5f,
                    cap = StrokeCap.Round
                )
            }
        }
        }
    }
}
