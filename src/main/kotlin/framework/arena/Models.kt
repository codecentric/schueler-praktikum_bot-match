package framework.arena

/**
 * Eine Blickrichtung / Bewegungsrichtung in der Arena.
 * dx/dy geben an, wie sich x/y verändern, wenn man einen Schritt in diese Richtung geht.
 * Achtung: y wächst nach unten (wie bei Bildschirm-Koordinaten), NORTH heißt also y-1.
 */
enum class Direction(val dx: Int, val dy: Int) {
    NORTH(0, -1), EAST(1, 0), SOUTH(0, 1), WEST(-1, 0)
}

/** Eine Position auf dem Arena-Raster. (0,0) ist oben links. */
data class Position(val x: Int, val y: Int) {
    /** Liefert die Position, die einen Schritt in [direction] entfernt liegt. */
    fun moved(direction: Direction) = Position(x + direction.dx, y + direction.dy)
}

/** Der Zustand eines einzelnen Roboters zu einem bestimmten Zeitpunkt. */
data class RobotState(
    val id: String,
    val teamName: String,
    val position: Position,
    val health: Int
) {
    /** Ein Roboter lebt, solange er noch Trefferpunkte (HP) übrig hat. */
    val alive: Boolean get() = health > 0
}

/**
 * Alles, was ein Bot über die aktuelle Situation "sehen" darf, wenn er entscheidet.
 * Wird jeden Tick neu an [RobotBrain.decide] übergeben.
 */
data class Sensors(
    val self: RobotState,
    val others: List<RobotState>,
    val arenaWidth: Int,
    val arenaHeight: Int,
    val tick: Int
)

/** Eine Aktion, die ein Bot in einem Tick ausführen kann. */
sealed interface Action {
    data class Move(val direction: Direction) : Action
    data class Shoot(val direction: Direction) : Action
    data object Wait : Action
}

/**
 * Das Interface, das jeder Schüler-Bot implementieren muss.
 * [decide] wird jeden Tick einmal aufgerufen und muss genau eine [Action] zurückgeben.
 */
interface RobotBrain {
    val name: String
    fun decide(sensors: Sensors): Action
}
