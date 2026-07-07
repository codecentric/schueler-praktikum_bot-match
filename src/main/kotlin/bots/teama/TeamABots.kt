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
        var direction = Direction.entries.random()
        println(direction)
        println(sensors)
        println (sensors.self.position)



        val MeinX = sensors.self.position.x
        val MeinY = sensors.self.position.y
        val gegner = sensors.others.get(0)
        var ziel = sensors.others.get(0)
        var opfer = abs(gegner.position.x - MeinX) + abs(gegner.position.y - MeinY)
        for (gegner in sensors.others) {
            val abstand = abs(gegner.position.x - MeinX) + abs(gegner.position.y - MeinY)
            if (abstand < opfer){
                opfer = abstand
                ziel = gegner

            }
        }
        if (sensors.others.size > 1){
            val gegner1 = sensors.others[1]
            if (gegner1.position.x == MeinX  gegner.position.x == MeinX) {}
        }


        var wenigsteHp = ziel.health
        for (gegner in sensors.others) {
            if (gegner.health < wenigsteHp){
                wenigsteHp = gegner.health
                ziel = gegner
            }

        }

        val x = sensors.self.position.x
        val y = sensors.self.position.y
        println(gegner)

        if (gegner.position.x == sensors.self.position.x){
            if (gegner.position.y > sensors.self.position.y)
                return Action.Shoot(Direction.SOUTH)
            else {
                return Action.Shoot(Direction.NORTH)
            }
        }
        if (gegner.position.y == sensors.self.position.y){
            if (gegner.position.x > sensors.self.position.x)
                return Action.Shoot(Direction.EAST)
            else {
                return Action.Shoot(Direction.WEST)
            }
        }

        if (gegner.position.y == sensors.self.position.y) {
            return Action.Shoot(direction)
        }

        if (x < 5) {
            direction = Direction.WEST
        }
        else{
            direction = Direction.EAST
        }

        if (y < 5) {
            direction = Direction.NORTH
        }
        else{
            direction = Direction.SOUTH
        }

        if (x == 0 || x == 9){
            if (y < 5){
                direction = Direction.NORTH
            }
            else{
                direction = Direction.SOUTH
            }
        }

        if (y == 0 || y == 9){
            if (x < 5){
                direction = Direction.WEST
            }
            else{
                direction = Direction.EAST
            }
        }

        return Action.Move(direction)
    }

    fun
}

val teamABots: List<RobotBrain> = listOf(MeinBot())