package bots.examples

import framework.arena.Action
import framework.arena.Direction
import framework.arena.RobotBrain
import framework.arena.Sensors

/**
 * Bewegt sich nie und schießt bei jedem Tick stur in eine feste, im
 * Konstruktor übergebene Richtung (Default Osten). Dient als einfachstes
 * Testziel ("Dauerfeuer"), z.B. um zu prüfen, ob ein anderer Bot ihm
 * ausweicht oder ihn gezielt angreift.
 */
class StillstandBot(
    override val name: String = "StillstandBot",
    private val shootDirection: Direction = Direction.EAST
) : RobotBrain {
    override fun decide(sensors: Sensors): Action {
        return Action.Shoot(shootDirection)
    }
}
