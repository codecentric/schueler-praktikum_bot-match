package framework.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
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
    val listState = rememberLazyListState()

    // Zeigt den neuesten Eintrag oben, ohne reverseLayout: die Liste wird für die
    // Anzeige einfach umgedreht (neuestes zuerst) und bei jeder Änderung zurück
    // an den Anfang (= oben = neuester Eintrag) gescrollt.
    val displayEntries = logEntries.asReversed()
    LaunchedEffect(logEntries.size) {
        listState.scrollToItem(0)
    }

    Box(modifier = modifier.fillMaxHeight()) {
        SelectionContainer {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().border(1.dp, Color.LightGray).background(Color(0xFFFAFAFA))
            ) {
                items(displayEntries) { entry ->
                    Text(text = entry, modifier = Modifier.padding(2.dp))
                }
            }
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(listState),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
        )
    }
}
