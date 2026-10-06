package dev.mathieuburnat.piratefocus.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** Petits messages façon toast, dessinés par l'appli elle-même pour marcher pareil sur Android et iPhone. */
class Toaster {
    var message by mutableStateOf<String?>(null)
        private set

    /** Change à chaque message, même identique, pour relancer le compte à rebours. */
    var id by mutableIntStateOf(0)
        private set

    /** Un seul toast à la fois : le nouveau remplace l'ancien. */
    fun show(text: String) {
        message = text
        id++
    }

    fun hide() {
        message = null
    }
}

val LocalToaster = staticCompositionLocalOf { Toaster() }

@Composable
fun ToastHost(toaster: Toaster, modifier: Modifier = Modifier) {
    val message = toaster.message ?: return
    LaunchedEffect(toaster.id) {
        delay(2_500)
        toaster.hide()
    }
    Text(
        text = message,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .padding(24.dp)
            .background(MaterialTheme.colorScheme.surface, RectangleShape)
            .border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), RectangleShape)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    )
}
