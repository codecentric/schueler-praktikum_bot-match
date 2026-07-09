package bots.teamb

import framework.arena.Action
import framework.arena.Direction
import framework.arena.Position
import framework.arena.RobotBrain
import framework.arena.Sensors
import kotlin.math.abs

class MeinBot(override val name: String = "Team B - Mr. Nonchalant") : RobotBrain {

    override fun decide(sensors: Sensors): Action {

        // Gegner-Radar
        val self = sensors.self.position
        for (gegner in sensors.others) {
            val naechster = gegner.position
            if (self.x == naechster.x && self.y == 0 && self.x == 0) {
                return Action.Move(Direction.EAST)
            }
        }

        // Die neue Aufgabe von Mr Nonchalant ist es, zum Punkt Z(0 | 0) zu gehen
        val dx = 0 - sensors.self.position.x
        val dy = 0 - sensors.self.position.y

        // Prüft, ob schon anwesend bei Z
        if (dx == 0 && dy == 0) {
            return Action.Shoot(Direction.SOUTH) // Wartet/Schießt, wenn angekommen
        }

        // bestimmt, wie der bot sich bewegen muss, um schnellstmöglich an den Punkt Z zu gelangen
        if (abs(dx) >= abs(dy)) {
            return Action.Move(if (dx > 0) Direction.EAST else Direction.WEST)
        } else {
            return Action.Move(if (dy > 0) Direction.SOUTH else Direction.NORTH)
        }
    }
}

val teamBBots: List<RobotBrain> = listOf(MeinBot())