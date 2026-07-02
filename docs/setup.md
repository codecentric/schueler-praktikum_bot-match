# Setup — Für den Dozenten

Diese Anleitung geht davon aus, dass IntelliJ IDEA bereits an Tag 1/2 installiert und getestet wurde.

## Voraussetzungen

- IntelliJ IDEA (Community reicht) mit Kotlin-Plugin (ist standardmäßig aktiv)
- JDK 17 oder neuer (die JetBrains Runtime, die mit IntelliJ mitgeliefert wird, reicht völlig — keine separate Installation nötig)
- Internetzugang beim ersten Öffnen des Projekts (Gradle lädt Compose- und Kotlin-Dependencies von Maven Central und dem JetBrains-Repo herunter, danach liegt alles im lokalen Gradle-Cache und funktioniert auch offline)

## Projekt öffnen

1. IntelliJ starten → **Open** → den Ordner `robot-games` auswählen (nicht eine einzelne Datei).
2. IntelliJ erkennt automatisch das Gradle-Projekt und importiert es. Das dauert beim ersten Mal 2-5 Minuten (Dependency-Download). Unten rechts läuft ein Fortschrittsbalken ("Sync" / "Indexing").
3. Falls IntelliJ nach einem JDK fragt: die vorgeschlagene JetBrains-Runtime (JBR) auswählen, oder falls keine automatisch erkannt wird, unter **File → Project Structure → SDK** ein JDK 17+ setzen.

## App starten

**Option A (empfohlen für Schüler):** Datei `src/main/kotlin/framework/Main.kt` öffnen, auf den grünen Play-Button links neben `fun main()` klicken.

**Option B (Terminal):**
```bash
./gradlew run
```

Die App öffnet ein Fenster mit der 10×10-Arena links, Steuerung/Scoreboard/Log rechts. Standardmäßig sind alle Bots aus `BotRegistry` (Beispiel-Bots + aktueller Stand aller drei Teams) als Kandidaten in der Checkbox-Liste auswählbar.

## Tests ausführen

```bash
./gradlew test
```

Führt die Engine-Unit-Tests aus (`GameEngineTest.kt`). Praktisch, um schnell zu prüfen, dass die Kernlogik nach eigenen Anpassungen noch funktioniert — für Schüler-Bot-Code selbst sind keine Pflicht-Tests vorgesehen (siehe optionale Aufgabe in Epic 4 im Backlog).

## Wenn etwas nicht kompiliert

Der häufigste Fehler bei Schülern: eine Bot-Datei hat einen Syntaxfehler oder die neue Bot-Klasse wurde nicht in die `teamXBots`-Liste am Ende der Datei eingetragen. Beides zeigt IntelliJ sofort als roten Unterstrich/Fehlermeldung in der Datei an, bevor überhaupt gebaut wird — das ist gewollt (frühes, sichtbares Feedback statt stiller Laufzeitfehler).

Falls Gradle nach einem Fehler "hängt" oder komisch reagiert: **File → Invalidate Caches → Invalidate and Restart**, danach nochmal Gradle-Sync abwarten.

## Integration am Turniertag (Tag 3)

Die drei Teams arbeiten während des Praktikums idealerweise alle auf demselben Projekt-Stand (z.B. gemeinsamer Ordner auf einem Netzlaufwerk, oder Dozent sammelt am Ende von Tag 2 die drei `bots/teamX/`-Ordner ein und kopiert sie auf seinen Rechner). Vor dem Finale:

1. Sicherstellen, dass jede Team-Datei `bots/teamX/TeamXBots.kt` kompiliert (am besten schon am Ende von Tag 2 einsammeln und einmal `./gradlew build` laufen lassen, damit am Turniertag keine Überraschungen warten).
2. `BotRegistry.kt` muss nicht verändert werden, sofern jedes Team seine Bots brav in die vorgesehene `teamXBots`-Liste einträgt — die Registry liest diese automatisch ein.
3. App starten, alle gewünschten Bots in der Checkbox-Liste auswählen, Battle-Royale laufen lassen.

## Bekannte Stolpersteine

- **Firmen-/Schulnetz blockt Maven Central:** Vor dem Praktikumstag einmal selbst `./gradlew build` im Zielnetz testen. Falls es klemmt, das Projekt vorher in einem funktionierenden Netz einmal komplett bauen (füllt den lokalen Gradle-Cache unter `~/.gradle`) und diesen Cache auf die Praktikumsrechner mitbringen.
- **Mehrere Bot-Klassen pro Team:** kein Problem, einfach weitere Klassen in derselben Datei (oder neue Dateien im selben Package) anlegen und alle in die `teamXBots`-Liste aufnehmen.
- **App reagiert nicht mehr / ein Bot scheint zu hängen:** das Framework fängt Endlosschleifen in Schüler-Code automatisch ab (nach 3 Timeouts wird der betroffene Bot für den Rest des Matches auf "Wait" gesetzt, sichtbar im Log). Die App selbst friert dabei nicht ein — falls doch, ist es ein echter Bug im Framework, nicht im Schülercode.
