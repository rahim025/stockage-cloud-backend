# Stockage Cloud - Android

App Android native (Kotlin + Jetpack Compose) consommant l'API `stockage-cloud-backend`.

## Ouvrir le projet
1. Ouvre Android Studio (Iguana ou plus récent).
2. `File > Open` puis sélectionne le dossier `stockage-cloud-android`.
3. Laisse Gradle synchroniser (première fois : télécharge les dépendances).

## Configurer l'URL du backend
Dans `app/build.gradle.kts` :
- `debug` pointe par défaut vers `http://10.0.2.2:3000/` (localhost de ta machine, vu depuis l'émulateur Android).
- `release` (`defaultConfig.buildConfigField BASE_URL`) : remplace par l'URL de ton déploiement Render avant de builder un APK release.

Si tu testes sur un vrai téléphone (pas l'émulateur), remplace `10.0.2.2` par l'IP locale de ton PC (ou directement l'URL Render).

## Ce qui est implémenté
- Connexion / Inscription (JWT stocké via DataStore)
- Liste des fichiers avec jauge de quota
- Suppression d'un fichier
- Couche réseau Retrofit complète (upload, téléchargement, quota) — prête à être branchée sur d'autres écrans

## Prochaines étapes possibles
- Écran d'upload (sélection de fichier via `ActivityResultContracts.OpenDocument`)
- Téléchargement effectif du fichier depuis l'URL signée renvoyée par l'API
- Filtrage de la liste par catégorie (`videos`, `documents`, `jeux`, `fichiers`)
- Génération d'un APK signé + publication en GitHub Release

## Build en ligne de commande
```bash
./gradlew assembleDebug
# APK généré dans app/build/outputs/apk/debug/
```
