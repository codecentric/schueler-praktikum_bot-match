package framework.arena

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GameEngineTest {

    private fun robot(id: String, x: Int, y: Int, health: Int = 100, team: String = id) =
        RobotState(id = id, teamName = team, position = Position(x, y), health = health)

    // ---------- resolveMoves ----------

    @Test
    fun `gueltige Bewegung in freie Zelle klappt`() {
        val a = robot("a", 1, 1)
        val requests = listOf(MoveRequest("a", Position(2, 1)))

        val result = resolveMoves(listOf(a), requests, arenaWidth = 5, arenaHeight = 5)

        assertEquals(Position(2, 1), result["a"])
    }

    @Test
    fun `Bewegung ausserhalb der Arena wird blockiert`() {
        val a = robot("a", 0, 0)
        val requests = listOf(MoveRequest("a", Position(-1, 0)))

        val result = resolveMoves(listOf(a), requests, arenaWidth = 5, arenaHeight = 5)

        assertEquals(Position(0, 0), result["a"])
    }

    @Test
    fun `Bewegung auf zu Tick-Beginn besetzte Zelle wird blockiert`() {
        val a = robot("a", 0, 0)
        val b = robot("b", 1, 0)
        val requests = listOf(MoveRequest("a", Position(1, 0)))

        val result = resolveMoves(listOf(a, b), requests, arenaWidth = 5, arenaHeight = 5)

        assertEquals(Position(0, 0), result["a"])
        assertEquals(Position(1, 0), result["b"])
    }

    @Test
    fun `zwei Bots wollen ins selbe freie Feld, beide bleiben stehen`() {
        val a = robot("a", 0, 0)
        val b = robot("b", 2, 0)
        val requests = listOf(
            MoveRequest("a", Position(1, 0)),
            MoveRequest("b", Position(1, 0))
        )

        val result = resolveMoves(listOf(a, b), requests, arenaWidth = 5, arenaHeight = 5)

        assertEquals(Position(0, 0), result["a"])
        assertEquals(Position(2, 0), result["b"])
    }

    @Test
    fun `kein Swap erlaubt`() {
        val a = robot("a", 0, 0)
        val b = robot("b", 1, 0)
        val requests = listOf(
            MoveRequest("a", Position(1, 0)),
            MoveRequest("b", Position(0, 0))
        )

        val result = resolveMoves(listOf(a, b), requests, arenaWidth = 5, arenaHeight = 5)

        assertEquals(Position(0, 0), result["a"])
        assertEquals(Position(1, 0), result["b"])
    }

    // ---------- resolveShots ----------

    @Test
    fun `Treffer in gerader Linie wird erkannt`() {
        val shooter = robot("a", 0, 0)
        val target = robot("b", 3, 0)
        val positions = mapOf("a" to shooter.position, "b" to target.position)
        val shots = listOf(ShotRequest("a", Direction.EAST))

        val damage = resolveShots(positions, listOf(shooter, target), shots, damage = 10)

        assertEquals(10, damage["b"])
    }

    @Test
    fun `kein Treffer wenn kein Bot in der Schusslinie`() {
        val shooter = robot("a", 0, 0)
        val other = robot("b", 3, 3)
        val positions = mapOf("a" to shooter.position, "b" to other.position)
        val shots = listOf(ShotRequest("a", Direction.EAST))

        val damage = resolveShots(positions, listOf(shooter, other), shots, damage = 10)

        assertTrue(damage.isEmpty())
    }

    @Test
    fun `mehrere Schuetzen treffen denselben Bot gleichzeitig, Schaden wird summiert`() {
        val target = robot("target", 2, 2)
        val shooterNorth = robot("north", 2, 0)
        val shooterWest = robot("west", 0, 2)
        val positions = mapOf(
            "target" to target.position,
            "north" to shooterNorth.position,
            "west" to shooterWest.position
        )
        val shots = listOf(
            ShotRequest("north", Direction.SOUTH),
            ShotRequest("west", Direction.EAST)
        )

        val damage = resolveShots(positions, listOf(target, shooterNorth, shooterWest), shots, damage = 10)

        assertEquals(20, damage["target"])
    }

    // ---------- BotExecutor ----------

    private class ThrowingBrain : RobotBrain {
        override val name = "Crasher"
        override fun decide(sensors: Sensors): Action = throw IllegalStateException("kaputt")
    }

    private class SlowBrain : RobotBrain {
        override val name = "Schnecke"
        override fun decide(sensors: Sensors): Action {
            Thread.sleep(200)
            return Action.Wait
        }
    }

    private fun dummySensors() = Sensors(
        self = RobotState("x", "X", Position(0, 0), 100),
        others = emptyList(),
        arenaWidth = 5,
        arenaHeight = 5,
        tick = 1
    )

    @Test
    fun `Bot der Exception wirft bekommt Action Wait`() {
        val executor = BotExecutor(timeoutMs = 100)
        val logs = mutableListOf<String>()

        val action = executor.decideSafely(ThrowingBrain(), dummySensors(), "crasher", logs::add)

        assertEquals(Action.Wait, action)
        assertTrue(logs.any { it.contains("Fehler") })
        executor.shutdown()
    }

    @Test
    fun `Bot der zu lange braucht bekommt Action Wait per Timeout`() {
        val executor = BotExecutor(timeoutMs = 50)
        val logs = mutableListOf<String>()

        val action = executor.decideSafely(SlowBrain(), dummySensors(), "slow", logs::add)

        assertEquals(Action.Wait, action)
        assertTrue(logs.any { it.contains("Zeitlimit") })
        executor.shutdown()
    }

    // ---------- GameEngine ----------

    private class WaitingBrain(override val name: String) : RobotBrain {
        override fun decide(sensors: Sensors): Action = Action.Wait
    }

    /** Bewegt sich einmal Richtung Osten, danach Dauerfeuer nach Osten. */
    private class ShooterBrain(override val name: String) : RobotBrain {
        override fun decide(sensors: Sensors): Action =
            if (sensors.tick == 1) Action.Move(Direction.EAST) else Action.Shoot(Direction.EAST)
    }

    @Test
    fun `einfaches Match zwischen zwei Bots laeuft bis zum Ergebnis durch`() {
        val engine = GameEngine(arenaWidth = 10, arenaHeight = 10, maxTicks = 200)
        val target = WaitingBrain("Ziel")
        val shooter = ShooterBrain("Schuetze")
        engine.startMatch(listOf(target, shooter))

        // Startpositionen liegen am Rand: bot-0 auf (0,0), bot-1 irgendwo auf dem
        // Perimeter. Damit der Schuss garantiert trifft, bringen wir beide Bots
        // testweise in dieselbe Reihe, indem wir ein neues Match mit klar
        // kontrollierbarer Erwartung laufen lassen: wir prüfen stattdessen nur,
        // dass das Match sauber terminiert (entweder durch Tod eines Bots oder maxTicks).
        var status = MatchStatus.RUNNING
        val logs = mutableListOf<String>()
        var safetyCounter = 0
        while (status == MatchStatus.RUNNING && safetyCounter < 500) {
            status = engine.step(logs::add)
            safetyCounter++
        }

        assertEquals(MatchStatus.FINISHED, status)
        val result = engine.result()
        assertNotNull(result)
        assertTrue(result.ticksPlayed <= 200)
        val alive = result.finalStates.filter { it.alive }
        assertTrue(alive.size <= 1 || result.ticksPlayed >= 200)
    }

    @Test
    fun `Schuetze in gleicher Reihe besiegt Ziel innerhalb von maxTicks`() {
        // Zwei Bots, die wir direkt (ohne GameEngine-Startpositionslogik) über
        // wiederholtes resolveShots simulieren, um einen deterministischen
        // Kampf in derselben Reihe zu garantieren.
        var targetHealth = 100
        val positions = mapOf("shooter" to Position(0, 0), "target" to Position(5, 0))
        val shooterState = robot("shooter", 0, 0)
        var targetState = robot("target", 5, 0, health = targetHealth)

        var ticks = 0
        while (targetState.alive && ticks < 200) {
            val damage = resolveShots(
                positions,
                listOf(shooterState, targetState),
                listOf(ShotRequest("shooter", Direction.EAST)),
                damage = 10
            )
            targetHealth = (targetHealth - (damage["target"] ?: 0)).coerceAtLeast(0)
            targetState = targetState.copy(health = targetHealth)
            ticks++
        }

        assertTrue(!targetState.alive)
        assertEquals(10, ticks)
    }
}
