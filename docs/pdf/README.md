# PDF-Export für `docs/backlog.md`

Erzeugt zwei druckbare PDF-Varianten aus dem Backlog. Quelle ist immer
`docs/backlog.md` — die Skripte hier verändern das Markdown nicht.

## Voraussetzungen

- [Pandoc](https://pandoc.org/) (getestet mit 3.10)
- LaTeX mit `xelatex` (z.B. via [MacTeX](https://tug.org/mactex/)) sowie den
  Paketen `tcolorbox`, `newunicodechar`, `enumitem` — bei einer vollständigen
  TeX-Live-/MacTeX-Installation bereits enthalten
- Font "Helvetica Neue" (auf macOS vorinstalliert)

## Varianten

### `build.sh` → `docs/backlog.pdf`

Hochformat, A4, jede Story in einer Box, mehrere Storys pro Seite,
Inhaltsverzeichnis. Zum normalen Durchlesen/Ausdrucken als Heft.

```bash
docs/pdf/build.sh
```

### `build-landscape.sh` → `docs/backlog-landscape.pdf`

Querformat, A4, genau eine Story pro Seite, große Schrift. Für Story-Karten
zum Ausschneiden/Auslegen (z.B. Scrum-Board).

```bash
docs/pdf/build-landscape.sh
```

## Dateien

| Datei | Zweck |
|---|---|
| `build.sh` | Baut `backlog.pdf` (Hochformat) |
| `build-landscape.sh` | Baut `backlog-landscape.pdf` (Querformat, 1 Story/Seite) |
| `header.tex` | LaTeX-Box-Styling für die Hochformat-Variante |
| `header-landscape.tex` | LaTeX-Box-Styling für die Querformat-Variante |
| `storybox.lua` | Pandoc-Filter: umschließt jede `###`-Story mit einer Box |
| `storybox-landscape.lua` | Wie oben, zusätzlich ein Seitenumbruch pro Story |

## Nach Änderungen an `backlog.md`

Beide Skripte einfach erneut ausführen — die PDFs werden komplett neu erzeugt.
