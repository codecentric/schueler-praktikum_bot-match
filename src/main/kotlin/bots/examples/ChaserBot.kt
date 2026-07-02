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

    private fun findNearestEnemy(sensors: Sensors): RobotState? {
        if (sensors.others.isEmpty()) return null
        return sensors.others.minByOrNull { other ->
            abs(other.position.x - sensors.self.position.x) +
                abs(other.position.y - sensors.self.position.y)
        }
    }
}
