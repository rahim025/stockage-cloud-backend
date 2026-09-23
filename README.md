# Stockage Cloud - Backend

Backend de stockage cloud (fichiers, vidéos, jeux, documents) avec authentification
et un quota de **2 To par utilisateur**.

## Stack
- Node.js / Express
- SQLite (via `better-sqlite3`) pour les métadonnées utilisateurs et fichiers
- Fichiers stockés directement sur le disque du serveur, dans `uploads/<user_id>/`
- Authentification par JWT

## Installation

```bash
npm install
cp .env.example .env
# éditer .env : mettre un vrai JWT_SECRET et ajuster STORAGE_DIR si besoin
npm start
```

Le serveur démarre sur `http://localhost:3000`.

## Endpoints principaux

| Méthode | Route                          | Description                          |
|---------|---------------------------------|---------------------------------------|
| POST    | /api/auth/inscription          | Créer un compte                       |
| POST    | /api/auth/connexion            | Se connecter, récupérer un token      |
| POST    | /api/fichiers/upload            | Envoyer un fichier (champ `fichier`)  |
| GET     | /api/fichiers?categorie=videos  | Lister les fichiers (filtre optionnel)|
| GET     | /api/fichiers/:id/telecharger   | Télécharger un fichier                |
| DELETE  | /api/fichiers/:id               | Supprimer un fichier                  |
| GET     | /api/fichiers/quota             | Voir l'espace utilisé / restant       |

Toutes les routes `/api/fichiers/*` nécessitent l'en-tête :
`Authorization: Bearer <token>`

## Catégories de fichiers
`fichiers`, `videos`, `jeux`, `documents` — déterminée automatiquement à partir
du type MIME, ou envoyée explicitement dans le champ `categorie` du formulaire.

## Important : dimensionnement du disque
Chaque utilisateur a un quota de 2 To. Prévoir l'espace disque du serveur en
conséquence (ex : 10 utilisateurs actifs = jusqu'à 20 To de disque nécessaire).

## Sécurité
- `helmet` pour les en-têtes HTTP de sécurité
- Limiteur de requêtes (`express-rate-limit`) sur `/api/auth/*` : 20 tentatives / 15 min
- Validation basique de l'email et longueur minimale du mot de passe (8 caractères) à l'inscription
- Taille max par fichier configurable via `TAILLE_MAX_FICHIER` (5 Go par défaut)
- Origine CORS configurable via `ORIGINE_AUTORISEE` (mets ton domaine en production, `*` en dev)
- Gestionnaire d'erreurs global (`middleware/gestionnaireErreurs.js`), route 404 catch-all

## Tests
```bash
npm test
```
Lance les tests unitaires (Node test runner intégré, aucune dépendance supplémentaire).
Un workflow GitHub Actions (`.github/workflows/ci.yml`) les exécute à chaque push/PR.

## Déploiement sur Render
Un fichier `render.yaml` est fourni. Points d'attention :
- Le stockage est **local au serveur** : sur le plan gratuit de Render le disque est éphémère
  (les fichiers uploadés disparaissent au redéploiement). Le `render.yaml` déclare un disque
  persistant, mais cette option nécessite un plan payant.
- Pense à définir `JWT_SECRET` manuellement dans le dashboard Render (il n'est pas commité).

## Publier sur GitHub

```bash
git init
git add .
git commit -m "Backend stockage cloud - quota 2 To par utilisateur"
git branch -M main
git remote add origin https://github.com/<ton-compte>/stockage-cloud-backend.git
git push -u origin main
```

## Prochaine étape
Construire l'app Android qui consomme cette API (connexion, upload, liste par
catégorie, téléchargement, jauge de quota).
