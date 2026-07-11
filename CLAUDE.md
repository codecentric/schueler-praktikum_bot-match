# CLAUDE.md

Technischer Überblick für Claude Code (oder Entwickler), die an diesem Repo weiterarbeiten. Für den didaktischen/organisatorischen Teil siehe [`README.md`](README.md) und [`docs/`](docs/).

## ⚠️ SEHR WICHTIG: Arbeitsweise für Claude Code

- Wo sinnvoll Subagents nutzen, um den Hauptkontext klein zu halten (z.B. Recherche/Exploration über mehrere Dateien an einen Subagent delegieren statt selbst alles zu lesen).
- Vor jeder nicht-trivialen Änderung erst eine Spezifikation (Plan) schreiben und dem User zur Abstimmung vorlegen. Erst nach Freigabe durch den User umsetzen.
- Bei jeder Änderung auf Konsistenz im gesamten Repo achten, insbesondere README.md und alle `docs/*.md` — Verweise, Dateinamen und Beschreibungen müssen zur Realität passen.
- Wenn eine `.md`-Datei unter `docs/` geändert wird, danach immer das passende PDF-Build-Skript ausführen (`docs/pdf/build/build.sh`/`build-landscape.sh` für `backlog.md`, sonst `build-docs.sh`), damit `docs/pdf/` nicht veraltet.

## Was ist das

Kotlin/Compose-Desktop-App für ein 3-Tage-Schülerpraktikum (10. Klasse, Kotlin-Anfänger). Roboter kämpfen auf einem 10×10-Raster gegeneinander. Schüler implementieren ausschließlich `RobotBrain.decide()`, das Framework (Engine + UI) ist vollständig fertig und wird von Schülern nicht verändert.

## Stack

- Kotlin 2.0.21 (JVM, kein Multiplatform-Target — Desktop-only)
- Compose Multiplatform 1.7.3 (`org.jetbrains.compose`) + `org.jetbrains.kotlin.plugin.compose` 2.0.21 (Compose-Compiler-Plugin muss exakt die Kotlin-Version matchen)
- Gradle 8.6, JVM Toolchain 21 (`settings.gradle.kts` bindet den `foojay-resolver-convention`-Plugin ein, damit Gradle bei Bedarf automatisch ein JDK 21 herunterlädt — Praktikumsrechner brauchen daher nur ein JDK 17 als Basis)
- JUnit 5 für Engine-Tests

## Architektur

```
framework/arena/   Reine JVM-Logik, KEINE Compose-Abhängigkeit
  Models.kt          Direction, Position, RobotState, Sensors, Action, RobotBrain
  Toolkit.kt          Extension-Functions für Schüler-Bots (Distanz, Richtung, Gegnersuche, Arena-Geometrie) — siehe docs/toolkit-referenz.md
  BotExecutor.kt      Führt RobotBrain.decide() sicher aus (Timeout + Crash-Schutz)
  GameEngine.kt       Tick-Loop, resolveMoves()/resolveShots() (reine Funktionen), Match-State
  BotRegistry.kt      Fasst alle bots.teamX.teamXBots + bots.examples zusammen

framework/ui/       Compose Desktop, konsumiert nur die public API von GameEngine
  App.kt              State-Owner, LaunchedEffect-Tick-Loop, Layout
  ArenaCanvas.kt      Zeichnet Grid + Roboter + Healthbars + Schuss-Linien/Explosionen
  SoundPlayer.kt      Erzeugt Schuss-Sound synthetisch (javax.sound.sampled, kein Audio-Asset)
  Scoreboard.kt / LogPanel.kt / Controls.kt

bots/examples/      Vom Dozenten gepflegte Referenz-Bots (RandomBot, StillstandBot, ChaserBot, FluchtBot, PowerBot)
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
- **`BotExecutor` erzwingt alle `shakeUpEveryTicks` Ticks (Default 25) eine zufällige `Action.Move` statt `decide()` aufzurufen.** Verhindert, dass sich zwei (oder mehr) Bots dauerhaft gegenseitig blockieren, z.B. weil sie jeden Tick aufs selbe besetzte Feld ziehen wollen und die Move-Auflösung das jedes Mal ablehnt. Move statt Shoot/Wait, weil nur eine tatsächliche Positionsänderung so eine Pattsituation aufbricht. 0 deaktiviert das Verhalten. `decideSafely()` gibt dafür ein `BotDecision(action, isShakeUp)` zurück statt nackter `Action` — `GameEngine.step()` hängt bei `isShakeUp == true` ein `" R"` ans Log-Zeilenende, damit im Protokoll erkennbar bleibt, dass die Bewegung vom Framework erzwungen wurde und nicht aus `RobotBrain.decide()` stammt.
- **Move-Auflösung ist snapshot-basiert.** Alle Zielzellen werden gegen den Zustand zu Tick-Beginn geprüft, nicht gegen bereits aktualisierte Positionen. Sonst hinge das Ergebnis von der internen Abarbeitungsreihenfolge der Bot-Liste ab. Kein Swap zwischen zwei sich kreuzenden Bots erlaubt; bei Zielkonflikt (2 Bots wollen ins selbe freie Feld) bewegt sich keiner.
- **Shot-Schaden wird gesammelt, nicht sofort angewendet.** Erst alle Treffer eines Ticks sammeln, dann gemeinsam Schaden anwenden — sonst würde die Abarbeitungsreihenfolge der Schützen beeinflussen, ob ein bereits „totgeschossener“ Bot in diesem Tick noch als gültiges Ziel zählt.
- **Kein Team-/Friendly-Fire-Konzept.** `RobotState.teamName` wird 1:1 aus `RobotBrain.name` übernommen. Die Gruppierung in `BotRegistry.allTeams` (Team A/B/C) ist rein organisatorisch für UI-Anzeige und Backlog — spielerisch ist es ein reines Free-for-all, jeder Bot kämpft für sich. Das ist bewusst so gewählt, um die Engine einfach zu halten; falls das erweitert werden soll, betrifft das `resolveShots()` in `GameEngine.kt`.
- **`RobotState.alive` ist eine abgeleitete Property** (`health > 0`), kein gespeichertes Feld — verhindert inkonsistente Zustände über `copy()` (z.B. `health = 0` ohne `alive` nachzuziehen).
- **Startpositionen werden am Arena-Perimeter berechnet (`computeStartPositions`, deterministisch, kein `Random`) und dann in `startMatch()` zufällig auf die Bots verteilt (`.shuffled()`).** Die deterministische Berechnung bleibt isoliert testbar (Engine-Tests), das Shuffling sorgt dafür, dass nicht z.B. immer `bot-0` denselben Startplatz bekommt.
- **`Toolkit.kt` ist bewusst eine Sammlung von Top-Level-Extension-Functions, keine Klasse/Objekt.** Grund: Schüler sollen `sensors.nearestEnemy()` bzw. `self.directionTo(ziel)` schreiben können, ohne zusätzliche Objekt-Instanz oder Static-Import-Gymnastik — ein einziger `import framework.arena.*` reicht, weil die Funktionen im selben Package wie `Models.kt` liegen. Der Zweck der Bibliothek ist ausschließlich, Raster-/Distanzrechnung von den Schülern fernzuhalten (Manhattan-Distanz, Vorzeichen-Dreh beim Fliehen, Sichtlinien-Check), nicht ein neues Architektur-Konzept einzuführen.

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
