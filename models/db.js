const Database = require('better-sqlite3');
require('dotenv').config();

const db = new Database(process.env.DB_FILE || './data.db');

db.pragma('journal_mode = WAL');

// Table des utilisateurs
db.exec(`
  CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    quota_octets INTEGER NOT NULL,
    espace_utilise INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
  )
`);

// Table des fichiers
db.exec(`
  CREATE TABLE IF NOT EXISTS files (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    nom_original TEXT NOT NULL,
    nom_stockage TEXT NOT NULL,
    categorie TEXT NOT NULL,
    taille_octets INTEGER NOT NULL,
    type_mime TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id)
  )
`);

module.exports = db;
