const test = require('node:test');
const assert = require('node:assert');
const determinerCategorie = require('../utils/categorie');

test('vidéo détectée via mimetype', () => {
  assert.strictEqual(determinerCategorie('video/mp4', null), 'videos');
});

test('document détecté via mimetype pdf', () => {
  assert.strictEqual(determinerCategorie('application/pdf', null), 'documents');
});

test('categorie envoyée explicitement est prioritaire', () => {
  assert.strictEqual(determinerCategorie('video/mp4', 'jeux'), 'jeux');
});

test('categorie invalide envoyée est ignorée', () => {
  assert.strictEqual(determinerCategorie('image/png', 'inexistante'), 'fichiers');
});

test('categorie par défaut est fichiers', () => {
  assert.strictEqual(determinerCategorie('image/png', null), 'fichiers');
});
