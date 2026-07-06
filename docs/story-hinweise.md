# Hinweise, Taktik & Lösungsgedanken zu den Backlog-Storys

Diese Datei ist eure **Denkhilfe** für die Aufgaben aus [`backlog.md`](backlog.md).
Sie gibt **keine fertige Lösung** vor, sondern hilft euch, selbst auf den
Lösungsweg zu kommen: mit Bildern, Beispielen und Leitfragen.

Lest vorher unbedingt einmal den [Framework-Guide](schueler-framework-guide.md) —
dort steht, was `sensors` enthält und welche `Action`s es gibt. Wenn ihr an einer
Story hängt, sucht sie hier und arbeitet die Leitfragen der Reihe nach ab.

---

## Erst mal die Karte verstehen

Alles dreht sich um Positionen auf einem 10×10-Raster. Wer die Koordinaten nicht
sicher im Kopf hat, verrechnet sich bei **jeder** Story. Also zuerst das hier
verinnerlichen:

<img src="images/arena-koordinaten.png" alt="10x10-Arena mit Koordinaten" width="520">

Die drei Dinge, die euch am häufigsten stolpern lassen:

1. **`(0,0)` ist oben links.** Nicht unten links wie im Mathe-Unterricht.
2. **`y` wächst nach unten.** NORTH (nach oben) heißt `y` wird **kleiner**,
   SOUTH heißt `y` wird **größer**. Das fühlt sich falsch an — ist aber so.
3. **`x` ist die Spalte (links/rechts), `y` die Zeile (oben/unten).**

Kleiner Merksatz für die Richtungen:

```
        NORTH  = y - 1   (nach oben)
WEST = x - 1              EAST = x + 1
        SOUTH  = y + 1   (nach unten)
```

### Die zwei wichtigsten Rechnungen

Fast jede Story braucht eine von diesen beiden. Schreibt sie euch einmal auf,
dann könnt ihr sie überall wiederverwenden.

**"Wie weit ist der Gegner weg?"** — Abstand (Manhattan-Distanz):

```kotlin
import kotlin.math.abs

val abstand = abs(gegner.position.x - self.position.x) +
              abs(gegner.position.y - self.position.y)
```

Das ist die Summe aus "wie viele Schritte nach links/rechts" **plus** "wie viele
nach oben/unten". Genau passend, weil man sich nur gerade (nicht diagonal)
bewegen kann.

**"In welche Richtung liegt der Gegner?"** — Richtungsvektor `dx`/`dy`:

```kotlin
val dx = gegner.position.x - self.position.x   // + = Gegner ist rechts, - = links
val dy = gegner.position.y - self.position.y   // + = Gegner ist unten, - = oben
```

`dx` und `dy` sagen euch, wo der Gegner **relativ zu euch** steht. Fast die
gesamte Bewegungs- und Schuss-Logik im ganzen Backlog ist am Ende nur:
*schau dir `dx` und `dy` an und leite daraus eine Richtung ab.*

> **Wichtige Falle beim Vorzeichen:** Wollt ihr **zum** Gegner hin, bewegt ihr
> euch in Richtung von `dx`/`dy`. Wollt ihr **weg** (fliehen), dreht ihr das
> Vorzeichen um. Das ist DIE Stelle, an der sich alle mal vertun — bei jeder
> Bewegungs-Story kurz gegenprüfen: "laufe ich gerade hin oder weg?"

---

## Epic 1 — Bewegung & Überleben

### Story 1.1 — Zufällige Bewegung  ·  1 Punkt

**Ziel:** Bot bewegt sich jeden Tick in eine zufällige Richtung. Reiner
"Läuft mein Bot überhaupt?"-Test.

**Leitfragen:**
- Wie bekomme ich alle vier Richtungen als Liste? (Tipp: `Direction.entries`)
- Wie wähle ich davon eine zufällig aus? (Tipp: `.random()`)
- Was muss `decide()` am Ende zurückgeben? Eine `Action.Move(...)`.

**Taktik-Gedanke:** Strategisch bringt Zufallslauf nichts — aber ihr seht sofort,
ob euer Bot in der App auftaucht und sich bewegt. Genau darum geht's hier. Erst
wenn das klappt, lohnen sich die schwereren Storys.

**Häufiger Fehler:** `Action.Move` ohne Richtung, oder `Direction.random()` statt
`Direction.entries.random()`.

---

### Story 1.2 — Am Rand bleiben / Zentrum meiden  ·  2 Punkte

**Ziel:** Bot soll sich nicht in der Mitte aufhalten, sondern Richtung Rand.

**Warum ist das taktisch klug?** In der Mitte kann euch aus **allen vier**
Richtungen jemand treffen. Am Rand fällt schon eine Richtung weg, in der Ecke
sogar zwei:

```
Mitte (schlecht):        Rand (besser):          Ecke (am sichersten):
   ↑                          ↑                       ↑
 ← ● →   4 Angriffs-       ← ● →   3 Seiten          ● →   nur 2 Seiten
   ↓       seiten            (Wand unten)            ↓       offen
                          ─────────             ─────────
```

**Leitfragen:**
- Wo ist die Mitte? `sensors.arenaWidth / 2` und `sensors.arenaHeight / 2`.
- Wie weit bin ich von der Mitte weg? Mit `abs(pos.x - mitteX)`.
- Wenn ich zu nah dran bin: in welche Richtung ist der Rand näher — links oder
  rechts? Vergleicht `pos.x` mit `mitteX`.

**Taktik-Gedanke:** Ihr müsst es nicht perfekt machen. Es reicht schon, wenn ihr
**eine** Achse betrachtet: "Stehe ich links von der Mitte? → weiter nach WEST.
Sonst → nach EAST." Wer mag, macht dasselbe zusätzlich für oben/unten.

---

### Story 1.3 — Flucht bei niedriger Gesundheit  ·  3 Punkte

**Ziel:** Sinkt `health` unter 20, läuft der Bot **weg** vom nächsten Gegner.

**Das ist der erste "wenn ... dann anders"-Bot.** Ihr baut eine Weiche:

```
          health < 20 ?
         /            \
       ja              nein
        |                |
   FLIEHEN          normal weiter
  (weg vom Gegner)  (z.B. Zufall / Angriff)
```

**Leitfragen:**
- Wie finde ich den nächsten Gegner? → `sensors.others.minByOrNull { ... abstand ... }`
- Was, wenn gar kein Gegner mehr da ist? → `minByOrNull` gibt `null`; mit
  `?: return Action.Wait` abfangen (sonst Absturz!).
- In welche Richtung ist "weg"? → `dx`/`dy` ausrechnen und in die Richtung mit
  dem **umgekehrten** Vorzeichen laufen.

**Der Kern-Trick der Flucht:** Ihr könnt pro Tick nur **eine** Richtung laufen,
nicht diagonal. Also: schaut, wo der größere Abstand ist (`abs(dx)` vs `abs(dy)`)
und flieht zuerst auf dieser Achse.

```
Gegner G, ich ●. dx = 4 (Gegner weit rechts), dy = 1 (kaum drüber).
→ abs(dx) > abs(dy) → auf der x-Achse fliehen → nach WEST.

●  ·  ·  ·  G      Nächster Tick wieder neu rechnen.
```

**Häufiger Fehler:** Vorzeichen verdreht → Bot rennt beim Fliehen genau **auf**
den Gegner zu. Immer testen: HP runter, läuft er wirklich weg?

---

## Epic 2 — Angriff

### Story 2.1 — Dauerfeuer in feste Richtung  ·  1 Punkt

**Ziel:** Bot schießt jeden Tick in dieselbe feste Richtung.

**Leitfragen:**
- Welche `Action` schießt? → `Action.Shoot(Direction.EAST)`.
- Warum trefft ihr damit fast nie? → Ein Schuss trifft nur, wenn ein Gegner
  **exakt** in derselben Zeile (bei EAST/WEST) bzw. Spalte (bei NORTH/SOUTH)
  steht. Zufällig passt das selten.

**Taktik-Gedanke:** Testet gegen `StillstandBot` — der bewegt sich nicht, also
könnt ihr euch so hinstellen, dass die Schüsse treffen. Diese Story ist nur zum
Kennenlernen von `Shoot`; die "richtige" Zielerei kommt in 2.2.

---

### Story 2.2 — Auf Gegner in Sichtlinie zielen  ·  3 Punkte

**Ziel:** Nur schießen, wenn ein Gegner **wirklich** in der Schusslinie steht —
und dann in die richtige Richtung.

**Was heißt "in Sichtlinie"?** Auf dem Raster gibt es keine schrägen Schüsse.
Ihr trefft jemanden nur, wenn er in derselben **Spalte** (gleiches `x`) oder
derselben **Zeile** (gleiches `y`) steht:

```
       G                 gleiches x wie ● → Schuss nach NORTH trifft G
       ·
       ●  ·  ·  G        gleiches y wie ● → Schuss nach EAST trifft den
                          rechten G

  G kann NICHT getroffen werden, wenn er weder in der Zeile noch
  in der Spalte steht (schräg):
       ·  ·  G
       ●  ·  ·           weder gleiches x noch gleiches y → kein Schuss
```

**Leitfragen:**
- Wie filtere ich auf treffbare Gegner? → `sensors.others.filter { es.position.x == self.x || es.position.y == self.y }`
- Wenn mehrere treffbar sind: welchen nehme ich? → den nächsten (`minByOrNull { abstand }`).
- Wie leite ich aus der Position die Schussrichtung ab? → gleiches `x` und Gegner
  hat kleineres `y` → NORTH; größeres `y` → SOUTH; gleiches `y` und größeres `x`
  → EAST; sonst WEST.
- Kein Gegner in Linie? → **nicht** ins Leere schießen, lieber `Action.Wait`
  (oder in 2.3: hinlaufen).

**Häufiger Fehler:** Erst den nächsten Gegner suchen und **dann** prüfen, ob er
in Linie steht. Besser umgekehrt: **erst filtern** (wer ist überhaupt treffbar),
**dann** den nächsten davon nehmen. Sonst zielt ihr auf jemanden, den ihr gar
nicht treffen könnt.

---

### Story 2.3 — Gegner verfolgen  ·  3 Punkte

**Ziel:** Steht kein Gegner in Schusslinie → einen Schritt auf den nächsten
zulaufen. Kombiniert mit 2.2 ergibt das einen echten Jäger-Bot.

**Die Gesamtlogik pro Tick:**

```
  nächsten Gegner suchen
          |
  steht er in Linie (gleiches x oder y)?
     /                    \
   ja                      nein
    |                        |
  SCHIESSEN              HINLAUFEN
 (Richtung aus 2.2)   (dx/dy → Schritt zum Gegner)
```

**Leitfragen:**
- "Hinlaufen" ist dasselbe wie "Fliehen" aus 1.3 — nur mit **richtigem** (nicht
  umgekehrtem) Vorzeichen. Welche Achse zuerst? → wieder die mit dem größeren
  Abstand.
- Warum nähert man sich damit "diagonal treppenförmig"? → weil man abwechselnd
  ein Feld in x- und dann in y-Richtung geht, bis man in einer Linie steht.

**Taktik-Gedanke:** Das ist genau der `ChaserBot` aus `bots/examples/`. Wenn ihr
nicht weiterkommt: **nicht abschreiben**, aber die Struktur anschauen und
verstehen, dann selbst nachbauen.

---

## Epic 3 — Strategie & Zustände

### Story 3.1 — Zustandsmaschine (Patrouille / Angriff / Flucht)  ·  5 Punkte

**Ziel:** Bot hat drei klar benannte Zustände und wechselt je nach Lage.

Das ist keine **neue** Logik — es ist das **Aufräumen** von allem aus Epic 1 & 2
in drei saubere Schubladen. Denkt an eine Ampel: je nach Situation ein anderer
Zustand, jeder Zustand macht etwas klar Unterscheidbares.

```
   health < 20  ────────────────►  FLUCHT    (Logik aus Story 1.3)
   und Gegner da?

   Gegner da (aber genug HP) ────►  ANGRIFF   (Logik aus Story 2.2 + 2.3)

   kein Gegner ──────────────────►  PATROUILLE (Logik aus Story 1.1)
```

**Leitfragen:**
- Wie stelle ich drei Zustände dar? → am saubersten mit `enum class BotState { PATROUILLE, ANGRIFF, FLUCHT }`.
- Wie bestimme ich den Zustand? → mit einem `when { ... }`, das die aktuelle Lage
  prüft. **Reihenfolge zählt:** zuerst FLUCHT prüfen, dann ANGRIFF, dann Rest.
- Warum zuerst Flucht? → Ein fast toter Bot soll fliehen, **auch wenn** ein
  Gegner in Schussweite steht. Prüft ihr Angriff zuerst, stirbt er beim Angreifen.

**Taktik-Gedanke:** Muss der Zustand gespeichert werden? Nein — es reicht, ihn
**jeden Tick neu** aus `sensors` zu berechnen. Das macht den Bot robust: er
reagiert immer auf die aktuelle Lage.

---

### Story 3.2 — Zielpriorisierung bei mehreren Gegnern  ·  3 Punkte

**Ziel:** Bei mehreren Gegnern nicht wahllos den erstbesten angreifen, sondern
nach einer klaren Regel auswählen.

**Der ganze Trick ist ein Zeichen-Tausch.** Bisher habt ihr immer den *nächsten*
Gegner gesucht:

```kotlin
val ziel = sensors.others.minByOrNull { abstand(...) }     // nächster
```

Jetzt tauscht ihr nur das Kriterium in den `{ }`:

```kotlin
val ziel = sensors.others.minByOrNull { it.health }        // schwächster
```

**Leitfragen:**
- Welche Regel wollt ihr? Mögliche Kriterien:
  - **schwächster zuerst** (`it.health`) — schaltet ihr am schnellsten aus einem
    Kampf aus.
  - **nächster zuerst** (`abstand`) — am leichtesten zu erreichen/treffen.
- Egal welches — schreibt als Kommentar dazu, **warum** ihr es gewählt habt.
- Der Rest (schießen/hinlaufen) bleibt exakt wie in Epic 2, nur mit `ziel` statt
  `nächster`.

**Häufiger Fehler:** Nur das Kriterium ändern, aber vergessen, danach überhaupt
zu schießen/laufen. Denkt dran, die Aktion aus 2.2/2.3 anzuhängen.

---

### Story 3.3 — Kür-Aufgabe (freie Idee)  ·  5 Punkte

**Ziel:** Eigene Idee, die über die Vorgaben hinausgeht. Ihr formuliert die Story
selbst, der Dozent bestätigt kurz die Machbarkeit.

**Was geht gut mit der API?**
- **An der Wand entlang** patrouillieren (Position mit `arenaWidth`/`arenaHeight`
  vergleichen).
- **Gegner in die Ecke drängen** (Bewegung relativ zu mehreren `others`).
- **"Nur schießen, wenn sicher"** — z.B. nur feuern, wenn genau **ein** Gegner in
  Linie steht, nicht mehrere.
- **Gegner-Zug merken** — mit einer `var`-Property speichert ihr die letzte
  Position eines Gegners und schätzt, wohin er läuft. (Vorher mit Dozent kurz
  besprechen.)

**Was geht NICHT?**
- Ihr könnt **nicht in die Zukunft sehen.** `sensors` zeigt nur den Zustand
  **jetzt**, bevor sich alle bewegen. Ein Schuss auf ein laufendes Ziel ist immer
  eine **Wette**, kein sicherer Treffer.
- Ihr könnt nicht sehen, was ein Gegner als Nächstes *entscheidet* — nur, wo er
  gerade steht.

**Taktik-Gedanke:** Fangt klein an. Eine simple, funktionierende Idee ist mehr
wert als eine geniale, die nicht läuft. Baut auf eurem Epic-3.1-Bot auf und fügt
**einen** cleveren Zusatz hinzu.

---

## Epic 4 — Qualität (optional)

### Story 4.1 — Unit-Test für eure Logik  ·  2 Punkte

**Ziel:** Ein Test, der `decide()` mit selbst gebautem `Sensors` aufruft und das
Ergebnis prüft.

**Warum geht das so einfach?** `decide()` ist eine **reine Funktion**: rein
`Sensors`, raus `Action`. Ihr braucht keine laufende App — ihr baut einfach von
Hand eine Situation und prüft, was rauskommt.

**Leitfragen:**
- Wie baue ich eine Test-Situation? → `RobotState(...)` und `Sensors(...)` sind
  normale `data class`-Objekte, die ihr direkt erzeugen könnt.
- Welche Situation teste ich? → z.B. Gegner steht östlich in gleicher Zeile →
  erwartet: `Action.Shoot(Direction.EAST)`.
- Wo muss die Datei liegen? → `src/test/kotlin/bots/teamX/...` (Ordner ggf. neu
  anlegen), `package` muss zum Pfad passen.

**Taktik-Gedanke:** Testet einen Fall, bei dem ihr die Antwort **sicher** wisst
(z.B. Gegner exakt rechts). Dann seht ihr sofort, ob eure Richtungslogik stimmt.

### Story 4.2 — Testduell protokollieren  ·  1 Punkt

Kein Code. App starten, euren Bot + einen Beispiel-Bot wählen, Match laufen
lassen, Ergebnis in 1-2 Sätzen notieren.

---

## Epic 5 — Turniervorbereitung

### Story 5.1 — Finale Version festlegen  ·  1 Punkt

Stellt sicher, dass in `teamXBots = listOf(...)` **genau** die Bot-Version steht,
die antreten soll — nicht drei halbfertige Zwischenstände. `./gradlew build` muss
grün sein.

### Story 5.2 — Strategie in 1-2 Sätzen  ·  1 Punkt

Könnt ihr in eigenen Worten sagen, **wann** euer Bot angreift, flieht,
patrouilliert? Wenn ja: fertig. Wenn nicht: das ist ein Zeichen, dass euer Bot
vielleicht noch zu unübersichtlich ist — gute Gelegenheit zum Aufräumen.

---

## Wenn ihr komplett feststeckt

1. **Läuft der Bot überhaupt?** Zurück zu Story 1.1 — taucht er in der Auswahl
   auf, bewegt er sich?
2. **Absturz?** Meist `sensors.others` leer und trotzdem `.first()` benutzt.
   Immer `firstOrNull()` / `minByOrNull()` + `?:`-Fallback.
3. **Läuft/schießt falsch herum?** Vorzeichen von `dx`/`dy` prüfen und dran
   denken: `y` wächst nach **unten**.
4. **Schießt nie?** Steht ein Gegner wirklich in gleicher Zeile/Spalte? Testet
   erst gegen `StillstandBot`, da ist es kontrollierbar.
5. Schaut euch die fertigen Bots in `bots/examples/` an — **nicht abschreiben**,
   sondern verstehen und selbst nachbauen.
