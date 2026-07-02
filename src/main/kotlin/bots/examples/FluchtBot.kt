package bots.examples

import framework.arena.Action
import framework.arena.Direction
import framework.arena.RobotBrain
import framework.arena.RobotState
import framework.arena.Sensors
import kotlin.math.abs

/**
 * Solange die eigenen HP bei 20 oder mehr liegen, verhält sich dieser Bot wie
 * [ChaserBot] (greift den nächsten Gegner an). Sobald die HP unter 20 fallen,
 * flüchtet er stattdessen: er wählt die Bewegungsrichtung, die den Abstand
 * zum nächsten Gegner am stärksten vergrößert. Landet er dabei an einer Wand,
 * ist das kein Problem - die Engine blockt ungültige Bewegungen ohnehin ab.
 */
class FluchtBot(override val name: String = "FluchtBot") : RobotBrain {

    private companion object {
        const val FLEE_HEALTH_THRESHOLD = 20
    }

    override fun decide(sensors: Sensors): Action {
        val target = findNearestEnemy(sensors) ?: return Action.Wait

        return if (sensors.self.health < FLEE_HEALTH_THRESHOLD) {
            fleeFrom(sensors, target)
        } else {
            attack(sensors, target)
        }
    }

    private fun findNearestEnemy(sensors: Sensors): RobotState? {
        if (sensors.others.isEmpty()) return null
        return sensors.others.minByOrNull { other ->
            abs(other.position.x - sensors.self.position.x) +
                abs(other.position.y - sensors.self.position.y)
        }
    }

    private fun attack(sensors: Sensors, target: RobotState): Action {
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

    /** Wählt die Richtung, die den Manhattan-Abstand zu [target] am meisten vergrößert. */
    private fun fleeFrom(sensors: Sensors, target: RobotState): Action {
        val self = sensors.self.position
        val currentDistance = abs(target.position.x - self.x) + abs(target.position.y - self.y)

        val bestDirection = Direction.entries.maxByOrNull { direction ->
            val moved = self.moved(direction)
            abs(target.position.x - moved.x) + abs(target.position.y - moved.y)
        }

        return if (bestDirection != null) {
            val moved = self.moved(bestDirection)
            val newDistance = abs(target.position.x - moved.x) + abs(target.position.y - moved.y)
            if (newDistance > currentDistance) Action.Move(bestDirection) else Action.Wait
        } else {
            Action.Wait
        }
    }
}
