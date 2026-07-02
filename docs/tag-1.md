# Tag 1 — Framework kennenlernen, erste Bots

Ziel des Tages: Jedes Team hat einen lauffähigen Bot, der sich bewegt UND schießt, und hat mindestens ein Testduell erfolgreich durchgeführt.

## 08:00–08:15 — Sprint Planning

- Kurzer Rückblick: was war Tag 1/2 (Kotlin-Grundlagen)?
- Sprint-Ziel vorstellen: *"Bot mit mindestens zwei Verhaltensregeln, der ein Testduell übersteht."*
- Backlog-Karten (Epic 1 + 2) liegen bereits am Board (siehe [scrum-board.md](scrum-board.md)). Jedes Team wählt 2-3 Storys aus Epic 1 zum Einstieg.

## 08:15–09:45 — Arbeitsblock 1: Framework-Walkthrough

Dozentengeführt, alle Teams gemeinsam:
1. App einmal live starten (`./gradlew run`), Beispiel-Bots (`RandomBot` vs. `ChaserBot`) gegeneinander laufen lassen — Schüler sehen live, was am Ende funktionieren soll.
2. Domain-Modell erklären anhand von `Models.kt`: `Direction`, `Position`, `RobotState`, `Sensors`, `Action`, `RobotBrain`. Betonen: **Schüler schreiben nur `decide()`**, alles andere ist fertig.
3. Kurzer Blick in `bots/teama/TeamABots.kt` (o.ä. für die eigene Gruppe): wo trage ich meinen Code ein, wie heißt die Liste, die ich pflegen muss.
4. Erste Übung gemeinsam an der Tafel/am Beamer: `RandomBot`-Logik nachvollziehen (liegt in `bots/examples/RandomBot.kt`), Zeile für Zeile durchgehen.

Didaktischer Hinweis: Die Konzepte `sealed interface` (für `Action`) und `enum class` mit Property (`Direction.dx/dy`) sind für die Schüler evtl. neu — kurz erklären, aber nicht vertiefen, sie müssen nur *benutzen*, nicht selbst definieren können.

## 09:45–10:00 Pause

## 10:00–11:30 — Arbeitsblock 2: Erste Bots (Epic 1)

Teamarbeit, Pair-Programming (wie an Tag 1/2 eingeführt: zwei Schüler pro Rechner, wechseln sich als "Driver"/"Navigator" ab).

- Story 1.1 (Zufällige Bewegung) als Einstieg — jedes Team bekommt seinen Bot zum ersten Mal ans Laufen.
- Story 1.2 (Rand halten) für Teams, die schneller fertig sind.
- Dozent geht herum, hilft bei Compile-Fehlern (häufigster Fehler: neue Bot-Klasse nicht in `teamXBots`-Liste eingetragen).

**Zwischenziel bis 11:30:** jeder Bot bewegt sich sichtbar in der App.

## 11:30–12:15 Mittagspause

## 12:15–13:30 — Arbeitsblock 3: Angriffslogik (Epic 2)

- Story 2.1 (Dauerfeuer feste Richtung) — einfacher Einstieg ins Schießen.
- Story 2.2 (Zielen auf Gegner in Sichtlinie) — anspruchsvoller, ggf. mit Dozenten-Hilfe pro Team.
- Schnelle Teams: Story 2.3 (Verfolgen) direkt ergänzen.

## 13:30–13:45 Pause

## 13:45–14:45 — Arbeitsblock 4: Erste Testduelle

- Jedes Team lässt seinen Bot gegen einen Beispiel-Bot (`bots/examples`) antreten, im Plenum oder team-intern.
- Beobachtungen im Scoreboard/Log gemeinsam anschauen: Warum hat der Bot verloren/gewonnen? Was würde man ändern?
- Karten am Board von "In Progress"/"Review" nach "Done" verschieben, sobald ein Team sein Testduell erfolgreich durchgeführt hat (= Akzeptanzkriterium erfüllt).

## 14:45–15:00 — Tagesrückblick

- Kurze Runde: Was hat funktioniert, wo gab es Stolpersteine?
- Ausblick auf Tag 2: Zustände/Strategie (Epic 3), Feinschliff.
