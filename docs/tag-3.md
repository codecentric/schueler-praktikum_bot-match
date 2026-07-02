# Tag 3 — Feinschliff, Turnier, Review

Ziel des Tages: Alle drei Team-Bots sind beim Dozenten integriert, ein Battle-Royale-Finale läuft, der Sprint wird mit Review und Retrospektive abgeschlossen.

## 08:00–08:15 — Daily Standup

Letztes Daily des Sprints. Fokus: "Was fehlt noch bis zur finalen Bot-Version (Story 5.1)?"

## 08:15–09:45 — Arbeitsblock 1: Feinschliff/Bugfixing

- Letzte Anpassungen an der eigenen Bot-Logik.
- Story 5.1 (Finale Bot-Version festlegen) abschließen: `teamXBots`-Liste enthält genau die Bots, die antreten sollen.
- Dozent sammelt final alle drei `bots/teamX/`-Ordner ein (falls nicht schon an Tag 2 geschehen) und kopiert sie in sein eigenes Projekt.

## 10:00–11:30 — Arbeitsblock 2: Integration

- Dozent führt `./gradlew build` mit allen drei integrierten Team-Ordnern aus, behebt eventuelle Compile-Konflikte (z.B. doppelte Klassennamen zwischen Teams — sollte durch getrennte Packages `bots.teama/b/c` nicht vorkommen, aber sicherheitshalber prüfen).
- Während der Integration: Teams bereiten ihre Strategie-Kurzvorstellung vor (Story 5.2, 1-2 Sätze).
- Sobald Integration steht: kurzer technischer Probelauf (Dozent), damit während der Proberunden mit Schülern keine Überraschungen auftreten.

## 11:30–12:15 Mittagspause

## 12:15–13:30 — Arbeitsblock 3: Turnier-Proberunden

- Einzelne Testduelle zwischen den drei finalen Team-Bots (nicht das große Finale, sondern Aufwärmen — jedes Team sieht, wie sein Bot gegen die anderen abschneidet, bevor es "zählt").
- Bei Bedarf: letzte kleine Anpassungen, falls ein offensichtliches Problem auffällt (z.B. Bot bewegt sich nur in eine Ecke und bleibt dort stecken).

## 13:45–14:45 — Arbeitsblock 4: Finale — Battle Royale

- Alle drei (oder mehr, falls Teams mehrere Bots eingetragen haben) Team-Bots gleichzeitig in der App auswählen, Speed moderat einstellen (z.B. 300-500ms, gut zum Zuschauen).
- Match laufen lassen, gemeinsam zuschauen und kommentieren.
- Bei Bedarf mehrere Runden (Battle Royale ist nicht deterministisch bei zufälligem Bot-Verhalten wie `RandomBot`-Anteilen — ein zweiter Durchlauf zeigt, ob das Ergebnis Zufall oder Strategie war).

## 14:45–15:00 — Sprint Review + Retrospektive

**Sprint Review (siehe [scrum-board.md](scrum-board.md)):**
- Jedes Team stellt seine Strategie kurz vor (Story 5.2).
- Kurzes Feedback aus der Runde.

**Retrospektive:**
- Zwei Fragen, Antworten auf Post-its ans Board: "Was lief gut?" / "Was würden wir nächstes Mal anders machen?"
- Kurze gemeinsame Sichtung, Praktikum-Abschluss.
