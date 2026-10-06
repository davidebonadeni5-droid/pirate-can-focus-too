<div align="center">

# 🏴‍☠️ Pirate Can Focus Too

**`~ un pirate aussi peut se concentrer ~`**

Une appli de concentration pour **Android, iPhone et Windows** où un capitaine pirate en pixel art
surveille ton pont, te raconte des bêtises et jette Instagram par-dessus bord.

<img src="docs/screenshots/menu.png" width="260" alt="Le menu du capitaine" />

</div>

---

## ⚓ C'est quoi, cette histoire ?

Tu connais la technique Pomodoro : on se concentre un moment, on fait une pause, on recommence.
Ici, c'est pareil, mais **en mer** :

| Dans la vraie vie | Dans Pirate Focus |
| --- | --- |
| Une session de concentration | Une **traversée** ⛵ |
| La pause | Une **escale** au port 🍹 |
| Les points gagnés | Des **doublons** 🪙 (1 par minute, bonus pour les longues traversées) |
| Abandonner | Le **navire coule** 🫧 (et le capitaine pleure) |
| Ouvrir TikTok en plein focus | **À L'ABORDAGE !!!** ☠️ |

Tout est en **pixel art** et en **police monospace**, comme un vieux terminal de bord.

## 🗺️ La visite guidée

<table>
  <tr>
    <td align="center"><img src="docs/screenshots/port.png" width="220" /><br/><b>Au port</b><br/>Choisis ta traversée : 5, 10, 30 min, ou <code>:</code> pour une durée sur mesure avec une roue qui défile.</td>
    <td align="center"><img src="docs/screenshots/en-mer.png" width="220" /><br/><b>En mer</b><br/>Le navire tangue, les vagues défilent, le capitaine exige le silence.</td>
    <td align="center"><img src="docs/screenshots/naufrage.png" width="220" /><br/><b>Naufrage</b><br/>Tu as abandonné. Le navire coule. Le perroquet est déçu.</td>
  </tr>
  <tr>
    <td align="center"><img src="docs/screenshots/abordage.png" width="220" /><br/><b>À l'abordage !</b><br/>Tu ouvres une appli interdite pendant une traversée ? Le capitaine surgit.</td>
    <td align="center"><img src="docs/screenshots/settings.png" width="220" /><br/><b>Paramètres</b><br/>Ta liste noire d'applis, avec recherche. Instagram, TikTok et Reddit y sont d'office.</td>
    <td align="center">🦜<br/><br/><b>Et aussi...</b><br/>Plus de 60 répliques de pirate tapées à la machine à écrire.<br/><i>« Un pirate qui procrastine, c'est juste un marin en pyjama. »</i></td>
  </tr>
</table>

## 🦜 Fonctionnalités

- ⏱️ **Minuteur façon Pomodoro** avec durées prédéfinies ou sur mesure (1 à 180 min)
- 🪙 **Doublons et traversées** comptés à chaque session terminée
- ☠️ **Gardien du navire** : bloque les applis de ta liste noire pendant le focus
- 🔍 **Recherche d'applis** dans la liste noire (sans se soucier des accents)
- ⌨️ **Répliques façon machine à écrire**, qui changent au hasard (tape sur la bulle pour en avoir une autre)
- 🌙 **Écran toujours allumé** pendant une traversée
- 💾 **Tout est sauvegardé** : doublons, traversée en cours (même si le téléphone ferme l'appli), carnet, boutique
- ⏰ **Minuteur fiable** : calé sur l'horloge, il ne dérive pas et sonne la fin de la traversée même appli fermée
- 📖 **Carnet de bord** : historique des traversées, série de jours d'affilée, la semaine en barres ASCII
- 🛒 **Boutique de Barbe-Grise** : un perroquet, un chapeau à plume, un galion à deux mâts
- 🍏 **iPhone aussi** : même code Kotlin grâce à Compose Multiplatform ([guide](docs/IPHONE.md))
- 🪟 **Windows aussi** : un installateur `.exe` dans les Releases, qui se met à jour tout seul à chaque lancement
- 🔄 **Mises à jour sans réinstaller** ([guide](docs/MISES-A-JOUR.md))
- 📴 **Hors ligne** : pas de compte, pas de pub, pas de serveur (seule la version Windows regarde GitHub pour ses mises à jour)

## 🧭 Prochaines escales

- [x] 📖 **Carnet de bord** : l'historique de tes traversées
- [x] 💾 Sauvegarder les doublons entre deux lancements
- [x] 🛒 Dépenser ses doublons (un perroquet, un chapeau, un plus gros navire)
- [x] 🍏 Une version iPhone
- [ ] ☠️ Un gardien sur iPhone (API Screen Time d'Apple)
- [ ] 🔕 Activer « Ne pas déranger » pendant le focus

---

## 🛠️ Get started (pour les moussaillons développeurs)

### Prérequis

- [Android Studio](https://developer.android.com/studio) (récent)
- Un **JDK 17 à 23** pour Gradle. Le JDK 21 embarqué par Android Studio fait très bien l'affaire.
  Si Android Studio propose « Use JVM 21 » à l'ouverture, accepte.
- Le SDK Android 35 (Android Studio l'installe tout seul au premier sync)

### Lancer l'app

```bash
git clone https://github.com/MathieuBurnat/pirate-can-focus-too.git
cd pirate-can-focus-too
```

1. Ouvre le dossier dans Android Studio (*File > Open*).
2. Attends la fin du sync Gradle.
3. Choisis un émulateur ou ton téléphone (débogage USB activé) et clique sur ▶.

En ligne de commande :

```bash
./gradlew assembleDebug        # construit l'APK
./gradlew testDebugUnitTest    # lance les tests unitaires
./gradlew installDebug         # installe sur l'appareil branché
./gradlew :app:run             # lance la version ordinateur (pratique pour tester vite)
./gradlew :app:packageExe      # construit l'installateur Windows (à lancer sous Windows)
```

Pour l'iPhone (il faut un Mac) : voir [docs/IPHONE.md](docs/IPHONE.md).

### Tester le gardien sur un émulateur

Le gardien a besoin de deux autorisations spéciales. Dans l'app, va dans **Paramètres** et touche les lignes `[!!]`,
ou accorde-les d'un coup avec adb :

```bash
adb shell appops set dev.mathieuburnat.piratefocus GET_USAGE_STATS allow
adb shell appops set dev.mathieuburnat.piratefocus SYSTEM_ALERT_WINDOW allow
```

Ajoute Chrome à la liste noire, lance une traversée, ouvre Chrome : à l'abordage !

### La cale (organisation du code)

```
app/src/
├── commonMain/   # partagé Android + iPhone : minuteur, carnet, boutique, sauvegarde, tous les écrans
├── androidMain/  # MainActivity, le gardien, notifications Android
├── iosMain/      # point d'entrée iPhone, notifications iOS
├── desktopMain/  # version ordinateur (Windows .exe) et ses mises à jour automatiques
└── commonTest/   # tests de la logique
iosApp/           # coque Swift de l'app iPhone (projet Xcode généré par XcodeGen)
```

- **Stack** : Kotlin Multiplatform, Compose Multiplatform (Material 3), aucun backend.
- **Sprites** : définis comme des grilles de caractères dans `PixelPirate.kt` et `PixelShip.kt`
  (un caractère = une couleur de la palette). Dessine ton propre perroquet !
- **Répliques** : toutes dans `focus/PirateQuotes.kt`. Courtes, drôles, en français pirate.
- Plus de détails dans [`CLAUDE.md`](CLAUDE.md).

### Contribuer

Les contributions sont les bienvenues, surtout :

- 🗣️ de **nouvelles répliques** de capitaine (le plus facile pour commencer !)
- 🎨 de **nouveaux sprites** pixel art
- 🐛 des correctifs et des idées d'escales

Ouvre une issue ou une pull request, et que le vent te soit favorable. ⛵

<div align="center">

---

*Fait avec du rhum (sans alcool) et beaucoup de `~^~-~^~-~^~`*

</div>
