# CLAUDE.md

Technischer Überblick für Claude Code (oder Entwickler), die an diesem Repo weiterarbeiten. Für den didaktischen/organisatorischen Teil siehe [`README.md`](README.md) und [`docs/`](docs/).

## Was ist das

Kotlin/Compose-Desktop-App für ein 3-Tage-Schülerpraktikum (10. Klasse, Kotlin-Anfänger). Roboter kämpfen auf einem 10×10-Raster gegeneinander. Schüler implementieren ausschließlich `RobotBrain.decide()`, das Framework (Engine + UI) ist vollständig fertig und wird von Schülern nicht verändert.

## Stack

- Kotlin 2.0.21 (JVM, kein Multiplatform-Target — Desktop-only)
- Compose Multiplatform 1.7.3 (`org.jetbrains.compose`) + `org.jetbrains.kotlin.plugin.compose` 2.0.21 (Compose-Compiler-Plugin muss exakt die Kotlin-Version matchen)
- Gradle 8.6, JVM Toolchain 21
- JUnit 5 für Engine-Tests

## Architektur

```
framework/arena/   Reine JVM-Logik, KEINE Compose-Abhängigkeit
  Models.kt          Direction, Position, RobotState, Sensors, Action, RobotBrain
  BotExecutor.kt      Führt RobotBrain.decide() sicher aus (Timeout + Crash-Schutz)
  GameEngine.kt       Tick-Loop, resolveMoves()/resolveShots() (reine Funktionen), Match-State
  BotRegistry.kt      Fasst alle bots.teamX.teamXBots + bots.examples zusammen

framework/ui/       Compose Desktop, konsumiert nur die public API von GameEngine
  App.kt              State-Owner, LaunchedEffect-Tick-Loop, Layout
  ArenaCanvas.kt      Zeichnet Grid + Roboter + Healthbars
  Scoreboard.kt / LogPanel.kt / Controls.kt

bots/examples/      Vom Dozenten gepflegte Referenz-Bots (RandomBot, StillstandBot, ChaserBot, FluchtBot)
bots/teama|b|c/      Von Schülern gepflegte Bot-Klasse + teamXBots-Liste (Konvention, siehe unten)
```

`framework/arena` ist bewusst UI-unabhängig gehalten, damit die Spielregeln unit-testbar sind (`src/test/kotlin/framework/arena/GameEngineTest.kt`) und die Engine ohne laufende App getestet werden kann.

## Zentrale API

```kotlin
interface RobotBrain {
    val name: String
    fun decide(sensors: Sensors): Action   // wird jeden Tick 1x aufgerufen
}

class GameEngine(arenaWidth = 10, arenaHeight = 10, maxTicks = 200, startHealth = 100, damagePerHit = 10) {
    fun startMatch(brains: List<RobotBrain>)
    fun step(onLog: (String) -> Unit): MatchStatus   // 1 Tick
    fun currentStates(): List<RobotState>
    fun result(): MatchResult?   // non-null nur wenn FINISHED
    fun shutdown()               // Daemon-Thread-Pool sauber beenden
}
```

Bot-IDs sind `"bot-0"`, `"bot-1"`, ... in Reihenfolge der `brains`-Liste bei `startMatch()`.

## Wichtige Design-Entscheidungen (nicht offensichtlich, bitte beim Ändern beachten)

- **Kein Coroutine-Timeout für Bot-Code.** `withTimeoutOrNull` kann CPU-lastige Endlosschleifen ohne Suspension-Point nicht abbrechen (kooperative Cancellation). `BotExecutor` nutzt stattdessen einen dedizierten Daemon-Thread-Pool + `Future.get(timeoutMs)`. Ein hängender Thread wird nicht gestoppt (technisch bei reinen CPU-Loops nicht sauber möglich), sondern nach 3 aufeinanderfolgenden Timeouts "eingefroren" (bekommt keine neuen Threads mehr zugewiesen, liefert nur noch `Action.Wait`).
- **Move-Auflösung ist snapshot-basiert.** Alle Zielzellen werden gegen den Zustand zu Tick-Beginn geprüft, nicht gegen bereits aktualisierte Positionen. Sonst hinge das Ergebnis von der internen Abarbeitungsreihenfolge der Bot-Liste ab. Kein Swap zwischen zwei sich kreuzenden Bots erlaubt; bei Zielkonflikt (2 Bots wollen ins selbe freie Feld) bewegt sich keiner.
- **Shot-Schaden wird gesammelt, nicht sofort angewendet.** Erst alle Treffer eines Ticks sammeln, dann gemeinsam Schaden anwenden — sonst würde die Abarbeitungsreihenfolge der Schützen beeinflussen, ob ein bereits „totgeschossener“ Bot in diesem Tick noch als gültiges Ziel zählt.
- **Kein Team-/Friendly-Fire-Konzept.** `RobotState.teamName` wird 1:1 aus `RobotBrain.name` übernommen. Die Gruppierung in `BotRegistry.allTeams` (Team A/B/C) ist rein organisatorisch für UI-Anzeige und Backlog — spielerisch ist es ein reines Free-for-all, jeder Bot kämpft für sich. Das ist bewusst so gewählt, um die Engine einfach zu halten; falls das erweitert werden soll, betrifft das `resolveShots()` in `GameEngine.kt`.
- **`RobotState.alive` ist eine abgeleitete Property** (`health > 0`), kein gespeichertes Feld — verhindert inkonsistente Zustände über `copy()` (z.B. `health = 0` ohne `alive` nachzuziehen).
- **Startpositionen sind deterministisch** (`computeStartPositions`, entlang des Arena-Perimeters verteilt), kein `Random` — damit Engine-Tests reproduzierbar sind.

## Schüler-Bot-Konvention

Jede Datei unter `bots/teamX/TeamXBots.kt` exportiert eine `val teamXBots: List<RobotBrain>`. `BotRegistry.kt` importiert diese drei Listen plus `bots.examples.*` und stellt `allTeams: Map<String, List<RobotBrain>>` sowie `allBots(): List<RobotBrain>` bereit. `Main.kt` übergibt `BotRegistry.allBots()` an `App()`. Neue Schüler-Bot-Klassen müssen in der jeweiligen `teamXBots`-Liste ergänzt werden, sonst tauchen sie nicht in der UI-Auswahl auf.

## Befehle

```bash
./gradlew test    # Engine-Unit-Tests (JUnit5), keine UI beteiligt
./gradlew build   # Kompilierung + Tests
./gradlew run     # startet die Compose-Desktop-App
```

## Bekannte, bewusste Grenzen (kein Bug)

- Reset in der UI erzeugt eine neue `GameEngine`-Instanz statt die bestehende zurückzusetzen (die Engine bietet kein `reset()`); die alte Instanz wird nicht explizit heruntergefahren, nur die zuletzt aktive beim Verlassen der Composition. Für ein 3-Tage-Praktikum unkritisch.
- Kein Turnier-Bracket-System — Battle-Royale mit allen ausgewählten Bots gleichzeitig deckt sowohl Testduelle (2 Bots) als auch das Finale (alle Bots) ab.
