import SwiftUI
import UIKit
import PirateShared

@main
struct PirateFocusApp: App {
    var body: some Scene {
        WindowGroup {
            // L'app Kotlin gère elle-même les encoches et la barre d'état.
            ComposeView().ignoresSafeArea(.all)
        }
    }
}

/** Affiche l'écran Compose écrit en Kotlin (app/src/iosMain/.../MainViewController.kt). */
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
