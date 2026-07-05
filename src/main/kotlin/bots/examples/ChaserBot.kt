package bots.examples

import framework.arena.Action
import framework.arena.Direction
import framework.arena.RobotBrain
import framework.arena.RobotState
import framework.arena.Sensors
import kotlin.math.abs

/**
 * Findet den nächsten lebenden Gegner (kleinste Manhattan-Distanz) und greift
 * ihn an: steht er bereits in exakt gleicher Reihe/Spalte (ein Schuss würde
 * treffen), wird geschossen - sonst nähert sich der Bot ihm Schritt für
 * Schritt an (Achse mit größerem Abstand zuerst, für diagonale Annäherung
 * über mehrere Ticks).
 */
class ChaserBot(override val name: String = "ChaserBot") : RobotBrain {
    /**
     * Greift den nächsten Gegner an: schießt sofort, wenn er in gleicher Reihe/Spalte
     * steht, sonst wird die Achse mit dem größeren Abstand zuerst angeglichen.
     *
     * @param sensors aktueller Wahrnehmungszustand (eigene Position, lebende Gegner).
     * @return [Action.Wait] wenn kein Gegner mehr lebt, sonst [Action.Shoot] bei
     *   exakter Ausrichtung oder [Action.Move] zur Annäherung.
     */
    override fun decide(sensors: Sensors): Action {
        val target = findNearestEnemy(sensors) ?: return Action.Wait

        val dx = target.position.x - sensors.self.position.x
        val dy = target.position.y - sensors.self.position.y

        return when {
            dx == 0 && dy < 0 -> Action.Shoot(Direction.NORTH)
            dx == 0 && dy > 0 -> Action.Shoot(Direction.SOUTH)
            dy == 0 && dx > 0 -> Action.Shoot(Direction.EAST)
            dy == 0 && dx < 0 -> Action.Shoot(Direction.WEST)
            abs(dx) >= abs(dy) -> Action.Move(if (dx > 0) Direction.EAST else Direction.WEST)
            else -> Action.Move(if (dy > 0) Direction.SOUTH else Direction.NORTH)
        }
    }

    /**
     * Sucht unter allen lebenden Gegnern denjenigen mit kleinster Manhattan-Distanz
     * ([abs]`(dx) + `[abs]`(dy)`, passend zum Grid ohne Diagonalbewegung).
     *
     * @param sensors liefert [Sensors.self] (Ausgangspunkt) und [Sensors.others] (Kandidaten).
     * @return nächstgelegener [RobotState] oder `null`, wenn kein Gegner mehr lebt.
     */
    private fun findNearestEnemy(sensors: Sensors): RobotState? {
        if (sensors.others.isEmpty()) return null
        return sensors.others.minByOrNull { other ->
            abs(other.position.x - sensors.self.position.x) +
                abs(other.position.y - sensors.self.position.y)
        }
    }
}
