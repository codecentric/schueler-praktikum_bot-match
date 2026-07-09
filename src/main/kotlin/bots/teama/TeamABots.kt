package bots.teama

// Hier entsteht euer Team-Bot. Ihr dürft diese Datei erweitern oder weitere
// Bot-Klassen in diesem Package anlegen. Vergesst nicht, neue Bots unten in
// `teamABots` einzutragen, damit sie im Spiel erscheinen.

import framework.arena.Action
import framework.arena.Direction
import framework.arena.RobotBrain
import framework.arena.Sensors
import kotlin.math.abs

/** Startpunkt für euren eigenen Bot - benennt/erweitert diese Klasse nach Belieben. */
class MeinBot(override val name: String = "Team A - SkibidiTerminator") : RobotBrain {

    override fun decide(sensors: Sensors): Action {
        if (istGegnerInZiellinie(sensors)) {
            return schießAufGegner(sensors)
        } else {
            return gehInDieEcke(sensors)
        }
    }

    fun istGegnerInZiellinie(sensors: Sensors): Boolean {
        for (gegner in sensors.others) {

            if (gegner.position.x == sensors.self.position.x) {
                return true

            }
            if (gegner.position.y == sensors.self.position.y) {
                return true

            }
        }
        return false
    }

    fun schießAufGegner(sensors: Sensors): Action {
        for (gegner in sensors.others) {
            if (gegner.position.x == sensors.self.position.x) {
                if (gegner.position.y > sensors.self.position.y)
                    return Action.Shoot(Direction.SOUTH)
                else {
                    return Action.Shoot(Direction.NORTH)
                }
            }
            if (gegner.position.y == sensors.self.position.y) {
                if (gegner.position.x > sensors.self.position.x)
                    return Action.Shoot(Direction.EAST)
                else {
                    return Action.Shoot(Direction.WEST)
                }
            }
        }
        return Action.Wait
    }

    fun gehInDieEcke(sensors: Sensors): Action {
        var direction = Direction.entries.random()

        val x = sensors.self.position.x
        val y = sensors.self.position.y

        if (y < 5) {
            direction = Direction.NORTH
        } else {
            direction = Direction.SOUTH
        }

        if (x == 0 || x == 9) {
            if (y < 5) {
                direction = Direction.NORTH
            } else {
                direction = Direction.SOUTH
            }
        }

        if (y == 0 || y == 9) {
            if (x < 5) {
                direction = Direction.WEST
            } else {
                direction = Direction.EAST
            }
        }
        return Action.Move(direction)
    }
}

val teamABots: List<RobotBrain> = listOf(MeinBot())