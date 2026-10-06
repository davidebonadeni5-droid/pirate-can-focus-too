package dev.mathieuburnat.piratefocus

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.mathieuburnat.piratefocus.data.AndroidAlarms
import dev.mathieuburnat.piratefocus.data.PrefsStore
import dev.mathieuburnat.piratefocus.data.SaveGame
import dev.mathieuburnat.piratefocus.focus.FocusViewModel
import dev.mathieuburnat.piratefocus.focus.Phase
import dev.mathieuburnat.piratefocus.guard.FocusGuardService
import dev.mathieuburnat.piratefocus.guard.GuardPermissions
import dev.mathieuburnat.piratefocus.ui.PirateApp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val saveGame by lazy { SaveGame(PrefsStore(this)) }
    private val alarms by lazy { AndroidAlarms(this) }

    private val focusViewModel: FocusViewModel by viewModels {
        viewModelFactory { initializer { FocusViewModel(saveGame, alarms) } }
    }

    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            focusViewModel.uiState.map { it.timer.phase }.distinctUntilChanged().collect { phase ->
                // Le gardien ne monte la garde que pendant une traversée, même si l'app est en arrière-plan.
                if (phase == Phase.FOCUS && GuardPermissions.isReady(this@MainActivity)) {
                    FocusGuardService.start(this@MainActivity)
                } else {
                    FocusGuardService.stop(this@MainActivity)
                }
                // Première traversée : on demande le droit de sonner la cloche à l'arrivée.
                if (phase == Phase.FOCUS) askForNotifications()
            }
        }

        setContent {
            PirateApp(saveGame, alarms, viewModel = focusViewModel)
        }
    }

    private fun askForNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
