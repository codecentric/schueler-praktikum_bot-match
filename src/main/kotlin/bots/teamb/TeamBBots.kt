package bots.teamb

// Hier entsteht euer Team-Bot. Ihr dürft diese Datei erweitern oder weitere
// Bot-Klassen in diesem Package anlegen. Vergesst nicht, neue Bots unten in
// `teamBBots` einzutragen, damit sie im Spiel erscheinen.

import framework.arena.Action
import framework.arena.Direction
import framework.arena.RobotBrain
import framework.arena.Sensors

/** Startpunkt für euren eigenen Bot - benennt/erweitert diese Klasse nach Belieben. */
class MeinBot(override val name: String = "Team B - Mr. Nonchalant") : RobotBrain {
    override fun decide(sensors: Sensors): Action {
        sensors.self.position.y
        sensors.self.position.x
        sensors.self.position
        if (sensors.self.position.x in 1..4 ){
            return Action.Move(Direction.WEST)
        }

        else if (sensors.self.position.y in 1..4 ){
            return Action.Move(Direction.NORTH)
        }

        else if (sensors.self.position.x in 5..8 ){
            return Action.Move(Direction.EAST)
        }

        else if (sensors.self.position.y in 5..8 ){
            return Action.Move(Direction.SOUTH)
        }
        // TODO: Schieße wenn ein Gegner in Sichtlinie ist
        // TODO: Reagiere auf niedrige eigene HP (sensors.self.health), z.B. mit Flucht
        return Action.Move(Direction.entries.random())


    }
}

val teamBBots: List<RobotBrain> = listOf(MeinBot())
