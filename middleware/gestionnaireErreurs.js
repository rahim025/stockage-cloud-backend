const multer = require('multer');

// Middleware d'erreurs global — doit être déclaré en dernier dans server.js
function gestionnaireErreurs(err, req, res, next) {
  if (err instanceof multer.MulterError) {
    if (err.code === 'LIMIT_FILE_SIZE') {
      return res.status(413).json({ erreur: 'Fichier trop volumineux' });
    }
    return res.status(400).json({ erreur: `Erreur d'upload : ${err.message}` });
  }

  console.error(err);
  res.status(500).json({ erreur: 'Erreur interne du serveur' });
}

module.exports = gestionnaireErreurs;
