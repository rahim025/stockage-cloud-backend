const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const { v4: uuidv4 } = require('uuid');
const db = require('../models/db');
const verifierToken = require('../middleware/auth');
const determinerCategorie = require('../utils/categorie');

const router = express.Router();
const STORAGE_DIR = process.env.STORAGE_DIR || './uploads';
// Taille max par fichier (par défaut 5 Go) — les erreurs multer sont interceptées
// par le middleware gestionnaireErreurs
const TAILLE_MAX_FICHIER = parseInt(process.env.TAILLE_MAX_FICHIER, 10) || 5 * 1024 * 1024 * 1024;

// Stockage temporaire sur disque, dossier par utilisateur, nom aléatoire pour éviter les collisions
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const dossierUser = path.join(STORAGE_DIR, req.userId);
    fs.mkdirSync(dossierUser, { recursive: true });
    cb(null, dossierUser);
  },
  filename: (req, file, cb) => {
    const nomStockage = `${uuidv4()}${path.extname(file.originalname)}`;
    cb(null, nomStockage);
  }
});

const upload = multer({ storage, limits: { fileSize: TAILLE_MAX_FICHIER } });

// Upload d'un fichier — vérifie le quota avant d'accepter
router.post('/upload', verifierToken, upload.single('fichier'), (req, res) => {
  if (!req.file) {
    return res.status(400).json({ erreur: 'Aucun fichier reçu' });
  }

  const utilisateur = db.prepare('SELECT * FROM users WHERE id = ?').get(req.userId);
  const nouvelEspaceUtilise = utilisateur.espace_utilise + req.file.size;

  if (nouvelEspaceUtilise > utilisateur.quota_octets) {
    fs.unlinkSync(req.file.path); // on retire le fichier, quota dépassé
    return res.status(413).json({ erreur: 'Quota de stockage dépassé' });
  }

  const categorie = determinerCategorie(req.file.mimetype, req.body.categorie);
  const id = uuidv4();

  db.prepare(`
    INSERT INTO files (id, user_id, nom_original, nom_stockage, categorie, taille_octets, type_mime)
    VALUES (?, ?, ?, ?, ?, ?, ?)
  `).run(id, req.userId, req.file.originalname, req.file.filename, categorie, req.file.size, req.file.mimetype);

  db.prepare('UPDATE users SET espace_utilise = ? WHERE id = ?').run(nouvelEspaceUtilise, req.userId);

  res.status(201).json({
    message: 'Fichier envoyé avec succès',
    fichier: { id, nom: req.file.originalname, categorie, taille_octets: req.file.size }
  });
});

// Liste des fichiers de l'utilisateur, filtrable par catégorie
router.get('/', verifierToken, (req, res) => {
  const { categorie } = req.query;

  let fichiers;
  if (categorie) {
    fichiers = db.prepare('SELECT * FROM files WHERE user_id = ? AND categorie = ? ORDER BY created_at DESC')
      .all(req.userId, categorie);
  } else {
    fichiers = db.prepare('SELECT * FROM files WHERE user_id = ? ORDER BY created_at DESC').all(req.userId);
  }

  res.json({ fichiers });
});

// Téléchargement d'un fichier
router.get('/:id/telecharger', verifierToken, (req, res) => {
  const fichier = db.prepare('SELECT * FROM files WHERE id = ? AND user_id = ?').get(req.params.id, req.userId);

  if (!fichier) {
    return res.status(404).json({ erreur: 'Fichier introuvable' });
  }

  const cheminFichier = path.join(STORAGE_DIR, req.userId, fichier.nom_stockage);
  res.download(cheminFichier, fichier.nom_original);
});

// Suppression d'un fichier
router.delete('/:id', verifierToken, (req, res) => {
  const fichier = db.prepare('SELECT * FROM files WHERE id = ? AND user_id = ?').get(req.params.id, req.userId);

  if (!fichier) {
    return res.status(404).json({ erreur: 'Fichier introuvable' });
  }

  const cheminFichier = path.join(STORAGE_DIR, req.userId, fichier.nom_stockage);
  if (fs.existsSync(cheminFichier)) {
    fs.unlinkSync(cheminFichier);
  }

  db.prepare('DELETE FROM files WHERE id = ?').run(req.params.id);

  const utilisateur = db.prepare('SELECT espace_utilise FROM users WHERE id = ?').get(req.userId);
  const nouvelEspace = Math.max(0, utilisateur.espace_utilise - fichier.taille_octets);
  db.prepare('UPDATE users SET espace_utilise = ? WHERE id = ?').run(nouvelEspace, req.userId);

  res.json({ message: 'Fichier supprimé avec succès' });
});

// Quota utilisé / restant
router.get('/quota', verifierToken, (req, res) => {
  const utilisateur = db.prepare('SELECT quota_octets, espace_utilise FROM users WHERE id = ?').get(req.userId);
  res.json({
    quota_octets: utilisateur.quota_octets,
    espace_utilise: utilisateur.espace_utilise,
    espace_restant: utilisateur.quota_octets - utilisateur.espace_utilise
  });
});

module.exports = router;
