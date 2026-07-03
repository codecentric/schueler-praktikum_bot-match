package framework.arena

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.random.Random

/**
 * Ruft [RobotBrain.decide] geschützt auf: Schülercode kann eine Endlosschleife,
 * eine Exception oder einen StackOverflowError enthalten - das darf niemals die
 * ganze App einfrieren oder abstürzen lassen.
 *
 * WARUM kein Coroutine-`withTimeoutOrNull`?
 * Coroutine-Cancellation ist kooperativ: sie wirkt nur an "Suspension Points"
 * (z.B. `delay()`, `yield()`). Eine CPU-lastige Endlosschleife wie `while (true) {}`
 * enthält keinen solchen Punkt und würde daher NIE abgebrochen - der Timeout liefe
 * einfach ins Leere, während der Thread für immer weiterläuft und CPU frisst.
 * Deshalb nutzen wir hier einen echten Thread pro Aufruf plus `Future.get(timeout)`:
 * Das JVM-Scheduling erzwingt den Timeout unabhängig davon, ob der Bot-Code
 * jemals "freiwillig" die Kontrolle abgibt. Der blockierte Thread läuft zwar im
 * schlimmsten Fall (bei einer echten Endlosschleife) dauerhaft weiter, aber als
 * Daemon-Thread hält er wenigstens den JVM-Prozess nicht am Leben, und wir geben
 * sofort `Action.Wait` zurück, sodass das Spiel weiterläuft.
 */
class BotExecutor(
    private val timeoutMs: Long = 50,
    // Alle N Ticks wird die Bot-Entscheidung durch eine zufällige Bewegung ersetzt,
    // um festgefahrene Situationen aufzubrechen (z.B. zwei Bots, die immer wieder
    // aufs selbe Feld wollen und sich gegenseitig blockieren). 0 = deaktiviert.
    private val shakeUpEveryTicks: Int = 25
) {

    // Pool statt fester Thread-Anzahl, weil Threads, die in einer Endlosschleife
    // hängen bleiben, nie zurückgegeben werden - der Pool wächst dann einfach nach.
    // Daemon-Threads, damit die JVM auch dann sauber beendet werden kann, wenn
    // noch "gefangene" Bot-Threads offen sind.
    private val executor: ExecutorService = Executors.newCachedThreadPool { r ->
        Thread(r, "bot-decide").apply { isDaemon = true }
    }

    private val consecutiveTimeouts = mutableMapOf<String, Int>()
    private val frozen = mutableSetOf<String>()

    /**
     * Führt [brain].decide(sensors) mit Zeitlimit aus.
     * Bei Timeout, Exception oder Error wird [Action.Wait] zurückgegeben statt
     * die Ausnahme weiterzureichen. Nach 3 Timeouts in Folge wird der Bot für
     * den Rest des Matches "eingefroren" (liefert nur noch Wait, ohne decide()
     * überhaupt noch aufzurufen - so sammeln sich keine weiteren hängenden Threads an).
     */
    fun decideSafely(brain: RobotBrain, sensors: Sensors, botId: String, onLog: (String) -> Unit): Action {
        if (botId in frozen) return Action.Wait

        // Anti-Hänger: alle shakeUpEveryTicks Ticks eine erzwungene Zufallsbewegung,
        // damit sich wiederholende Blockaden (z.B. zwei Bots wollen dauerhaft aufs
        // selbe Feld) aufgelöst werden. Move statt Shoot/Wait, weil nur eine
        // Positionsänderung die Blockade tatsächlich bricht.
        if (shakeUpEveryTicks > 0 && sensors.tick > 0 && sensors.tick % shakeUpEveryTicks == 0) {
            return Action.Move(Direction.entries[Random.nextInt(Direction.entries.size)])
        }

        val future = executor.submit(Callable { brain.decide(sensors) })
        return try {
            val action = future.get(timeoutMs, TimeUnit.MILLISECONDS)
            consecutiveTimeouts[botId] = 0
            action
        } catch (e: TimeoutException) {
            // Wir brechen den hängenden Task NICHT hart ab (interrupt() würde bei
            // einer CPU-Schleife ohnehin nichts bewirken) - wir ignorieren ihn einfach
            // und lassen das Match weiterlaufen.
            val count = (consecutiveTimeouts[botId] ?: 0) + 1
            consecutiveTimeouts[botId] = count
            onLog("${brain.name}: Zeitlimit überschritten (Endlosschleife?) -> Wait")
            if (count >= 3) {
                frozen += botId
                onLog("${brain.name}: 3x hintereinander zu langsam -> für den Rest des Matches eingefroren")
            }
            Action.Wait
        } catch (e: Throwable) {
            // Fängt bewusst Throwable (nicht nur Exception), damit auch
            // StackOverflowError o.ä. aus Schülercode nicht die App mitreißt.
            onLog("${brain.name}: Fehler in decide(): ${e::class.simpleName}: ${e.message} -> Wait")
            Action.Wait
        }
    }

    /** Setzt Timeout-Zähler und eingefrorene Bots zurück, z.B. für ein neues Match. */
    fun reset() {
        consecutiveTimeouts.clear()
        frozen.clear()
    }

    /** Fährt den Thread-Pool herunter (z.B. beim Beenden der App). */
    fun shutdown() {
        executor.shutdownNow()
    }
}
