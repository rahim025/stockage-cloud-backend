require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');

const { initDb } = require('./models/db');
const authRoutes = require('./routes/auth');
const filesRoutes = require('./routes/files');
const gestionnaireErreurs = require('./middleware/gestionnaireErreurs');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(helmet());
app.use(cors({ origin: process.env.ORIGINE_AUTORISEE || '*' }));
app.use(express.json());

// Limite les tentatives de connexion/inscription pour freiner le brute-force
const limiteurAuth = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 20,
  message: { erreur: 'Trop de tentatives, réessayez plus tard' }
});

app.use('/api/auth', limiteurAuth, authRoutes);
app.use('/api/fichiers', filesRoutes);

app.get('/api/sante', (req, res) => {
  res.json({ statut: 'ok' });
});

// Route inconnue
app.use((req, res) => {
  res.status(404).json({ erreur: 'Route introuvable' });
});

// Gestionnaire d'erreurs global — doit rester en dernier
app.use(gestionnaireErreurs);

initDb()
  .then(() => {
    app.listen(PORT, () => {
      console.log(`Serveur de stockage cloud démarré sur le port ${PORT}`);
    });
  })
  .catch((err) => {
    console.error('Échec de connexion à la base de données Supabase :', err);
    process.exit(1);
  });
