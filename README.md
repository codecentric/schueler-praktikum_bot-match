# Bot-Match 2.0 — 3 Tage Kotlin Praktikum

Kotlin/Compose-Desktop-Projekt für ein Schülerpraktikum: Roboter kämpfen auf einem 10×10-Raster gegeneinander. Schüler schreiben ausschließlich Bot-Entscheidungslogik (`RobotBrain.decide()`), das komplette Framework (Engine, Rendering, Kampf-Regeln) ist fertig.

Dieses Repo ist der **technische Teil** des Kurses. Das **pädagogische Material** (Zeitplan, Tagesprogramme, Backlog, Musterlösungen) liegt in [`docs/`](docs/).

> 🔴 **Version 2** dieses Praktikums: Gegenüber der ersten Durchführung (Branch `complex` Version 1.0) wurde der Umfang bewusst durch die Nutzung einer Toolkit-Bibliothek (für Distanz-/Richtungsberechnung statt Handrechnung) vereinfacht, damit der Fokus stärker auf Kotlin- und OOP-Grundlagen liegt statt auf zusätzlicher Mathematik.

<img src="docs/images/screenshot.png" alt="Bot-Match Screenshot" width="600">


## Für den Dozenten: Schnelleinstieg

1. Lies [`docs/dozent/setup.md`](docs/dozent/setup.md) — IntelliJ/JDK-Setup, Projekt öffnen, App starten.
2. Lies [`docs/zeitplan.md`](docs/zeitplan.md) — Stunde-für-Stunde-Ablauf aller 3 Tage.
3. Details je Tag: [`docs/tag-1.md`](docs/tag-1.md), [`docs/tag-2.md`](docs/tag-2.md), [`docs/tag-3.md`](docs/tag-3.md). Für den Framework-Walkthrough an Tag 1: [`docs/dozent/tag-1-framework-walkthrough.md`](docs/dozent/tag-1-framework-walkthrough.md) (Inhalt + Ablauf/Regie in einer Datei).
4. Backlog für die Schüler: [`docs/backlog.md`](docs/backlog.md) — Musterlösungen dazu **nur für dich**: [`docs/dozent/loesungen.md`](docs/dozent/loesungen.md). Erklärung der Beispiel-Bots als Sparringspartner: [`docs/dozent/example-bots.md`](docs/dozent/example-bots.md).
5. Scrum-Board-Vorlage: [`docs/dozent/scrum-board.md`](docs/dozent/scrum-board.md).
6. Technischer Überblick über die Codebasis: [`CLAUDE.md`](CLAUDE.md).
7. Ursprüngliche Anforderungen und Design-Entscheidungen: [`docs/dozent/anforderungen.md`](docs/dozent/anforderungen.md).
8. Zum Ausdrucken: alle Markdown-Dateien liegen auch als PDF in [`docs/pdf/`](docs/pdf/) (Backlog zusätzlich als Story-Karten in Groß, siehe [`docs/pdf/README.md`](docs/pdf/README.md)).
9. Kurze Bewegungspause nach einer Pause: [`docs/dozent/energizer-menschlicher-bot.md`](docs/dozent/energizer-menschlicher-bot.md) (Tag 1), [`docs/dozent/energizer-schnick-schnack-schnuck-turnier.md`](docs/dozent/energizer-schnick-schnack-schnuck-turnier.md) (Tag 3).

## Für Schüler: eigenen Bot bauen

Anleitung mit Beispielen, wie ihr euren Roboter steuert:
[`docs/schueler-framework-guide.md`](docs/schueler-framework-guide.md).
Kurzer Überblick über die wichtigsten Objekte als Klassendiagramm:
[`docs/modell-uebersicht.md`](docs/modell-uebersicht.md).
Fertige Helferfunktionen für Distanz, Richtung und Gegnersuche — selbst
herrechnen ist nicht nötig: [`docs/toolkit-referenz.md`](docs/toolkit-referenz.md).
Denkanstöße je Story, falls ihr nicht weiterkommt: [`docs/story-hinweise.md`](docs/story-hinweise.md).

Kurzer Überblick zu Git/GitHub, dem Versionsverwaltungs-Workflow dieses Praktikums (Branches pro Team, tägliche Pull Requests):
[`docs/git-github-basics.md`](docs/git-github-basics.md).
Kurzer Überblick zu Gradle, dem Build-Tool hinter `./gradlew run/test/build`:
[`docs/gradle-basics.md`](docs/gradle-basics.md).

## Projekt starten

Voraussetzung: JDK 17+ (JetBrains Runtime aus IntelliJ reicht als Basis), Internetzugang beim ersten Build (Gradle lädt Dependencies von Maven Central und bei Bedarf automatisch ein JDK 21 für den Build).

```bash
./gradlew run     # startet die Compose-Desktop-App
./gradlew test    # führt die Engine-Unit-Tests aus
./gradlew build   # kompiliert alles + führt Tests aus
```

In IntelliJ: Projektordner öffnen, Gradle-Import abwarten, dann `framework/Main.kt` → `main()` mit dem grünen Play-Button starten.

## Projektstruktur

```
src/main/kotlin/
  framework/arena/   Engine: Modelle, Spielregeln, Bot-Ausführung (fertig, nicht anfassen)
  framework/ui/       Compose-UI: Arena-Zeichenfläche, Scoreboard, Log, Steuerung (fertig)
  framework/Main.kt   Einstiegspunkt
  bots/examples/      Fertige Beispiel-Bots des Dozenten (Sparringspartner)
  bots/teama|b|c/      Hier schreiben die drei Schülerteams ihre Bots
src/test/kotlin/       Unit-Tests für die Engine
docs/                  Kursmaterial (Zeitplan, Backlog, Lösungen, Setup)
```

## Wie Schüler-Bots ins Spiel kommen

Jedes Team hat eine Datei unter `bots/teamX/TeamXBots.kt` mit einer Bot-Klasse und einer Liste `teamXBots`. Diese Listen werden zentral in [`framework/arena/BotRegistry.kt`](src/main/kotlin/framework/arena/BotRegistry.kt) zusammengeführt — der Dozent muss dafür keinen Schülercode ändern, nur ggf. die Registry erweitern, wenn ein Team mehrere Bot-Klassen anlegt.

Am Ende jedes Tages öffnet jedes Team einen Pull Request von seinem
Team-Branch auf den Basis-Branch des Durchlaufs (z.B. `student-2026_07-A` →
`student-2026_07`) — Details zum Branch-Namensschema und Ablauf siehe
[`docs/git-github-basics.md`](docs/git-github-basics.md). Nach dem Merge
aller drei Teams enthält der Basis-Branch den gemeinsamen Stand, gegen den
sich das Testduell/Finale starten lässt.
