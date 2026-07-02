package bots.teama

// Hier entsteht euer Team-Bot. Ihr dürft diese Datei erweitern oder weitere
// Bot-Klassen in diesem Package anlegen. Vergesst nicht, neue Bots unten in
// `teamABots` einzutragen, damit sie im Spiel erscheinen.

import framework.arena.Action
import framework.arena.Direction
import framework.arena.RobotBrain
import framework.arena.Sensors

/** Startpunkt für euren eigenen Bot - benennt/erweitert diese Klasse nach Belieben. */
class MeinBot(override val name: String = "Team A - MeinBot") : RobotBrain {
    override fun decide(sensors: Sensors): Action {
        // TODO: Bewege dich stattdessen in Richtung des nächsten Gegners
        // TODO: Schieße wenn ein Gegner in Sichtlinie ist
        // TODO: Reagiere auf niedrige eigene HP (sensors.self.health), z.B. mit Flucht
        return Action.Move(Direction.SOUTH)
    }
}

val teamABots: List<RobotBrain> = listOf(MeinBot())
