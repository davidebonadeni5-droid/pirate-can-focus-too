# 🔄 Mettre à jour l'appli sans la réinstaller

## Pourquoi il fallait réinstaller ?

Android n'accepte d'installer une nouvelle version **par-dessus** l'ancienne que si :

1. elle est **signée avec la même clé** que l'ancienne ;
2. son **numéro de version** (`versionCode`) est **plus grand**.

Chaque PC (et chaque machine de CI) a sa propre clé de *debug* : un APK construit ailleurs ne peut donc pas
remplacer celui qui est installé, et il faut désinstaller (en perdant ses doublons).

## La solution : une clé unique + des versions publiées automatiquement

À chaque push sur `main`, le workflow `.github/workflows/release.yml` :

- construit l'APK **signé avec ta clé à toi** (toujours la même) ;
- lui donne un numéro de version qui **augmente tout seul** ;
- le publie dans les **Releases** GitHub du dépôt.

Sur le téléphone, l'appli **[Obtainium](https://obtainium.imranr.dev/)** surveille ces Releases et propose
la mise à jour : un tap, et c'est installé par-dessus. Doublons, carnet de bord et boutique restent.

### 1. Créer la clé (une seule fois, à garder précieusement !)

Dans WSL :

```bash
keytool -genkeypair -v -keystore pirate.jks -alias pirate -keyalg RSA -keysize 2048 -validity 10000
base64 -w0 pirate.jks > pirate.jks.b64
```

⚠️ **Ne commite jamais `pirate.jks`.** Range-le dans un endroit sûr (gestionnaire de mots de passe, clé USB) :
si tu le perds, la prochaine version devra être réinstallée.

### 2. Donner la clé à GitHub

Sur GitHub : *Settings > Secrets and variables > Actions > New repository secret* :

| Secret | Valeur |
| --- | --- |
| `PIRATE_KEYSTORE_BASE64` | le contenu de `pirate.jks.b64` |
| `PIRATE_KEYSTORE_PASSWORD` | le mot de passe du keystore |
| `PIRATE_KEY_ALIAS` | `pirate` |
| `PIRATE_KEY_PASSWORD` | le mot de passe de la clé |

Sans ces secrets, le workflow ne publie rien (il laisse juste un avertissement).

### 3. Sur le téléphone

1. **Une dernière fois**, désinstalle la version installée depuis Android Studio (elle a une autre clé).
2. Installe [Obtainium](https://github.com/ImranR98/Obtainium/releases), ajoute l'URL du dépôt GitHub.
   Si le dépôt est privé, ajoute un *token* GitHub (lecture seule) dans les réglages d'Obtainium.
3. C'est tout : les prochaines versions s'installent par-dessus. ⛵

> 💡 Un `./gradlew installDebug` depuis Android Studio utilise encore la clé de debug : il entrera en conflit
> avec la version installée par Obtainium. Pour développer, utilise plutôt l'émulateur.

## Et sur iPhone ?

- **Depuis Xcode** (compte Apple gratuit) : relancer ▶ installe la nouvelle version **par-dessus**, les données
  restent. Mais Apple fait expirer l'appli au bout de **7 jours** : il faut la relancer depuis Xcode chaque semaine.
- **[SideStore](https://sidestore.io/) / AltStore** : renouvellent la signature tout seuls en Wi-Fi, toujours
  avec le compte gratuit.
- **Compte Apple Developer payant (99 $/an)** : **TestFlight**, avec mises à jour automatiques comme l'App Store,
  et plus d'expiration à 7 jours.
