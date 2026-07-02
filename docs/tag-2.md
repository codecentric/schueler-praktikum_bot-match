# Tag 2 — Strategie vertiefen, Bot robust machen

Ziel des Tages: Jedes Team hat einen Bot mit mehreren erkennbaren Verhaltenszuständen (mindestens Angriff + Flucht oder Angriff + Patrouille) und hat ihn mehrfach gegen andere Bots getestet.

## 08:00–08:15 — Daily Standup

Format siehe [scrum-board.md](scrum-board.md): 3 Fragen pro Team, max. 2 Min. je Team.

## 08:15–09:45 — Arbeitsblock 1: Vertiefung Strategie

- Story 1.3 (Flucht bei niedriger Gesundheit) nachholen, falls an Tag 1 noch nicht geschafft.
- Einstieg in Story 3.1 (Zustandsmaschine): Dozent erklärt kurz am Beispiel `bots/examples/FluchtBot.kt` (bereits fertig, als Referenz zeigbar — nicht die Musterlösung aus `docs/dozent/loesungen.md` direkt zeigen, Schüler sollen selbst probieren).
- Konzept "Zustand" verständlich machen: "Euer Bot verhält sich unterschiedlich, je nachdem in welcher Situation er gerade ist — wie eine Ampel, die je nach Zustand rot/gelb/grün zeigt."

## 09:45–10:00 Pause

## 10:00–11:30 — Arbeitsblock 2: Zustandsmaschinen (Epic 3)

Teamarbeit:
- Story 3.1 umsetzen (mind. 3 Zustände: Patrouille/Angriff/Flucht oder eigene Benennung).
- Story 3.2 (Zielpriorisierung) für Teams, die mehrere Gegner gleichzeitig behandeln wollen.
- Dozent achtet besonders auf Lesbarkeit: `when`-Ausdrücke statt verschachtelte `if`-Ketten, sprechende Namen für Zustände.

## 11:30–12:15 Mittagspause

## 12:15–13:30 — Arbeitsblock 3: Testduelle + Backlog-Grooming

- Jedes Team lässt seinen aktuellen Bot gegen mindestens 2 verschiedene Beispiel-Bots antreten (Story 4.2).
- Kurzes Grooming am Board (siehe scrum-board.md): reicht das Backlog noch für den Rest des Tages und Tag 3? Teams, die schon alle Pflicht-Storys geschafft haben, formulieren eine Kür-Story (3.3).
- Dozent sammelt Beobachtungen: gibt es Bugs im Framework selbst (nicht im Schülercode), die noch behoben werden müssen, bevor Tag 3 startet?

## 13:30–13:45 Pause

## 13:45–14:45 — Arbeitsblock 4: Optional — Unit-Tests (Epic 4) / Feinschliff

- Wer mag: Story 4.1 (eigener Unit-Test) ausprobieren — guter Anlass, das an Tag 1/2 eingeführte Unit-Test-Konzept nochmal in einem für die Schüler relevanten Kontext zu üben.
- Wer nicht: Zeit für weiteren Feinschliff der Bot-Logik, weitere Testduelle, Vorbereitung auf Story 3.3 (Kür).
- Gegen Ende: Dozent bittet alle Teams, ihre `bots/teamX/`-Dateien in einen gemeinsamen Ordner zu kopieren (Vorbereitung für die Integration an Tag 3, siehe [setup.md](setup.md) Abschnitt "Integration am Turniertag").

## 14:45–15:00 — Tagesrückblick

- Kurze Runde: aktueller Stand jedes Teams, was ist für Tag 3 noch offen?
- Hinweis auf Tag 3: Vormittag = letzter Feinschliff, Nachmittag = Turnier + Review.
