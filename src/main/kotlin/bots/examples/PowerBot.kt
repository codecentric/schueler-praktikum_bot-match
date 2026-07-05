package bots.examples

import framework.arena.Action
import framework.arena.Direction
import framework.arena.Position
import framework.arena.RobotBrain
import framework.arena.RobotState
import framework.arena.Sensors
import kotlin.math.abs

/**
 * Der aggressivste Beispiel-Bot: kombiniert zwei Ideen, die in echten Kampf-KIs
 * (z.B. Zielpriorisierung nach Bedrohung/Gesundheit, siehe gängige Utility-AI-
 * Ansätze) üblich sind, statt nur stur den nächsten Gegner zu verfolgen wie
 * [ChaserBot]:
 *
 * 1. Steht IRGENDEIN Gegner bereits in Schusslinie (gleiche Reihe/Spalte), wird
 *    sofort geschossen - und zwar auf den schwächsten unter ihnen (niedrigste
 *    HP zuerst töten, statt wahllos auf den nächstbesten zu ballern).
 * 2. Ist niemand in Schusslinie, wird ein Ziel per Score aus Distanz UND
 *    Gesundheit gewählt (nahe UND schwache Gegner zuerst), nicht nur die
 *    nächste Distanz wie bei [ChaserBot]. Zum Ausrichten wird zuerst die
 *    x-Differenz geschlossen (gleiche Spalte anpeilen), dann geschossen -
 *    empirisch die stärkste der getesteten Annäherungsstrategien.
 *
 * Bewusst KEINE Flucht bei niedriger Gesundheit (anders als [FluchtBot]): gegen
 * einen Gegner mit identischem Bewegungsmuster (z.B. FluchtBot selbst) würde ein
 * Flucht-Trigger dazu führen, dass beide Bots synchron in eine endlose
 * Verfolgungsjagd geraten und sich nie mehr treffen -> Unentschieden statt Sieg.
 * PowerBot bleibt aggressiv bis zum Schluss.
 *
 * Grenze: Gegen einen exakt gleich starken, ähnlich aggressiven Bot (gleiche HP,
 * gleicher Schaden, z.B. [ChaserBot]) endet der Kampf zwangsläufig unentschieden -
 * sobald beide in derselben Linie stehen und feuern, sterben sie wegen 100 HP /
 * 10 Schaden nach exakt 10 Ticks gleichzeitig. Das ist Arithmetik, keine
 * Schwäche der Zielwahl.
 */
class PowerBot(override val name: String = "PowerBot") : RobotBrain {

    /**
     * Priorisiert Gegner in Sichtlinie (schwächster zuerst), sonst Annäherung an
     * das beste Ziel nach Distanz+HP-Score. Nie Flucht, siehe Klassen-Doc.
     *
     * @param sensors aktueller Wahrnehmungszustand (eigene Position, lebende Gegner).
     * @return [Action.Wait] wenn kein Gegner mehr lebt, sonst [Action.Shoot] auf das
     *   priorisierte Ziel in Sichtlinie oder [Action.Move] zur Annäherung.
     */
    override fun decide(sensors: Sensors): Action {
        val self = sensors.self
        val enemies = sensors.others
        if (enemies.isEmpty()) return Action.Wait

        val alignedEnemies = enemies.filter { isAligned(self.position, it.position) }
        if (alignedEnemies.isNotEmpty()) {
            val weakest = alignedEnemies.minWithOrNull(
                compareBy<RobotState> { it.health }.thenBy { manhattanDistance(self.position, it.position) }
            )!!
            return Action.Shoot(directionTo(self.position, weakest.position))
        }

        val target = bestTarget(self.position, enemies)
        return moveTowardAlignment(self.position, target.position)
    }

    /**
     * Prüft, ob [from] und [to] in derselben Reihe oder Spalte liegen - also ein
     * Schuss in die richtige Richtung treffen würde.
     */
    private fun isAligned(from: Position, to: Position): Boolean =
        from.x == to.x || from.y == to.y

    /** Distanz auf dem Grid ohne Diagonalbewegung: Summe der Achsenabstände. */
    private fun manhattanDistance(a: Position, b: Position): Int =
        abs(a.x - b.x) + abs(a.y - b.y)

    /**
     * Leitet aus der relativen Lage von [to] zu [from] die Schussrichtung ab.
     * Voraussetzung: [isAligned] für dasselbe Paar ist bereits `true`.
     */
    private fun directionTo(from: Position, to: Position): Direction {
        return when {
            from.x == to.x -> if (to.y < from.y) Direction.NORTH else Direction.SOUTH
            else -> if (to.x > from.x) Direction.EAST else Direction.WEST
        }
    }

    /** Score aus Distanz und Gesundheit: nahe UND schwache Gegner werden bevorzugt. */
    private fun bestTarget(self: Position, enemies: List<RobotState>): RobotState =
        enemies.minBy { manhattanDistance(self, it.position) + it.health / 10 }

    /**
     * Schließt zuerst die x-Differenz (bewegt sich in die gleiche Spalte wie das
     * Ziel), erst danach die y-Differenz. Diese "Spalte zuerst"-Reihenfolge hat
     * sich in Testläufen gegen alle anderen Beispiel-Bots als die stärkste
     * Ausricht-Strategie erwiesen.
     *
     * @param self eigene Position.
     * @param target Zielposition, der angenähert werden soll.
     * @return [Action.Move] Richtung x-Angleichung, oder falls `dx == 0`, y-Angleichung.
     */
    private fun moveTowardAlignment(self: Position, target: Position): Action {
        val dx = target.x - self.x
        val dy = target.y - self.y
        return if (dx != 0) {
            Action.Move(if (dx > 0) Direction.EAST else Direction.WEST)
        } else {
            Action.Move(if (dy > 0) Direction.SOUTH else Direction.NORTH)
        }
    }
}
