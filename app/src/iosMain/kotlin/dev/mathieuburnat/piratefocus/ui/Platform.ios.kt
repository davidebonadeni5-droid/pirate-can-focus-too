package dev.mathieuburnat.piratefocus.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import platform.UIKit.UIApplication

@Composable
actual fun KeepScreenOn(enabled: Boolean) {
    DisposableEffect(enabled) {
        UIApplication.sharedApplication.idleTimerDisabled = enabled
        onDispose { UIApplication.sharedApplication.idleTimerDisabled = false }
    }
}

/** Pas de bouton retour sur iPhone : on revient au menu avec « < MENU ». */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit

/** Pas de gardien sur iPhone pour l'instant : rien à réveiller. */
@Composable
actual fun rememberGuardReady(): Boolean = true

@Composable
actual fun GuardSettings() {
    Column {
        Text("-- Le gardien du navire --", style = MaterialTheme.typography.titleSmall)
        Text(
            "Sur iPhone, Apple garde les clés de la cale : une appli ne peut pas voir quelle autre appli est ouverte. " +
                "Le capitaine ne peut donc pas surgir sur Instagram... pour l'instant.",
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(12.dp))
        Text("-- L'astuce du capitaine --", style = MaterialTheme.typography.titleSmall)
        Text(
            "Réglages > Concentration > « + » : crée un mode « Pirate Focus » qui coupe les notifications " +
                "des sirènes (Instagram, TikTok...). Lance-le avant de lever l'ancre !",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
