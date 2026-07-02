package framework.arena

/**
 * Zentrale Stelle, an der der Dozent alle Team-Bot-Listen sowie die
 * Beispiel-Bots zusammenführt, ohne Schülercode anfassen zu müssen.
 *
 * Hinweis zur Team-Zuordnung: `GameEngine.startMatch()` setzt `RobotState.teamName`
 * aktuell 1:1 auf `RobotBrain.name` - es gibt (noch) keine eigene Struktur, die
 * mehrere Bot-Instanzen zu einem gemeinsamen "Team" gruppiert. Die Gliederung
 * hier in [allTeams] ist daher rein organisatorisch (zur Anzeige/Auswahl in der
 * UI), hat aber keine Auswirkung auf Spielregeln (z.B. Freund-Feind-Erkennung
 * beim Schießen gibt es nicht - jeder Bot kämpft effektiv für sich allein).
 */
object BotRegistry {
    val allTeams: Map<String, List<RobotBrain>> = mapOf(
        "Team A" to bots.teama.teamABots,
        "Team B" to bots.teamb.teamBBots,
        "Team C" to bots.teamc.teamCBots,
        "Beispiele" to listOf(
            bots.examples.RandomBot(),
            bots.examples.StillstandBot(),
            bots.examples.ChaserBot(),
            bots.examples.FluchtBot()
        )
    )

    fun allBots(): List<RobotBrain> = allTeams.values.flatten()
}
