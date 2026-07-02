# Scrum-Board — Vorlage

Ein gemeinsames physisches Board für alle drei Teams (Whiteboard oder Pinnwand mit Post-its). Kein digitales Tool nötig — Fokus soll auf Kotlin-Code bleiben, nicht auf Tool-Einarbeitung.

## Spalten

```
| Backlog | To Do | In Progress | Review (Pair-Check) | Done |
```

- **Backlog:** alle noch nicht ausgewählten User Storys aus [backlog.md](backlog.md) (als Karten vorbereitet, siehe unten)
- **To Do:** von einem Team für die aktuelle Session ausgewählt
- **In Progress:** wird gerade bearbeitet
- **Review (Pair-Check):** fertig codiert, wird von einem zweiten Teammitglied gegengelesen/getestet, bevor sie als fertig gilt (das ist das im Kurs eingeführte Pair-Programming-/Review-Prinzip in Kurzform)
- **Done:** akzeptiert (Akzeptanzkriterien aus dem Backlog erfüllt, im Testduell ausprobiert)

## Karten

Jede Karte = eine User Story. Auf der Karte notieren:
- Story-Titel (aus dem Backlog)
- Team-Kürzel als Farbe oder Buchstabe: **A** / **B** / **C**
- Story Points (1/2/3/5)

Vorbereitung durch den Dozenten vor Tag 1: alle Storys aus `backlog.md` als Karten in die Backlog-Spalte, ohne Team-Zuordnung — jedes Team zieht sich seine Storys im Sprint Planning selbst.

## Sprint Planning (Tag 1, 08:00–08:15)

Kurz halten, das Backlog ist schon vorbereitet:
1. Dozent erklärt kurz Sprint-Ziel: *"Bot mit mindestens zwei Verhaltensregeln, der ein Testduell übersteht."*
2. Jedes Team wählt 2-3 Storys aus Epic 1 für den Start (siehe backlog.md — Reihenfolge der Epics ist als Empfehlung gedacht, Teams dürfen aber selbst entscheiden, auch quer durch die Epics).
3. Karten wandern von Backlog nach To Do, mit Team-Buchstabe markiert.

## Daily Standup (Tag 2 + 3, 08:00–08:15)

Reihum, ein Sprecher pro Team, max. 2 Minuten pro Team, 3 Fragen:
1. Was haben wir seit gestern geschafft?
2. Was nehmen wir uns für heute vor?
3. Wo hakt es / brauchen wir Hilfe?

Der Dozent moderiert, greift Blocker sofort danach in kleiner Runde auf (nicht im Daily selbst diskutieren, das sprengt die Zeit).

## Grooming (Tag 2, im Arbeitsblock "Testduelle + Grooming")

Kurzer Check (5 Min): Sind noch passende Storys im Backlog für den Rest von Tag 2 und Tag 3? Bei Bedarf motiviert der Dozent Teams, eigene Kür-Storys zu formulieren (freie Kreativität ist ausdrücklich erwünscht, siehe Epic 3 "Kür-Aufgabe" im Backlog).

## Sprint Review (Tag 3, 14:45–15:00)

Kein Foliensatz nötig. Ablauf:
1. Battle-Royale-Finale läuft (alle Teams schauen zu).
2. Jedes Team stellt in 1-2 Sätzen seine Strategie vor: "Unser Bot greift an, solange er über 20 HP hat, sonst flieht er in die nächste Ecke."
3. Kurzer Applaus/Feedback-Runde.

## Retrospektive (direkt danach, 5-10 Min)

Zwei Fragen an alle, Antworten kurz auf Post-its sammeln und am Board anpinnen:
- Was lief gut?
- Was würden wir nächstes Mal anders machen?
