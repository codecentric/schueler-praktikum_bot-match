package framework.arena

/** Ein Bot möchte sich in diesem Tick auf [target] bewegen. */
data class MoveRequest(val botId: String, val target: Position)

/**
 * Löst alle Bewegungswünsche EINES Ticks gegen einen Snapshot auf.
 *
 * WARUM snapshot-basiert (also gegen den Zustand zu Tick-BEGINN geprüft, nicht
 * gegen bereits aktualisierte Positionen)? Sonst würde die Reihenfolge, in der
 * wir die Bots zufällig abarbeiten, das Ergebnis beeinflussen (Bot A könnte auf
 * ein Feld ziehen, das Bot B gerade erst durch seinen eigenen Zug freigemacht hat -
 * je nach Abarbeitungsreihenfolge käme mal das eine, mal das andere Ergebnis raus).
 * Mit einem festen Snapshot ist das Ergebnis unabhängig von der internen
 * Abarbeitungsreihenfolge und damit reproduzierbar.
 *
 * Regeln:
 * - Zielfeld außerhalb der Arena -> Bewegung ungültig, Bot bleibt stehen.
 * - Zielfeld war zu Tick-Beginn von einem lebenden Bot belegt -> ungültig.
 *   Das gilt auch dann, wenn dieser Bot selbst im gleichen Tick wegzieht - es
 *   gibt also keinen Platztausch/Swap zwischen zwei sich kreuzenden Bots, weil
 *   beide jeweils das (zu Tick-Beginn) belegte Feld des anderen anvisieren.
 * - Wollen zwei oder mehr Bots ins selbe freie Zielfeld, bewegt sich KEINER
 *   von ihnen (Konflikt).
 * - Tote Bots blockieren keine Felder und werden ignoriert.
 */
fun resolveMoves(
    snapshot: List<RobotState>,
    moveRequests: List<MoveRequest>,
    arenaWidth: Int,
    arenaHeight: Int
): Map<String, Position> {
    val aliveSnapshot = snapshot.filter { it.alive }
    val occupiedAtStart = aliveSnapshot.map { it.position }.toSet()
    val startPositionById = aliveSnapshot.associate { it.id to it.position }

    // Nur Anfragen von (noch) lebenden Bots berücksichtigen, deren Ziel im
    // Raster liegt und das zu Tick-Beginn nicht belegt war.
    val validCandidates = moveRequests.filter { req ->
        startPositionById.containsKey(req.botId) &&
            req.target.x in 0 until arenaWidth &&
            req.target.y in 0 until arenaHeight &&
            req.target !in occupiedAtStart
    }

    val result = startPositionById.toMutableMap()
    for ((target, requesters) in validCandidates.groupBy { it.target }) {
        if (requesters.size == 1) {
            result[requesters[0].botId] = target
        }
        // Bei mehr als einem Bewerber um dasselbe freie Feld: Konflikt,
        // niemand bewegt sich (Eintrag bleibt auf der Startposition stehen).
    }
    return result
}

/** Ein Bot möchte in diesem Tick in [direction] schießen. */
data class ShotRequest(val botId: String, val direction: Direction)

/** Ein tatsächlich abgefeuerter Schuss in einem Tick, für die UI-Anzeige (Laserstrahl). */
data class ShotEvent(val shooterId: String, val fromPosition: Position, val toPosition: Position, val hitBot: Boolean, val direction: Direction)

/**
 * Löst alle Schusswünsche EINES Ticks auf. Wird NACH [resolveMoves] aufgerufen
 * und rechnet daher mit den NEUEN Positionen (nach der Bewegungsauflösung).
 *
 * WARUM erst alle Treffer sammeln und dann gemeinsam Schaden anwenden, statt
 * sofort beim Verarbeiten jedes Schusses den Schaden abzuziehen? Weil sonst die
 * Abarbeitungsreihenfolge der Schüsse das Ergebnis verfälschen könnte (z.B. ob
 * ein bereits "totgeschossener" Bot in diesem selben Tick noch als gültiges Ziel
 * für einen anderen Schützen zählt oder nicht). In der echten Welt fallen alle
 * Schüsse eines Ticks gleichzeitig - das bilden wir ab, indem wir zuerst pro
 * Ziel den Gesamtschaden aufsummieren und ihn erst danach (vom Aufrufer) auf
 * alle betroffenen Bots gleichzeitig anwenden.
 *
 * Ein Schuss trifft den nächstgelegenen lebenden Bot in exakt derselben Reihe
 * (NORTH/SOUTH: gleiche x-Koordinate) bzw. Spalte (EAST/WEST: gleiche
 * y-Koordinate) in Schussrichtung. Trifft ein Schuss niemanden, passiert nichts.
 */
fun resolveShots(
    positionsAfterMove: Map<String, Position>,
    aliveStates: List<RobotState>,
    shotRequests: List<ShotRequest>,
    damage: Int = 10
): Map<String, Int> {
    val aliveIds = aliveStates.filter { it.alive }.map { it.id }.toSet()
    val damageByTarget = mutableMapOf<String, Int>()

    for (shot in shotRequests) {
        val shooterId = shot.botId
        if (shooterId !in aliveIds) continue
        val shooterPos = positionsAfterMove[shooterId] ?: continue

        val targetId = nearestTargetInDirection(shooterId, shooterPos, shot.direction, aliveIds, positionsAfterMove)

        targetId?.let {
            damageByTarget[it] = (damageByTarget[it] ?: 0) + damage
        }
    }
    return damageByTarget
}

/** Liefert die ID des nächstgelegenen lebenden Bots in [direction] von [shooterPos] aus, oder null. */
internal fun nearestTargetInDirection(
    shooterId: String,
    shooterPos: Position,
    direction: Direction,
    aliveIds: Set<String>,
    positionsAfterMove: Map<String, Position>
): String? {
    var nearestTargetId: String? = null
    var nearestDistance = Int.MAX_VALUE

    for (candidateId in aliveIds) {
        if (candidateId == shooterId) continue
        val pos = positionsAfterMove[candidateId] ?: continue
        val distance = distanceInDirection(shooterPos, pos, direction) ?: continue
        if (distance < nearestDistance) {
            nearestDistance = distance
            nearestTargetId = candidateId
        }
    }
    return nearestTargetId
}


/**
 * Liefert den Abstand von [from] zu [to] entlang [direction], falls [to] exakt
 * in dieser Richtung liegt (gleiche Reihe/Spalte, vor [from]). Sonst null.
 */
private fun distanceInDirection(from: Position, to: Position, direction: Direction): Int? {
    return when (direction) {
        Direction.NORTH -> if (to.x == from.x && to.y < from.y) from.y - to.y else null
        Direction.SOUTH -> if (to.x == from.x && to.y > from.y) to.y - from.y else null
        Direction.EAST -> if (to.y == from.y && to.x > from.x) to.x - from.x else null
        Direction.WEST -> if (to.y == from.y && to.x < from.x) from.x - to.x else null
    }
}

/** Liefert die Position am Arena-Rand, an der ein Schuss aus [from] in [direction] endet (kein Ziel getroffen). */
private fun edgePosition(from: Position, direction: Direction, arenaWidth: Int, arenaHeight: Int): Position {
    return when (direction) {
        Direction.NORTH -> Position(from.x, 0)
        Direction.SOUTH -> Position(from.x, arenaHeight - 1)
        Direction.EAST -> Position(arenaWidth - 1, from.y)
        Direction.WEST -> Position(0, from.y)
    }
}

/** Status eines laufenden Matches. */
enum class MatchStatus { RUNNING, FINISHED }

/** Ergebnis eines beendeten Matches. */
data class MatchResult(
    val winnerId: String?,
    val isDraw: Boolean,
    val finalStates: List<RobotState>,
    val ticksPlayed: Int
)

/**
 * Orchestriert ein komplettes Match: hält den Zustand aller Bots und führt
 * per [step] jeweils genau einen Tick aus. Kennt bewusst nichts von Compose/UI -
 * der UI-Layer treibt die Engine von außen an (z.B. per LaunchedEffect-Schleife)
 * und bekommt Log-Zeilen über den [onLog]-Callback statt über println.
 */
class GameEngine(
    private val arenaWidth: Int = 10,
    private val arenaHeight: Int = 10,
    private val maxTicks: Int = 200,
    private val startHealth: Int = 100,
    private val damagePerHit: Int = 10,
    private val botExecutor: BotExecutor = BotExecutor()
) {
    private var brainById: Map<String, RobotBrain> = emptyMap()
    private var states: LinkedHashMap<String, RobotState> = linkedMapOf()
    private var tick: Int = 0
    private var status: MatchStatus = MatchStatus.RUNNING
    private var lastShots: List<ShotEvent> = emptyList()

    /** Startet ein neues Match mit den gegebenen Bot-Gehirnen. */
    fun startMatch(brains: List<RobotBrain>) {
        botExecutor.reset()
        tick = 0
        status = if (brains.size <= 1) MatchStatus.FINISHED else MatchStatus.RUNNING
        lastShots = emptyList()

        // computeStartPositions() selbst bleibt deterministisch (siehe dort), damit
        // sie isoliert testbar ist. Für ein echtes Match werden die Plätze aber
        // zufällig auf die Bots verteilt, damit nicht z.B. immer bot-0 in der Ecke
        // oben links landet.
        val startPositions = computeStartPositions(brains.size, arenaWidth, arenaHeight).shuffled()
        brainById = brains.indices.associate { i -> botIdOf(i) to brains[i] }
        states = linkedMapOf()
        brains.forEachIndexed { i, brain ->
            val id = botIdOf(i)
            states[id] = RobotState(
                id = id,
                teamName = brain.name,
                position = startPositions[i],
                health = startHealth
            )
        }
    }

    private fun botIdOf(index: Int): String = "bot-$index"

    /**
     * Führt genau einen Tick aus: ruft decide() für alle lebenden Bots auf
     * (Snapshot-Sensors von Tick-Beginn), löst Bewegungen auf, dann Schüsse,
     * wendet Schaden an und prüft, ob das Match beendet ist.
     */
    fun step(onLog: (String) -> Unit): MatchStatus {
        if (status == MatchStatus.FINISHED) return status
        tick++

        val snapshot = states.values.toList()
        val aliveSnapshot = snapshot.filter { it.alive }

        val actions = aliveSnapshot.associate { robot ->
            val brain = brainById.getValue(robot.id)
            val sensors = Sensors(
                self = robot,
                others = aliveSnapshot.filter { it.id != robot.id },
                arenaWidth = arenaWidth,
                arenaHeight = arenaHeight,
                tick = tick
            )
            robot.id to botExecutor.decideSafely(brain, sensors, robot.id, onLog)
        }

        val moveRequests = actions.mapNotNull { (id, action) ->
            if (action is Action.Move) {
                MoveRequest(id, states.getValue(id).position.moved(action.direction))
            } else null
        }
        val positionsAfterMove = resolveMoves(aliveSnapshot, moveRequests, arenaWidth, arenaHeight)

        val shotRequests = actions.mapNotNull { (id, action) ->
            if (action is Action.Shoot) ShotRequest(id, action.direction) else null
        }
        val damageByTarget = resolveShots(positionsAfterMove, aliveSnapshot, shotRequests, damagePerHit)
        val aliveIds = aliveSnapshot.map { it.id }.toSet()
        lastShots = shotRequests.mapNotNull { request ->
            val from = positionsAfterMove[request.botId] ?: return@mapNotNull null
            val targetId = nearestTargetInDirection(request.botId, from, request.direction, aliveIds, positionsAfterMove)
            val to = targetId?.let { positionsAfterMove[it] }
                ?: edgePosition(from, request.direction, arenaWidth, arenaHeight)
            ShotEvent(request.botId, from, to, hitBot = targetId != null, request.direction)
        }

        for (robot in aliveSnapshot) {
            val newPosition = positionsAfterMove[robot.id] ?: robot.position
            val damage = damageByTarget[robot.id] ?: 0
            val newHealth = (robot.health - damage).coerceAtLeast(0)
            states[robot.id] = robot.copy(position = newPosition, health = newHealth)
            if (damage > 0) {
                onLog("${robot.teamName} (${robot.id}) erhält $damage Schaden -> $newHealth HP")
            }
        }

        val aliveNow = states.values.filter { it.alive }
        status = if (aliveNow.size <= 1 || tick >= maxTicks) MatchStatus.FINISHED else MatchStatus.RUNNING
        return status
    }

    fun currentStates(): List<RobotState> = states.values.toList()

    /** Schüsse des zuletzt ausgeführten Ticks, für die UI-Anzeige (Laserstrahlen). */
    fun lastShots(): List<ShotEvent> = lastShots

    fun currentTick(): Int = tick

    fun currentStatus(): MatchStatus = status

    /** Liefert das Match-Ergebnis, oder null solange das Match noch läuft. */
    fun result(): MatchResult? {
        if (status != MatchStatus.FINISHED) return null
        val finalStates = states.values.toList()
        val aliveBots = finalStates.filter { it.alive }

        val (winnerId, isDraw) = when {
            aliveBots.size == 1 -> aliveBots[0].id to false
            aliveBots.isEmpty() -> null to true
            else -> {
                val maxHealth = aliveBots.maxOf { it.health }
                val leaders = aliveBots.filter { it.health == maxHealth }
                if (leaders.size == 1) leaders[0].id to false else null to true
            }
        }
        return MatchResult(winnerId = winnerId, isDraw = isDraw, finalStates = finalStates, ticksPlayed = tick)
    }

    /** Fährt den internen Bot-Thread-Pool herunter (z.B. beim Beenden der App). */
    fun shutdown() {
        botExecutor.shutdown()
    }
}

/**
 * Verteilt [count] Bots deterministisch (kein Random, damit Tests reproduzierbar
 * sind) und überlappungsfrei entlang des Arena-Randes, in möglichst
 * gleichmäßigem Abstand zueinander.
 */
internal fun computeStartPositions(count: Int, arenaWidth: Int, arenaHeight: Int): List<Position> {
    if (count <= 0) return emptyList()
    val perimeter = buildPerimeter(arenaWidth, arenaHeight)
    return (0 until count).map { i ->
        val index = (i.toLong() * perimeter.size / count).toInt() % perimeter.size
        perimeter[index]
    }
}

/** Läuft im Uhrzeigersinn einmal am Rand der Arena entlang. */
private fun buildPerimeter(width: Int, height: Int): List<Position> {
    if (width <= 0 || height <= 0) return listOf(Position(0, 0))
    if (width == 1 || height == 1) {
        // Kein echter Rand möglich -> das ganze (schmale) Raster ablaufen.
        return (0 until width).flatMap { x -> (0 until height).map { y -> Position(x, y) } }
    }
    val points = mutableListOf<Position>()
    for (x in 0 until width) points.add(Position(x, 0))
    for (y in 1 until height) points.add(Position(width - 1, y))
    for (x in width - 2 downTo 0) points.add(Position(x, height - 1))
    for (y in height - 2 downTo 1) points.add(Position(0, y))
    return points
}
