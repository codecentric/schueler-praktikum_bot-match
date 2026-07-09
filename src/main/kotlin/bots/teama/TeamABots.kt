package bots.teama

// Hier entsteht euer Team-Bot. Ihr dürft diese Datei erweitern oder weitere
// Bot-Klassen in diesem Package anlegen. Vergesst nicht, neue Bots unten in
// `teamABots` einzutragen, damit sie im Spiel erscheinen.

import framework.arena.Action
import framework.arena.Direction
import framework.arena.Position
import framework.arena.RobotBrain
import framework.arena.RobotState
import framework.arena.Sensors
import kotlin.math.abs

/** Startpunkt für euren eigenen Bot - benennt/erweitert diese Klasse nach Belieben. */
class MeinBot(override val name: String = "Team A - SkibidiTerminator") : RobotBrain {

    override fun decide(sensors: Sensors): Action {
        if (sensors.others.size <= 1) {
            return eleminiereLetztenGegner(sensors)
        }
        return taktischerZug(sensors)
    }

    // ---------- Endgame: nur noch ein Gegner übrig ----------

    /** Letzter Gegner übrig: ausrichten und schießen, sonst gezielt annähern. */
    fun eleminiereLetztenGegner(sensors: Sensors): Action {
        val gegner = sensors.others.firstOrNull() ?: return gehInDieEcke(sensors)
        val self = sensors.self.position
        if (istAusgerichtet(self, gegner.position)) {
            return Action.Shoot(richtungZu(self, gegner.position))
        }
        return Action.Move(schrittRichtungZu(self, gegner.position))
    }

    // ---------- Mehrere Gegner: Trade abwägen oder Bedrohung minimieren ----------

    fun taktischerZug(sensors: Sensors): Action {
        val self = sensors.self.position
        val ausgerichtete = sensors.others.filter { istAusgerichtet(self, it.position) }

        val bestesZiel = ausgerichtete.minByOrNull { it.health }
        if (bestesZiel != null) {
            return Action.Shoot(richtungZu(self, bestesZiel.position))
        }

        return wähleSicherenZug(sensors)
    }

    /** Bewertet die 4 Nachbarfelder nach Bedrohung (weniger ausgerichtete Gegner ist besser). */
    fun wähleSicherenZug(sensors: Sensors): Action {
        val self = sensors.self.position
        val kandidaten = Direction.entries.mapNotNull { richtung ->
            val ziel = self.moved(richtung)
            if (ziel.x !in 0 until sensors.arenaWidth || ziel.y !in 0 until sensors.arenaHeight) return@mapNotNull null
            richtung to ziel
        }
        if (kandidaten.isEmpty()) return gehInDieEcke(sensors)

        val schwächsterGegner = sensors.others.minByOrNull { it.health }

        val beste = kandidaten.minWithOrNull(
            compareBy(
                { (_, ziel) -> bedrohungAn(ziel, sensors.others) },
                { (_, ziel) -> schwächsterGegner?.let { distanz(ziel, it.position) } ?: 0 }
            )
        )
        return Action.Move((beste ?: kandidaten.first()).first)
    }

    fun distanz(a: Position, b: Position): Int = abs(a.x - b.x) + abs(a.y - b.y)

    /** Anzahl Gegner, die von [position] aus in Schusslinie stünden. */
    fun bedrohungAn(position: Position, gegner: List<RobotState>): Int =
        gegner.count { istAusgerichtet(position, it.position) }

    // ---------- Geometrie-Hilfsfunktionen ----------

    fun istAusgerichtet(a: Position, b: Position): Boolean = a.x == b.x || a.y == b.y

    /** Schussrichtung von [self] zu einem ausgerichteten [ziel]. */
    fun richtungZu(self: Position, ziel: Position): Direction {
        if (self.x == ziel.x) {
            return if (ziel.y > self.y) Direction.SOUTH else Direction.NORTH
        }
        return if (ziel.x > self.x) Direction.EAST else Direction.WEST
    }

    /**
     * Bewegungsrichtung von [self] Richtung [ziel]. Gleicht zuerst die Reihe (y) an,
     * damit möglichst früh eine Ausrichtung entsteht (schussbereit), statt lange
     * ungeschützt direkt auf den Gegner zuzulaufen.
     */
    fun schrittRichtungZu(self: Position, ziel: Position): Direction {
        val dx = ziel.x - self.x
        val dy = ziel.y - self.y
        return if (dy != 0) {
            if (dy > 0) Direction.SOUTH else Direction.NORTH
        } else {
            if (dx > 0) Direction.EAST else Direction.WEST
        }
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
