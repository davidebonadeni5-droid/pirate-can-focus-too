package dev.mathieuburnat.piratefocus.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mathieuburnat.piratefocus.data.ShipClock
import dev.mathieuburnat.piratefocus.focus.Phase
import dev.mathieuburnat.piratefocus.journal.Categories
import dev.mathieuburnat.piratefocus.journal.Category
import dev.mathieuburnat.piratefocus.journal.Journal
import dev.mathieuburnat.piratefocus.journal.JournalViewModel
import dev.mathieuburnat.piratefocus.journal.Side
import dev.mathieuburnat.piratefocus.journal.Verdict
import dev.mathieuburnat.piratefocus.logbook.Logbook

/** Ce qu'on est en train de modifier dans une boîte de dialogue. */
private sealed interface JournalDialog {
    data class Count(val category: Category, val current: Int) : JournalDialog
    data class NewLine(val side: Side) : JournalDialog
    data class Remove(val category: Category) : JournalDialog
}

@Composable
fun JournalScreen(viewModel: JournalViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val journal = state.journal
    val day = state.day
    val toaster = LocalToaster.current
    var dialog by remember { mutableStateOf<JournalDialog?>(null) }

    // Un seul toast à la fois, même quand on enchaîne les tournées.
    LaunchedEffect(state.toast) {
        state.toast?.let { message ->
            toaster.show(message)
            viewModel.consumeToast()
        }
    }

    state.popup?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::dismissPopup,
            shape = RectangleShape,
            title = { Text("☠ LE CAPITAINE ☠") },
            text = { TypewriterText(message, style = MaterialTheme.typography.bodyLarge) },
            confirmButton = { TextButton(onClick = viewModel::dismissPopup) { Text("OUI CAPITAINE") } },
        )
    }

    when (val current = dialog) {
        is JournalDialog.Count -> CountDialog(
            category = current.category,
            initial = current.current,
            onConfirm = { viewModel.setCount(current.category, it); dialog = null },
            onDismiss = { dialog = null },
        )
        is JournalDialog.NewLine -> NewLineDialog(
            side = current.side,
            onConfirm = { viewModel.addCategory(it, current.side); dialog = null },
            onDismiss = { dialog = null },
        )
        is JournalDialog.Remove -> AlertDialog(
            onDismissRequest = { dialog = null },
            shape = RectangleShape,
            title = { Text("Jeter « ${current.category.label} » ?") },
            text = { Text("La ligne et tous ses compteurs passent par-dessus bord. Pour toujours.") },
            confirmButton = {
                TextButton(onClick = { viewModel.removeCategory(current.category); dialog = null }) { Text("PAR-DESSUS BORD") }
            },
            dismissButton = { TextButton(onClick = { dialog = null }) { Text("GARDER") } },
        )
        null -> Unit
    }

    val verdict = journal.verdict(day)
    val tipsy = verdict == Verdict.EPONGE || verdict == Verdict.PILIER_DE_TAVERNE

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "< MENU",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .align(Alignment.Start)
                    .clickable(onClick = onBack)
                    .padding(vertical = 4.dp),
            )
            Text("=== MON JOURNAL ===", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            DayNavigator(day = day, isToday = state.isToday, onTurn = viewModel::turnPage)

            PixelPirate(phase = Phase.IDLE, tipsy = tipsy, size = 150.dp)
            TypewriterText(
                text = "« ${state.quote} »",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(2.dp, MaterialTheme.colorScheme.onBackground), RectangleShape)
                    .clickable(onClick = viewModel::newQuote)
                    .padding(12.dp),
            )

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(Side.SPORT to "MUSCLES", Side.BOISSON to "BOUTEILLES").forEachIndexed { index, (side, title) ->
                    if (index == 1) Text("VS", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterVertically))
                    Tally(
                        title = title,
                        side = side,
                        journal = journal,
                        day = day,
                        color = if (side == Side.SPORT) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        onChange = viewModel::change,
                        onEditCount = { dialog = JournalDialog.Count(it, journal.count(day, it.id)) },
                        onNewLine = { dialog = JournalDialog.NewLine(side) },
                        onRemove = { dialog = JournalDialog.Remove(it) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Touche le nombre pour l'écrire au clavier.\nAppui long sur une ligne perso pour la jeter.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            History(journal = journal, selected = day, onOpen = viewModel::openDay)
        }
    }
}

@Composable
private fun DayNavigator(day: Long, isToday: Boolean, onTurn: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            "< veille",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.clickable { onTurn(-1) }.padding(8.dp),
        )
        Text(
            if (isToday) "aujourd'hui" else "${Logbook.weekday(day)} ${ShipClock.dayLabel(day)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Text(
            "lendemain >",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .alpha(if (isToday) 0.3f else 1f)
                .clickable(enabled = !isToday) { onTurn(1) }
                .padding(8.dp),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Tally(
    title: String,
    side: Side,
    journal: Journal,
    day: Long,
    color: Color,
    onChange: (Category, Int) -> Unit,
    onEditCount: (Category) -> Unit,
    onNewLine: () -> Unit,
    onRemove: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = color)
        Text(journal.total(day, side).toString().padStart(2, '0'), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = color)
        journal.categories(side).forEach { category ->
            val count = journal.count(day, category.id)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(BorderStroke(2.dp, color), RectangleShape)
                    .combinedClickable(
                        onClick = { onChange(category, 1) },
                        onLongClick = { if (category.custom) onRemove(category) },
                    )
                    .padding(vertical = 8.dp, horizontal = 4.dp),
            ) {
                Text(category.label, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                if (category.detail.isNotEmpty()) {
                    Text(category.detail, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "[-]",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .alpha(if (count > 0) 1f else 0.3f)
                            .clickable(enabled = count > 0) { onChange(category, -1) }
                            .padding(6.dp),
                    )
                    Text(
                        count.toString().padStart(2, '0'),
                        style = MaterialTheme.typography.titleMedium,
                        color = color,
                        modifier = Modifier.clickable { onEditCount(category) }.padding(6.dp),
                    )
                    Text(
                        "[+]",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.clickable { onChange(category, 1) }.padding(6.dp),
                    )
                }
            }
        }
        Text(
            "+ ligne perso",
            style = MaterialTheme.typography.bodySmall,
            color = color,
            modifier = Modifier.clickable(onClick = onNewLine).padding(8.dp),
        )
    }
}

/** Les pages précédentes : un résumé par jour, on touche pour relire (et corriger). */
@Composable
private fun History(journal: Journal, selected: Long, onOpen: (Long) -> Unit) {
    val days = journal.history().take(30)
    if (days.isEmpty()) return
    Text("-- Pages précédentes --", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
    days.forEach { day ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpen(day) }
                .padding(vertical = 6.dp),
        ) {
            Text(
                "${if (day == selected) ">" else " "} ${Logbook.weekday(day)} ${ShipClock.dayLabel(day)}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (day == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.weight(1f))
            Text(
                "muscles ${journal.total(day, Side.SPORT)} | verres ${journal.total(day, Side.BOISSON)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun CountDialog(category: Category, initial: Int, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(initial.toString()) }
    val value = text.toIntOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RectangleShape,
        title = { Text(category.label) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { input -> text = input.filter(Char::isDigit).take(2) },
                label = { Text("combien ? (0 à ${Categories.MAX_COUNT})") },
                singleLine = true,
                shape = RectangleShape,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(value ?: 0) }, enabled = value != null) { Text("NOTÉ !") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ANNULER") } },
    )
}

@Composable
private fun NewLineDialog(side: Side, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RectangleShape,
        title = { Text(if (side == Side.SPORT) "Nouveau sport" else "Nouvelle boisson") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(Categories.MAX_LABEL) },
                placeholder = { Text(if (side == Side.SPORT) "> course, natation..." else "> shot, soda...") },
                singleLine = true,
                shape = RectangleShape,
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) { Text("AJOUTER") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ANNULER") } },
    )
}
