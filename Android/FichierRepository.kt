package com.stockagecloud.app.data.repository

import com.stockagecloud.app.data.model.Fichier
import com.stockagecloud.app.data.model.QuotaResponse
import com.stockagecloud.app.data.remote.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class FichierRepository(private val apiService: ApiService) {

    suspend fun listerFichiers(categorie: String? = null): Result<List<Fichier>> {
        return try {
            val reponse = apiService.listerFichiers(categorie)
            if (reponse.isSuccessful && reponse.body() != null) {
                Result.success(reponse.body()!!.fichiers)
            } else {
                Result.failure(Exception("Erreur lors du chargement (code ${reponse.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploader(fichier: File): Result<Fichier> {
        return try {
            val corpsFichier = fichier.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val partie = MultipartBody.Part.createFormData("fichier", fichier.name, corpsFichier)
            val reponse = apiService.uploader(partie)
            if (reponse.isSuccessful && reponse.body() != null) {
                Result.success(reponse.body()!!.fichier)
            } else {
                Result.failure(Exception("Échec de l'envoi (code ${reponse.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenirLienTelechargement(id: String): Result<String> {
        return try {
            val reponse = apiService.obtenirLienTelechargement(id)
            if (reponse.isSuccessful && reponse.body() != null) {
                Result.success(reponse.body()!!.url_telechargement)
            } else {
                Result.failure(Exception("Impossible de générer le lien (code ${reponse.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun supprimer(id: String): Result<Unit> {
        return try {
            val reponse = apiService.supprimerFichier(id)
            if (reponse.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Échec de la suppression (code ${reponse.code()})"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenirQuota(): Result<QuotaResponse> {
        return try {
            val reponse = apiService.obtenirQuota()
            if (reponse.isSuccessful && reponse.body() != null) {
                Result.success(reponse.body()!!)
            } else {
                Result.failure(Exception("Impossible de charger le quota (code ${reponse.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
