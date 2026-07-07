package bots.teamb

import framework.arena.Action
import framework.arena.Direction
import framework.arena.Position
import framework.arena.RobotBrain
import framework.arena.RobotState
import framework.arena.Sensors
import kotlin.math.abs

class MeinBot(override val name: String = "Team B - Mr. Nonchalant") : RobotBrain {

    override fun decide(sensors: Sensors): Action {
        val naechster = this.naechsterGegner(sensors, sensors.self.position)

        val dx = naechster.position.x - sensors.self.position.x
        val dy = naechster.position.y - sensors.self.position.y

        if (sensors.self.health < 21) {
            if (abs(dx) > abs(dy)) {
                return Action.Move(if (dx > 0) Direction.WEST else Direction.EAST)
            } else {
                return Action.Move(if (dy > 0) Direction.NORTH else Direction.SOUTH)
            }
        }
        else if (dy == 0) {
            return Action.Shoot(if (dx > 0) Direction.EAST else Direction.WEST)
        }
        else if (dx == 0) {
            return Action.Shoot(if (dy > 0) Direction.SOUTH else Direction.NORTH)
        }
        else {
            return Action.Move(Direction.entries.random())
        }
    }

    fun naechsterGegner(sensors: Sensors, selfPosition: Position): RobotState {
        var naechster = sensors.others[0]
        var kleinsterAbstand = abs(naechster.position.x - selfPosition.x) + abs(naechster.position.y - selfPosition.y)
        for (gegner in sensors.others) {
            val abstand = abs(gegner.position.x - selfPosition.x) + abs(gegner.position.y - selfPosition.y)
            if (abstand < kleinsterAbstand) {
                kleinsterAbstand = abstand
                naechster = gegner
            }
        }
        return naechster
    }
}

val teamBBots: List<RobotBrain> = listOf(MeinBot())