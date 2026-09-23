const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { v4: uuidv4 } = require('uuid');
const db = require('../models/db');

const router = express.Router();
const QUOTA_PAR_UTILISATEUR = parseInt(process.env.QUOTA_PAR_UTILISATEUR, 10);
const REGEX_EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// Inscription
router.post('/inscription', async (req, res) => {
  const { email, password } = req.body;

  if (!email || !password) {
    return res.status(400).json({ erreur: 'Email et mot de passe requis' });
  }

  if (!REGEX_EMAIL.test(email)) {
    return res.status(400).json({ erreur: 'Format email invalide' });
  }

  if (password.length < 8) {
    return res.status(400).json({ erreur: 'Le mot de passe doit contenir au moins 8 caractères' });
  }

  const utilisateurExistant = db.prepare('SELECT id FROM users WHERE email = ?').get(email);
  if (utilisateurExistant) {
    return res.status(409).json({ erreur: 'Un compte existe déjà avec cet email' });
  }

  const password_hash = await bcrypt.hash(password, 10);
  const id = uuidv4();

  db.prepare(
    'INSERT INTO users (id, email, password_hash, quota_octets, espace_utilise) VALUES (?, ?, ?, ?, 0)'
  ).run(id, email, password_hash, QUOTA_PAR_UTILISATEUR);

  const token = jwt.sign({ userId: id }, process.env.JWT_SECRET, { expiresIn: '30d' });

  res.status(201).json({
    message: 'Compte créé avec succès',
    token,
    utilisateur: { id, email, quota_octets: QUOTA_PAR_UTILISATEUR }
  });
});

// Connexion
router.post('/connexion', async (req, res) => {
  const { email, password } = req.body;

  const utilisateur = db.prepare('SELECT * FROM users WHERE email = ?').get(email);
  if (!utilisateur) {
    return res.status(401).json({ erreur: 'Email ou mot de passe incorrect' });
  }

  const motDePasseValide = await bcrypt.compare(password, utilisateur.password_hash);
  if (!motDePasseValide) {
    return res.status(401).json({ erreur: 'Email ou mot de passe incorrect' });
  }

  const token = jwt.sign({ userId: utilisateur.id }, process.env.JWT_SECRET, { expiresIn: '30d' });

  res.json({
    message: 'Connexion réussie',
    token,
    utilisateur: {
      id: utilisateur.id,
      email: utilisateur.email,
      quota_octets: utilisateur.quota_octets,
      espace_utilise: utilisateur.espace_utilise
    }
  });
});

module.exports = router;
