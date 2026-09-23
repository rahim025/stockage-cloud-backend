package com.stockagecloud.app.data.model

// --- Authentification ---

data class AuthRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val message: String,
    val token: String,
    val utilisateur: Utilisateur
)

data class Utilisateur(
    val id: String,
    val email: String,
    val quota_octets: Long,
    val espace_utilise: Long? = null
)

// --- Fichiers ---

data class Fichier(
    val id: String,
    val nom_original: String,
    val categorie: String,
    val taille_octets: Long,
    val type_mime: String? = null,
    val created_at: String? = null
)

data class ListeFichiersResponse(
    val fichiers: List<Fichier>
)

data class UploadResponse(
    val message: String,
    val fichier: Fichier
)

data class TelechargementResponse(
    val url_telechargement: String
)

data class QuotaResponse(
    val quota_octets: Long,
    val espace_utilise: Long,
    val espace_restant: Long
)

// --- Erreurs ---

data class ErreurApi(
    val erreur: String? = null
)
