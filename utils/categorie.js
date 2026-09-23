// Détermine la catégorie (fichiers / videos / jeux / documents) à partir du type mime
// ou d'une catégorie envoyée explicitement par l'app cliente (ex: app Android)
function determinerCategorie(mimetype, categorieEnvoyee) {
  const categoriesValides = ['fichiers', 'videos', 'jeux', 'documents'];
  if (categorieEnvoyee && categoriesValides.includes(categorieEnvoyee)) {
    return categorieEnvoyee;
  }
  if (mimetype.startsWith('video/')) return 'videos';
  if (
    mimetype === 'application/pdf' ||
    mimetype.includes('word') ||
    mimetype.includes('document') ||
    mimetype === 'text/plain'
  ) {
    return 'documents';
  }
  if (mimetype === 'application/vnd.android.package-archive' || mimetype.includes('octet-stream')) {
    return 'jeux';
  }
  return 'fichiers';
}

module.exports = determinerCategorie;
