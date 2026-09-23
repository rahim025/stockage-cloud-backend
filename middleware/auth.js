const jwt = require('jsonwebtoken');

function verifierToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1]; // "Bearer <token>"

  if (!token) {
    return res.status(401).json({ erreur: 'Token manquant, connexion requise' });
  }

  jwt.verify(token, process.env.JWT_SECRET, (err, payload) => {
    if (err) {
      return res.status(403).json({ erreur: 'Token invalide ou expiré' });
    }
    req.userId = payload.userId;
    next();
  });
}

module.exports = verifierToken;
