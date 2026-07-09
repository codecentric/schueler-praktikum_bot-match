package bots.teamc

// Hier entsteht euer Team-Bot. Ihr dürft diese Datei erweitern oder weitere
// Bot-Klassen in diesem Package anlegen. Vergesst nicht, neue Bots unten in
// `teamCBots` einzutragen, damit sie im Spiel erscheinen.

import kotlin.math.abs
import framework.arena.Action
import framework.arena.Direction
import framework.arena.RobotBrain
import framework.arena.RobotState
import framework.arena.Sensors

/** Startpunkt für euren eigenen Bot - benennt/erweitert diese Klasse nach Belieben. */
class MeinBot(override val name: String = "Team Winner - BummsBot") : RobotBrain {
    override fun decide (sensors: Sensors): Action {
        val self = sensors.self.position
        if (sensors.others.isEmpty()) {
            return Action.Wait
        }

        // Für jede Richtung: wer wäre dort das nächste Ziel? (Ein Schuss trifft
        // immer den nächsten Gegner in der Linie, egal wen wir "meinen".)
        val ziele = Direction.entries.mapNotNull { richtung ->
            naechsterInRichtung(self, richtung, sensors.others)?.let { richtung to it }
        }
        if (ziele.isNotEmpty()) {
            // Unter allen schießbaren Zielen den schwächsten zuerst erledigen (sichert Kills),
            // bei Gleichstand den nächsten.
            val (richtung, ziel) = ziele.minWith(
                compareBy({ it.second.health }, { abstand(self, it.second.position) })
            )
            return Action.Shoot(richtung)
        }

        // Kein Ziel in gerader Linie -> zum nächsten Gegner vorrücken.
        var gegner = sensors.others[0]
        var kleinsterAbstand = abstand(self, gegner.position)
        for (kandidat in sensors.others) {
            val d = abstand(self, kandidat.position)
            if (d < kleinsterAbstand) {
                kleinsterAbstand = d
                gegner = kandidat
            }
        }

        val dx = gegner.position.x - self.x
        val dy = gegner.position.y - self.y
        // Auf der Achse mit dem KLEINEREN Abstand bewegen: die wird zuerst auf 0
        // gebracht, wir sind also schneller in einer Reihe/Spalte mit dem Gegner
        // und können schießen (mehr Ticks auf der großen Achse zu laufen bringt
        // uns nicht schneller in Schussposition).
        val richtungZumGegner = if (abs(dx) <= abs(dy)) {
            if (dx > 0) Direction.EAST else Direction.WEST
        } else {
            if (dy > 0) Direction.SOUTH else Direction.NORTH
        }

        // Bei kritischem HP-Stand und Gegner in unmittelbarer Nähe lieber
        // zurückweichen als weiter in die Gefahr zu laufen.
        val fluchtnoetig = sensors.self.health <= 20 && kleinsterAbstand <= 2
        return Action.Move(if (fluchtnoetig) richtungZumGegner.gegenteil() else richtungZumGegner)
    }

    private fun abstand(a: framework.arena.Position, b: framework.arena.Position) = abs(a.x - b.x) + abs(a.y - b.y)

    private fun naechsterInRichtung(self: framework.arena.Position, richtung: Direction, andere: List<RobotState>): RobotState? {
        return andere.filter { kandidat ->
            when (richtung) {
                Direction.NORTH -> kandidat.position.x == self.x && kandidat.position.y < self.y
                Direction.SOUTH -> kandidat.position.x == self.x && kandidat.position.y > self.y
                Direction.EAST -> kandidat.position.y == self.y && kandidat.position.x > self.x
                Direction.WEST -> kandidat.position.y == self.y && kandidat.position.x < self.x
            }
        }.minByOrNull { abstand(self, it.position) }
    }

    private fun Direction.gegenteil(): Direction = when (this) {
        Direction.NORTH -> Direction.SOUTH
        Direction.SOUTH -> Direction.NORTH
        Direction.EAST -> Direction.WEST
        Direction.WEST -> Direction.EAST
    }
}
val teamCBots: List<RobotBrain> = listOf(MeinBot())




