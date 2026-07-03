# Euer Bot — Anleitung für Schüler

Diese Anleitung zeigt euch, wie ihr einen eigenen Roboter baut und steuert. Ihr
müsst nichts von der Spiel-Engine, der Grafik oder den Regeln selbst programmieren
— das ist alles schon fertig. Eure einzige Aufgabe: **eine Funktion namens
`decide()` schreiben**, die in jeder Spielrunde einmal aufgerufen wird und sagt,
was euer Roboter als Nächstes tut.

## Das Spielprinzip in drei Sätzen

Das Spielfeld ist ein 10×10-Raster. Jeder Roboter hat 100 Lebenspunkte (HP) und
verliert bei jedem Treffer 10 HP. Das Spiel läuft in **Ticks** (Runden) ab: in
jedem Tick darf jeder noch lebende Roboter genau eine Aktion machen — sich bewegen,
schießen, oder warten.

## Wo schreibt ihr euren Code?

In eurer Team-Datei, z.B. `bots/teama/TeamABots.kt`. Sie sieht am Anfang so aus:

```kotlin
package bots.teama

import framework.arena.Action
import framework.arena.Direction
import framework.arena.RobotBrain
import framework.arena.Sensors

class MeinBot(override val name: String = "Team A - MeinBot") : RobotBrain {
    override fun decide(sensors: Sensors): Action {
        return Action.Move(Direction.SOUTH)
    }
}

val teamABots: List<RobotBrain> = listOf(MeinBot())
```

Ihr müsst nur den **Inhalt von `decide()`** ändern (die Zeile mit
`return Action.Move(...)` und alles, was ihr davor noch braucht). Alles andere in
dieser Datei bleibt normalerweise unverändert.

**Wichtig, leicht zu vergessen:** Legt ihr eine ganz neue Bot-Klasse an, muss sie
unten in die Liste `teamABots = listOf(...)` eingetragen werden — sonst taucht sie
in der App gar nicht in der Auswahl auf, obwohl der Code fehlerfrei kompiliert.

## Wie eine Entscheidung funktioniert

Bei jedem Tick ruft die Engine `decide(sensors)` auf. Ihr bekommt ein `sensors`-
Objekt mit allem, was ihr über die aktuelle Situation wissen dürft, und müsst genau
eine `Action` zurückgeben. Mehr passiert nicht — kein eigener Loop, kein
"warten auf Ereignis", einfach: rein `Sensors`, raus `Action`.

```
   Sensors  --->  decide()  --->  Action
  (was ich sehe)              (was ich tue)
```

---

## 1. Was ihr über die Welt wisst: `sensors`

`sensors` ist euer einziges Fenster zur Welt. Es enthält:

| Feld | Bedeutung |
|---|---|
| `sensors.self` | euer eigener Roboter-Zustand |
| `sensors.others` | Liste aller **noch lebenden** gegnerischen Roboter |
| `sensors.arenaWidth` / `sensors.arenaHeight` | Größe des Spielfelds (10 × 10) |
| `sensors.tick` | die aktuelle Runden-Nummer |

Tote Gegner tauchen in `sensors.others` nicht mehr auf — ihr müsst also nie selbst
prüfen, ob ein Gegner schon besiegt ist.

### Euer eigener Zustand: `sensors.self`

```kotlin
sensors.self.position   // eure Position, z.B. Position(x=3, y=7)
sensors.self.health     // eure aktuellen HP, z.B. 80
sensors.self.id         // eure interne ID, z.B. "bot-0"
```

Beispiel — eure eigene Gesundheit abfragen:

```kotlin
if (sensors.self.health < 20) {
    // wenig HP übrig — hier könnte Flucht-Logik stehen
}
```

### Positionen: `Position(x, y)`

Jede Position hat ein `x` (Spalte) und ein `y` (Zeile). `(0, 0)` ist **oben links**.

**Achtung, wichtige Stolperfalle:** `y` wird **kleiner**, je weiter oben man ist,
und **größer**, je weiter unten. Nach Norden (oben) laufen heißt also: `y` sinkt,
nicht steigt.

```kotlin
val meinePosition = sensors.self.position
val binAmLinkenRand = meinePosition.x == 0
val binAmOberenRand = meinePosition.y == 0
```

### Gegner: `sensors.others`

Eine Liste, jeder Eintrag hat dieselben Felder wie `sensors.self`
(`.position`, `.health`, `.id`).

Beispiel — gibt es überhaupt noch einen Gegner?

```kotlin
val gegner = sensors.others.firstOrNull()
if (gegner == null) {
    // keiner mehr da, z.B. gewonnen
}
```

Beispiel — den nächstgelegenen Gegner finden (Abstand grob über
x-Differenz + y-Differenz gerechnet, das nennt man Manhattan-Distanz):

```kotlin
import kotlin.math.abs

val naechsterGegner = sensors.others.minByOrNull { gegner ->
    abs(gegner.position.x - sensors.self.position.x) +
        abs(gegner.position.y - sensors.self.position.y)
}
```

Beispiel — den schwächsten Gegner finden (wenigste HP):

```kotlin
val schwaechsterGegner = sensors.others.minByOrNull { it.health }
```

---

## 2. Was ihr tun könnt: `Action`

Jede `decide()`-Funktion muss **genau eine** von drei Aktionen zurückgeben:

```kotlin
Action.Move(Direction.NORTH)    // einen Schritt in eine Richtung gehen
Action.Shoot(Direction.EAST)    // in eine Richtung schießen
Action.Wait                     // nichts tun
```

Die vier möglichen Richtungen: `Direction.NORTH`, `Direction.SOUTH`,
`Direction.EAST`, `Direction.WEST` (Norden, Süden, Osten, Westen).

**Achtung, Schreibweise-Falle:** `Action.Wait` schreibt man **ohne** Klammern.
`Action.Move(...)` und `Action.Shoot(...)` brauchen **mit** Klammern eine
Richtung als Parameter. Ohne Klammern bei `Move`/`Shoot`, oder mit Klammern bei
`Wait`, gibt es einen Compile-Fehler.

**Wichtig zu wissen:** Euer Bot schlägt mit einer `Action` nur *vor*, was passieren
soll — die Engine setzt das nach ihren eigenen Regeln um. Wenn ihr z.B.
`Action.Move(Direction.NORTH)` zurückgebt, aber dort steht schon ein anderer
Roboter oder das Feld liegt außerhalb der Arena, passiert einfach nichts. Es gibt
keine Fehlermeldung — der Zug verpufft, und im nächsten Tick dürft ihr wieder
entscheiden.

**Schießen trifft nur in gerader Linie:** Ein Schuss nach `EAST` trifft nur einen
Gegner, der in **exakt derselben Zeile** (gleiches `y`) rechts von euch steht.
Steht kein Gegner in dieser Linie, verpufft der Schuss wirkungslos — es passiert
nichts Schlimmes, aber ihr richtet auch keinen Schaden an.

**Ein Roboter blockiert die Sichtlinie.** Trifft euer Schuss nicht den Gegner,
den ihr im Sinn hattet, sondern steht ein anderer Roboter näher in derselben
Linie, wird stattdessen der getroffen. Ihr könnt also nicht "durch" einen
Roboter hindurchschießen.

**Ihr könnt euch nicht durch andere Roboter hindurchbewegen.** Ein Zielfeld,
auf dem zu Beginn des Ticks noch ein lebender Roboter steht, ist blockiert —
auch wenn dieser Roboter im selben Tick selbst wegzieht. Alle Bewegungswünsche
eines Ticks werden nämlich gleichzeitig gegen den Zustand *vor* dem Tick
geprüft, nicht nacheinander. Daraus folgen drei Fälle:

- **Besetztes Feld:** Ziel war zu Tick-Beginn belegt -> ihr bleibt stehen,
  auch wenn der Bewohner im gleichen Tick wegzieht.
- **Kein Platztausch:** Zwei Roboter, die genau die Plätze tauschen wollen,
  bleiben beide stehen (ist quasi eine Sonderform des besetzten Felds).
- **Zielkonflikt:** Wollen zwei oder mehr Roboter ins selbe freie Feld,
  bewegt sich keiner von ihnen.

---

## 3. Nützliche Kotlin-Bausteine für eure Bot-Logik

Kurzreferenz für Dinge, die in den Beispielen oben vorkommen und die ihr für eure
eigene Logik wiederverwenden könnt:

| Baustein | Beispiel | Bedeutung |
|---|---|---|
| `?:` (Elvis-Operator) | `sensors.others.firstOrNull() ?: return Action.Wait` | "Wenn links `null` rausbekommt, dann mach stattdessen das rechts vom `?:`" |
| `.firstOrNull()` | `sensors.others.firstOrNull()` | erstes Element einer Liste, oder `null` falls leer |
| `.minByOrNull { }` | `sensors.others.minByOrNull { it.health }` | Element mit dem kleinsten Wert (z.B. wenigste HP) |
| `.random()` | `Direction.entries.random()` | zufälliges Element aus einer Liste/Aufzählung |
| `if`/`else` als Ausdruck | `val x = if (a) 1 else 2` | `if`/`else` kann direkt einen Wert liefern, nicht nur verzweigen |
| `when` | siehe `ChaserBot.kt` in `bots/examples/` | wie eine große, übersichtliche `if`/`else if`-Kette |

---

## 4. Vom Framework fertig vorgegebene Beispiel-Bots

In `bots/examples/` liegen vier fertige Bots, die ihr euch als Vorlage anschauen
dürft (aber nicht verändern müsst):

- **`RandomBot.kt`** — bewegt oder schießt zufällig in eine zufällige Richtung.
- **`StillstandBot.kt`** — bewegt sich nie, schießt immer in eine feste Richtung.
  Guter erster Gegner zum Testen, weil er sich nicht wehrt.
- **`ChaserBot.kt`** — verfolgt den nächstgelegenen Gegner und schießt, sobald er
  in Sichtlinie steht.
- **`FluchtBot.kt`** — wie `ChaserBot`, flieht aber bei HP unter 20.

Testet euren eigenen Bot am besten zuerst gegen `StillstandBot` (einfachster
Gegner), dann gegen `ChaserBot` und `FluchtBot` (anspruchsvoller).

---

## 5. Bot testen

```bash
./gradlew run
```

startet die App. Wählt in der Bot-Auswahl euren eigenen Bot und einen Beispiel-Bot
aus `bots/examples`, startet das Match, und schaut im Scoreboard/Log rechts, was
passiert.

**Wenn euer Bot einen Fehler hat** (z.B. Endlosschleife oder Absturz): keine Sorge,
das Spiel stürzt deswegen nicht ab. Euer Bot macht in dem Fall einfach `Wait`, bis
ihr den Fehler behoben und neu gestartet habt.

## 6. Häufige Anfängerfehler

- **Neue Bot-Klasse angelegt, aber nicht in `teamXBots` eingetragen** → Bot
  compiliert, taucht aber nicht in der App-Auswahl auf.
- **`Action.Wait` mit Klammern geschrieben** (`Action.Wait()`) → Compile-Fehler.
- **Richtung verwechselt** — denkt daran: `NORTH` heißt `y` wird kleiner (nach
  oben), `SOUTH` heißt `y` wird größer (nach unten).
- **`sensors.others` als leer angenommen, aber nicht geprüft** — wenn ihr
  `sensors.others.first()` statt `sensors.others.firstOrNull()` benutzt und die
  Liste ist leer, gibt es eine Exception. Lieber immer `firstOrNull()` +
  `?:`-Fallback benutzen (siehe Beispiele oben).

## Wo ihr weitermachen könnt

Das Backlog in [`docs/backlog.md`](backlog.md) enthält alle Aufgaben (User Storys)
mit genauen Anforderungen, sortiert nach Schwierigkeit. Fangt mit Epic 1 an.
