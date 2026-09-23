const express = require('express');
const multer = require('multer');
const { v4: uuidv4 } = require('uuid');
const { createClient } = require('@supabase/supabase-js');
const { pool } = require('../models/db');
const verifierToken = require('../middleware/auth');
const determinerCategorie = require('../utils/categorie');

const router = express.Router();

// Client Supabase (clé "service_role" — a le droit d'écrire dans le bucket,
// ne doit JAMAIS être exposée côté client/app mobile)
const supabase = createClient(process.env.SUPABASE_URL, process.env.SUPABASE_SERVICE_KEY);
const BUCKET = process.env.SUPABASE_BUCKET || 'fichiers';

// Taille max par fichier (par défaut 5 Go) — les erreurs multer sont interceptées
// par le middleware gestionnaireErreurs
const TAILLE_MAX_FICHIER = parseInt(process.env.TAILLE_MAX_FICHIER, 10) || 5 * 1024 * 1024 * 1024;

// Upload en mémoire (buffer) — plus d'écriture sur disque, le fichier part direct vers Supabase Storage
const upload = multer({ storage: multer.memoryStorage(), limits: { fileSize: TAILLE_MAX_FICHIER } });

// Upload d'un fichier — vérifie le quota avant d'accepter
router.post('/upload', verifierToken, upload.single('fichier'), async (req, res, next) => {
  try {
    if (!req.file) {
      return res.status(400).json({ erreur: 'Aucun fichier reçu' });
    }

    const { rows } = await pool.query('SELECT * FROM users WHERE id = $1', [req.userId]);
    const utilisateur = rows[0];
    const espaceActuel = Number(utilisateur.espace_utilise);
    const nouvelEspaceUtilise = espaceActuel + req.file.size;

    if (nouvelEspaceUtilise > Number(utilisateur.quota_octets)) {
      return res.status(413).json({ erreur: 'Quota de stockage dépassé' });
    }

    const categorie = determinerCategorie(req.file.mimetype, req.body.categorie);
    const id = uuidv4();
    const extension = req.file.originalname.includes('.')
      ? req.file.originalname.split('.').pop()
      : '';
    const nomStockage = extension ? `${uuidv4()}.${extension}` : uuidv4();
    // Chemin dans le bucket, préfixé par l'utilisateur pour isoler ses fichiers
    const cheminStockage = `${req.userId}/${nomStockage}`;

    const { error: erreurUpload } = await supabase.storage
      .from(BUCKET)
      .upload(cheminStockage, req.file.buffer, { contentType: req.file.mimetype });

    if (erreurUpload) {
      return res.status(500).json({ erreur: `Échec de l'upload vers le stockage : ${erreurUpload.message}` });
    }

    await pool.query(
      `INSERT INTO files (id, user_id, nom_original, nom_stockage, categorie, taille_octets, type_mime)
       VALUES ($1, $2, $3, $4, $5, $6, $7)`,
      [id, req.userId, req.file.originalname, cheminStockage, categorie, req.file.size, req.file.mimetype]
    );

    await pool.query('UPDATE users SET espace_utilise = $1 WHERE id = $2', [nouvelEspaceUtilise, req.userId]);

    res.status(201).json({
      message: 'Fichier envoyé avec succès',
      fichier: { id, nom: req.file.originalname, categorie, taille_octets: req.file.size }
    });
  } catch (err) {
    next(err);
  }
});

// Liste des fichiers de l'utilisateur, filtrable par catégorie
router.get('/', verifierToken, async (req, res, next) => {
  try {
    const { categorie } = req.query;

    const { rows: fichiers } = categorie
      ? await pool.query(
          'SELECT * FROM files WHERE user_id = $1 AND categorie = $2 ORDER BY created_at DESC',
          [req.userId, categorie]
        )
      : await pool.query('SELECT * FROM files WHERE user_id = $1 ORDER BY created_at DESC', [req.userId]);

    res.json({ fichiers });
  } catch (err) {
    next(err);
  }
});

// Téléchargement d'un fichier — génère une URL signée temporaire vers Supabase Storage
router.get('/:id/telecharger', verifierToken, async (req, res, next) => {
  try {
    const { rows } = await pool.query('SELECT * FROM files WHERE id = $1 AND user_id = $2', [req.params.id, req.userId]);
    const fichier = rows[0];

    if (!fichier) {
      return res.status(404).json({ erreur: 'Fichier introuvable' });
    }

    // URL valable 60 secondes, avec le nom d'origine pour le téléchargement
    const { data, error } = await supabase.storage
      .from(BUCKET)
      .createSignedUrl(fichier.nom_stockage, 60, { download: fichier.nom_original });

    if (error) {
      return res.status(500).json({ erreur: `Échec de la génération du lien : ${error.message}` });
    }

    res.json({ url_telechargement: data.signedUrl });
  } catch (err) {
    next(err);
  }
});

// Suppression d'un fichier
router.delete('/:id', verifierToken, async (req, res, next) => {
  try {
    const { rows } = await pool.query('SELECT * FROM files WHERE id = $1 AND user_id = $2', [req.params.id, req.userId]);
    const fichier = rows[0];

    if (!fichier) {
      return res.status(404).json({ erreur: 'Fichier introuvable' });
    }

    await supabase.storage.from(BUCKET).remove([fichier.nom_stockage]);

    await pool.query('DELETE FROM files WHERE id = $1', [req.params.id]);

    const { rows: userRows } = await pool.query('SELECT espace_utilise FROM users WHERE id = $1', [req.userId]);
    const nouvelEspace = Math.max(0, Number(userRows[0].espace_utilise) - Number(fichier.taille_octets));
    await pool.query('UPDATE users SET espace_utilise = $1 WHERE id = $2', [nouvelEspace, req.userId]);

    res.json({ message: 'Fichier supprimé avec succès' });
  } catch (err) {
    next(err);
  }
});

// Quota utilisé / restant
router.get('/quota', verifierToken, async (req, res, next) => {
  try {
    const { rows } = await pool.query('SELECT quota_octets, espace_utilise FROM users WHERE id = $1', [req.userId]);
    const utilisateur = rows[0];
    const quota = Number(utilisateur.quota_octets);
    const utilise = Number(utilisateur.espace_utilise);
    res.json({
      quota_octets: quota,
      espace_utilise: utilise,
      espace_restant: quota - utilise
    });
  } catch (err) {
    next(err);
  }
});

module.exports = router;
