package bots.examples

import framework.arena.Action
import framework.arena.Direction
import framework.arena.RobotBrain
import framework.arena.Sensors
import kotlin.random.Random

/**
 * Einfachster Beispiel-Bot: wählt bei jedem Tick zufällig, ob er sich bewegt
 * oder schießt, und wählt dabei jeweils eine zufällige Richtung.
 * Dient als Baseline-Gegner zum Testen anderer Bots.
 */
class RandomBot(override val name: String = "RandomBot") : RobotBrain {
    override fun decide(sensors: Sensors): Action {
        val direction = Direction.entries.random()
        return if (Random.nextBoolean()) {
            Action.Move(direction)
        } else {
            Action.Shoot(direction)
        }
    }
}
