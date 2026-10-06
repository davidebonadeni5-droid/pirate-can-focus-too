package dev.mathieuburnat.piratefocus.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import dev.mathieuburnat.piratefocus.data.ShipClock
import dev.mathieuburnat.piratefocus.focus.FocusTimer
import dev.mathieuburnat.piratefocus.focus.PirateQuotes
import dev.mathieuburnat.piratefocus.logbook.Logbook
import dev.mathieuburnat.piratefocus.logbook.Voyage

private const val BAR_WIDTH = 16

/** Le carnet de bord : les traversées passées, la série de jours et la semaine en barres ASCII. */
@Composable
fun LogbookScreen(history: List<Voyage>, onBack: () -> Unit) {
    val today = remember { ShipClock.dayOf(ShipClock.now()) }
    val stats = remember(history) { Logbook.stats(history, today, ShipClock::dayOf) }
    val week = remember(history) { Logbook.lastDays(history, today, ShipClock::dayOf) }
    val recent = remember(history) { history.asReversed().take(50) }
    val maxMinutes = week.maxOf { it.second }.coerceAtLeast(1)

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.safeDrawingPadding().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "< MENU",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .clickable(onClick = onBack)
                            .padding(vertical = 4.dp),
                    )
                    Text("=== CARNET DE BORD ===", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "« ${PirateQuotes.streak(stats.currentStreak)} »",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(2.dp, MaterialTheme.colorScheme.onBackground), RectangleShape)
                            .padding(12.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    StatLine("série en cours", "${stats.currentStreak} j")
                    StatLine("meilleure série", "${stats.bestStreak} j")
                    StatLine("aujourd'hui", "${stats.todayMinutes} min")
                    StatLine("traversées réussies", "${stats.voyages}")
                    StatLine("temps en mer", "${stats.minutes / 60} h ${FocusTimer.pad(stats.minutes % 60)}")
                    StatLine("naufrages", "${stats.sunk}")

                    Spacer(Modifier.height(16.dp))
                    Text("-- Les 7 derniers jours --", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    week.forEach { (day, minutes) ->
                        val filled = (minutes * BAR_WIDTH + maxMinutes - 1) / maxMinutes
                        Text(
                            "${Logbook.weekday(day)} |${"#".repeat(filled)}${".".repeat(BAR_WIDTH - filled)}| ${minutes}m",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (day == today) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("-- Dernières traversées --", style = MaterialTheme.typography.titleSmall)
                    if (recent.isEmpty()) {
                        Text(
                            "Le carnet est vierge. Lève l'ancre pour écrire la première page !",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 12.dp),
                        )
                    }
                }
            }
            items(recent) { voyage ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(ShipClock.shortDateTime(voyage.endedAtMillis), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.weight(1f))
                    Text(
                        if (voyage.completed) "${voyage.minutes} min  ⛵" else "${voyage.minutes} min  ☠ coulé",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (voyage.completed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text("> $label", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
    }
}

