# Ein Bot, Schritt für Schritt aufgebaut

### Stufe 0 — nichts tun

```kotlin
override fun decide(sensors: Sensors): Action {
    return Action.Wait
}
```

Der einfachste mögliche Bot. Macht nichts, verliert aber auch nie durch eigene
dumme Züge.

### Stufe 1 — zufällig bewegen

```kotlin
override fun decide(sensors: Sensors): Action {
    val richtung = Direction.entries.random()
    return Action.Move(richtung)
}
```

`Direction.entries` ist die Liste aller vier Richtungen, `.random()` wählt eine
zufällig aus. Das entspricht der ersten Backlog-Story (1.1).

### Stufe 2 — immer in eine feste Richtung schießen

```kotlin
override fun decide(sensors: Sensors): Action {
    return Action.Shoot(Direction.EAST)
}
```

Einfach, aber trifft nur Gegner, die zufällig genau östlich von euch in derselben
Zeile stehen.

### Stufe 3 — auf einen Gegner in Sichtlinie zielen

```kotlin
override fun decide(sensors: Sensors): Action {
    val gegner = sensors.others.firstOrNull() ?: return Action.Wait

    return if (sensors.self.position.y == gegner.position.y) {
        // gleiche Zeile — der Gegner ist östlich oder westlich von mir
        if (gegner.position.x > sensors.self.position.x) {
            Action.Shoot(Direction.EAST)
        } else {
            Action.Shoot(Direction.WEST)
        }
    } else {
        Action.Move(Direction.SOUTH)   // sonst irgendwie bewegen
    }
}
```

Prüft, ob ein Gegner in derselben Zeile (`y`) steht, und schießt dann in die
passende Richtung. Sonst bewegt sich der Bot (hier fest nach Süden — das ließe
sich noch verbessern, z.B. in Richtung des Gegners laufen statt fest nach Süden).

### Stufe 4 — bei niedriger Gesundheit fliehen

```kotlin
override fun decide(sensors: Sensors): Action {
    val gegner = sensors.others.firstOrNull() ?: return Action.Wait

    if (sensors.self.health < 20) {
        // fliehen: in die Richtung weg vom Gegner laufen
        return if (sensors.self.position.x > gegner.position.x) {
            Action.Move(Direction.EAST)
        } else {
            Action.Move(Direction.WEST)
        }
    }

    // sonst normal angreifen wie in Stufe 3
    return if (sensors.self.position.y == gegner.position.y) {
        if (gegner.position.x > sensors.self.position.x) {
            Action.Shoot(Direction.EAST)
        } else {
            Action.Shoot(Direction.WEST)
        }
    } else {
        Action.Move(Direction.SOUTH)
    }
}
```

Fragt zuerst die eigene Gesundheit ab (`sensors.self.health < 20`) und verhält sich
dann komplett anders. Das ist das Grundmuster für Story 1.3 und für Story 3.1
(Zustandsmaschine — euer Bot verhält sich unterschiedlich, je nachdem "in welchem
Zustand" er gerade ist, ähnlich einer Ampel).
