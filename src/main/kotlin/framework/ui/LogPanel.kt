package framework.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Scrollbares Event-Log. Neueste Einträge stehen oben, damit man beim Zuschauen
 * nicht selbst scrollen muss, um die aktuellsten Ereignisse zu sehen.
 *
 * Hinweis: Die übergebene [logEntries]-Liste sollte vom Aufrufer selbst schon
 * auf eine sinnvolle Maximallänge begrenzt werden (siehe App.kt: takeLast(500)),
 * damit der Speicherverbrauch bei langen Matches nicht unbegrenzt wächst.
 */
@Composable
fun LogPanel(logEntries: List<String>, modifier: Modifier = Modifier) {
    // reverseLayout = true zeigt das letzte Element der Liste ganz oben an,
    // ohne dass wir manuell scrollen müssen.
    LazyColumn(
        modifier = modifier.border(1.dp, Color.LightGray).background(Color(0xFFFAFAFA)),
        reverseLayout = true
    ) {
        items(logEntries) { entry ->
            Text(text = entry, modifier = Modifier.padding(2.dp))
        }
    }
}
