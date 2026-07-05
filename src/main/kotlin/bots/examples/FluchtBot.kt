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

    /**
     * Greift den nächsten Gegner an, solange die eigenen HP über [FLEE_HEALTH_THRESHOLD]
     * liegen, sonst flüchtet er von ihm weg.
     *
     * @param sensors aktueller Wahrnehmungszustand (eigene HP/Position, lebende Gegner).
     * @return [Action.Wait] wenn kein Gegner mehr lebt, sonst je nach eigener HP
     *   Angriffs- oder Fluchtaktion.
     */
    override fun decide(sensors: Sensors): Action {
        val target = findNearestEnemy(sensors) ?: return Action.Wait

        return if (sensors.self.health < FLEE_HEALTH_THRESHOLD) {
            fleeFrom(sensors, target)
        } else {
            attack(sensors, target)
        }
    }

    /**
     * Sucht unter allen lebenden Gegnern denjenigen mit kleinster Manhattan-Distanz.
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

    /**
     * Schießt bei exakter Ausrichtung auf [target], sonst Bewegung zur Annäherung
     * (Achse mit größerem Abstand zuerst) - identische Logik wie [ChaserBot.decide].
     *
     * @param sensors liefert die eigene Position.
     * @param target das anzugreifende Ziel.
     * @return [Action.Shoot] bei Sichtlinie, sonst [Action.Move] zur Annäherung.
     */
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

    /**
     * Testet alle vier [Direction]-Werte durch und wählt die, die den simulierten
     * Abstand zu [target] am meisten vergrößert ([Direction.entries]`.maxByOrNull`),
     * statt nur die Achse mit größerem Ausschlag zu betrachten. Bricht sicher ab
     * (kein Move), wenn sich der Abstand durch keine Richtung verbessern lässt
     * (z.B. Ecke der Arena).
     *
     * @param sensors liefert die eigene Position.
     * @param target der Gegner, von dem geflohen wird.
     * @return [Action.Move] in die beste Fluchtrichtung, sonst [Action.Wait].
     */
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
