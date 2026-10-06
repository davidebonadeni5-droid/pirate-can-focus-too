package dev.mathieuburnat.piratefocus.ui

import androidx.compose.runtime.Composable

/** Garde l'écran allumé pendant une traversée. */
@Composable
expect fun KeepScreenOn(enabled: Boolean)

/** Le bouton retour du téléphone (Android seulement ; l'iPhone n'en a pas). */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)

/** Le gardien a-t-il ce qu'il faut pour surveiller le pont ? */
@Composable
expect fun rememberGuardReady(): Boolean

/** La section « gardien du navire » des paramètres : très différente selon le téléphone. */
@Composable
expect fun GuardSettings()
