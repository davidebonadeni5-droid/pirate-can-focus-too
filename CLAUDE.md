# CLAUDE.md

## Le projet

**Pirate Can Focus Too** est une application Android **et iPhone** de concentration (minuteur façon Pomodoro)
déguisée en aventure de pirate. Le ton est **fun et drôle** : un capitaine pirate en **pixel art**
accompagne l'utilisateur, avec une interface entièrement en **police monospace** (esprit terminal / rétro).

- Une session de focus = une traversée en mer. La finir rapporte des doublons (pièces d'or).
- Une pause = une escale au port.
- Abandonner une session = le navire coule (gentiment, avec une réplique moqueuse).
- Les textes de l'app sont en **français**, avec un vocabulaire de pirate exagéré.

## Stack

- Kotlin Multiplatform + Compose Multiplatform (Material 3) : un seul module `app` pour Android, iOS et ordinateur.
- iOS : `iosApp/` est une coque SwiftUI ; le projet Xcode est généré par XcodeGen (`iosApp/project.yml`).
- Sauvegarde locale (`data/SaveGame.kt`) : SharedPreferences sur Android, NSUserDefaults sur iPhone.
- Gradle Kotlin DSL + catalogue de versions (`gradle/libs.versions.toml`).
- `minSdk 26`, `targetSdk`/`compileSdk 35`, JVM 17.
- Pas de dépendance réseau ni de backend : tout est local. Seule exception : la version Windows
  (`desktopMain/DesktopUpdater.kt`) lit la dernière Release GitHub au lancement pour se mettre à jour.
- CI : `.github/workflows/build.yml` (APK + tests + app iPhone simulateur + .exe Windows),
  `release.yml` (APK signé, .exe, .msi et .zip Windows publiés en Release, pour des mises à jour
  sans réinstaller : `docs/MISES-A-JOUR.md`).

## Environnement du développeur

- Le projet est ouvert dans **Android Studio sous Windows** (`C:\0_projects\pirate-can-focus-too`).
- `git` et `gh` sont installés dans **WSL (Ubuntu)**, pas dans PowerShell :
  lancer les commandes git via `wsl -e bash -lc "cd /mnt/c/0_projects/pirate-can-focus-too && git ..."`.
- Le build se fait depuis Android Studio, ou avec `gradlew.bat` en utilisant le JDK 21 d'Android Studio
  (`$env:JAVA_HOME="$env:USERPROFILE\.jdks\jbr-21.0.11"`). Gradle 8.11.1 refuse le JDK 25.
- Le téléphone de test (Galaxy A55) et un émulateur sont accessibles via `adb`.
- Les captures du README sont dans `docs/screenshots/` (prises sur l'émulateur).
- Fins de ligne : `.gitattributes` force LF dans le dépôt.

## Organisation du code

```
app/src/
├── commonMain/kotlin/dev/mathieuburnat/piratefocus/   # partagé Android + iPhone
│   ├── data/
│   │   ├── Platform.kt          # KeyValueStore, Alarms (notifications), ShipClock (heure locale)
│   │   └── SaveGame.kt          # sauvegarde : minuteur, carnet, boutique, journal du jour
│   ├── focus/
│   │   ├── FocusTimer.kt        # logique pure du minuteur, calée sur l'heure de fin (testée)
│   │   ├── FocusViewModel.kt    # état du navire : minuteur, doublons, carnet, boutique
│   │   └── PirateQuotes.kt      # répliques du capitaine (phases, notifications, boutique, carnet)
│   ├── logbook/Logbook.kt       # carnet de bord : stats, séries de jours (logique pure, testée)
│   ├── shop/Shop.kt             # boutique : perroquet, chapeau, galion (logique pure, testée)
│   ├── journal/                 # journal secret (7 taps sur le menu) : sport contre boissons, une page par jour
│   └── ui/
│       ├── Platform.kt          # expect : écran allumé, bouton retour, section gardien des paramètres
│       ├── PirateApp.kt         # navigation : menu, focus, carnet, boutique, journal, paramètres
│       ├── Toaster.kt           # toasts dessinés par l'app (pareil sur Android et iPhone)
│       ├── MenuScreen.kt, FocusScreen.kt, LogbookScreen.kt, ShopScreen.kt, JournalScreen.kt, SettingsScreen.kt
│       ├── MinutesWheel.kt      # roue de défilement pour choisir les minutes
│       ├── PixelPirate.kt       # le capitaine (+ perroquet, chapeau) + palette et drawSprite() partagés
│       ├── PixelShip.kt         # le navire (ou le galion), centré, qui tangue
│       └── theme/Theme.kt       # couleurs "mer de nuit" + typo monospace partout
├── androidMain/                 # MainActivity, guard/ (le gardien), notifications AlarmManager, actual
├── iosMain/                     # MainViewController (entrée iPhone), NSUserDefaults, notifications iOS, actual
├── desktopMain/                 # version ordinateur / Windows .exe (./gradlew :app:run), DesktopUpdater
└── commonTest/                  # tests kotlin.test de toute la logique pure
```

## Conventions

- Tout le texte affiché utilise `FontFamily.Monospace` (via le thème, ne pas le contourner).
- Le pixel art est défini sous forme de grilles de caractères dans `PixelPirate.kt`
  (un caractère = une couleur de la palette). Ajouter de nouveaux sprites de la même façon.
- La logique du minuteur reste dans `FocusTimer.kt`, sans dépendance Android, pour rester testable.
  L'heure est toujours passée en paramètre (`now`) : jamais d'horloge cachée dans la logique.
- Le code commun ne doit rien importer d'Android (`android.*`, `java.*`, `String.format`...).
  Ce qui dépend de la plateforme passe par une interface (`data/Platform.kt`) ou un `expect` (`ui/Platform.kt`).
- Les nouvelles répliques vont dans `PirateQuotes.kt` : courtes, drôles, en français pirate.
