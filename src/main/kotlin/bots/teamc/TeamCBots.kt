package bots.teamc

// Hier entsteht euer Team-Bot. Ihr dürft diese Datei erweitern oder weitere
// Bot-Klassen in diesem Package anlegen. Vergesst nicht, neue Bots unten in
// `teamCBots` einzutragen, damit sie im Spiel erscheinen.

import framework.arena.Action
import framework.arena.Direction
import framework.arena.RobotBrain
import framework.arena.Sensors
import org.jetbrains.skiko.setupSkikoLoggerFactory

/** Startpunkt für euren eigenen Bot - benennt/erweitert diese Klasse nach Belieben. */
class MeinBot(override val name: String = "Team C - MeinBot") : RobotBrain {
    override fun decide(sensors: Sensors): Action {


        val gegner = sensors.others.get(0)
        println(gegner.teamName)
        if (gegner.position.y == sensors.self.position.y) {

            if (gegner.position.x < sensors.self.position.x) {
                return Action.Shoot(Direction.WEST)
            } else if (gegner.position.x > sensors.self.position.x) {
                return Action.Shoot(Direction.EAST)
            }
            if (gegner.position.x == sensors.self.position.x)

                if (gegner.position.y < sensors.self.position.y) {
                    return Action.Shoot(Direction.NORTH)
                } else if (gegner.position.y > sensors.self.position.y) {
                    return Action.Shoot(Direction.SOUTH)

                }
            if (gegner.position.y != sensors.self.position.y) {
                println("North")
                if (gegner.position.y < sensors.self.position.y) {
                    return Action.Move(Direction.NORTH)
                }
                if (gegner.position.y > sensors.self.position.y) {
                    println("South")
                    return Action.Move(Direction.SOUTH)
                }

            } else if (gegner.position.x != sensors.self.position.x) {
                return Action.Move(Direction.EAST)
            } else if (gegner.position.x != sensors.self.position.x) {
                return Action.Move(Direction.WEST)
            }
        return TODO("Provide the return value")
    }
    return TODO("Provide the return value")
}

}

val teamCBots: List<RobotBrain> = listOf(MeinBot())




