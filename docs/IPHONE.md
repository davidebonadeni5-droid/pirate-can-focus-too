# 🍏 Lancer l'appli sur iPhone

Le code de l'appli est **le même** que sur Android (Kotlin Multiplatform + Compose Multiplatform).
Le dossier `iosApp/` contient seulement la petite coque Swift qui l'affiche.

## Ce qu'il faut (sur le Mac)

- **Xcode** (App Store) et un compte Apple, même **gratuit**.
- **Java 17+** : le plus simple est d'installer Android Studio (son Java est trouvé tout seul),
  sinon `brew install openjdk@17`.
- **XcodeGen** : `brew install xcodegen`.

## Première fois

```bash
git clone https://github.com/davidebonadeni5-droid/pirate-can-focus-too.git
cd pirate-can-focus-too/iosApp
xcodegen                     # génère PirateFocus.xcodeproj
open PirateFocus.xcodeproj
```

Dans Xcode :

1. Clique sur le projet **PirateFocus** > onglet **Signing & Capabilities** > **Team** : choisis ton compte Apple.
   Si Xcode râle sur l'identifiant, change le *Bundle Identifier* (par ex. `com.tonnom.piratefocus`).
2. Branche l'iPhone, choisis-le en haut, et clique sur ▶. La première compilation Kotlin prend quelques minutes.
3. Sur l'iPhone : *Réglages > Général > VPN et gestion de l'appareil* > fais confiance à ton compte développeur.
4. Active le *Mode développeur* si l'iPhone le demande (*Réglages > Confidentialité et sécurité*).

## Ce qui change par rapport à Android

| | Android | iPhone |
| --- | --- | --- |
| Minuteur, doublons, carnet, boutique, journal | ✅ | ✅ |
| Notification à la fin de la traversée | ✅ | ✅ |
| Le capitaine surgit sur Instagram (gardien) | ✅ | ❌ Apple l'interdit aux applis |

Pour le gardien sur iPhone, l'astuce est d'utiliser un **mode Concentration** d'iOS (voir l'écran Paramètres de l'appli).
Une vraie version avec l'API *Screen Time* d'Apple est possible plus tard, mais demande un compte développeur payant
et une autorisation spéciale d'Apple.

Pour les mises à jour sans réinstaller (et la limite des 7 jours) : voir [MISES-A-JOUR.md](MISES-A-JOUR.md).
