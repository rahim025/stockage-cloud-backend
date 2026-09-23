const { Pool } = require('pg');
require('dotenv').config();

// Connexion à la base Postgres hébergée sur Supabase
// DATABASE_URL vient du dashboard Supabase : Project Settings > Database > Connection string
const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: { rejectUnauthorized: false }
});

// Crée les tables si elles n'existent pas encore (exécuté une fois au démarrage)
async function initDb() {
  await pool.query(`
    CREATE TABLE IF NOT EXISTS users (
      id TEXT PRIMARY KEY,
      email TEXT UNIQUE NOT NULL,
      password_hash TEXT NOT NULL,
      quota_octets BIGINT NOT NULL,
      espace_utilise BIGINT NOT NULL DEFAULT 0,
      created_at TIMESTAMPTZ NOT NULL DEFAULT now()
    )
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS files (
      id TEXT PRIMARY KEY,
      user_id TEXT NOT NULL REFERENCES users(id),
      nom_original TEXT NOT NULL,
      nom_stockage TEXT NOT NULL,
      categorie TEXT NOT NULL,
      taille_octets BIGINT NOT NULL,
      type_mime TEXT,
      created_at TIMESTAMPTZ NOT NULL DEFAULT now()
    )
  `);
}

module.exports = { pool, initDb };
