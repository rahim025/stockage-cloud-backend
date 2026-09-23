package com.stockagecloud.app.data.repository

import com.google.gson.Gson
import com.stockagecloud.app.data.local.TokenDataStore
import com.stockagecloud.app.data.model.AuthRequest
import com.stockagecloud.app.data.model.ErreurApi
import com.stockagecloud.app.data.remote.ApiService

sealed class ResultatAuth {
    data class Succes(val email: String) : ResultatAuth()
    data class Echec(val message: String) : ResultatAuth()
}

class AuthRepository(
    private val apiService: ApiService,
    private val tokenDataStore: TokenDataStore
) {

    suspend fun inscription(email: String, motDePasse: String): ResultatAuth {
        return try {
            val reponse = apiService.inscription(AuthRequest(email, motDePasse))
            traiterReponse(reponse)
        } catch (e: Exception) {
            ResultatAuth.Echec(e.message ?: "Erreur réseau")
        }
    }

    suspend fun connexion(email: String, motDePasse: String): ResultatAuth {
        return try {
            val reponse = apiService.connexion(AuthRequest(email, motDePasse))
            traiterReponse(reponse)
        } catch (e: Exception) {
            ResultatAuth.Echec(e.message ?: "Erreur réseau")
        }
    }

    suspend fun deconnexion() {
        tokenDataStore.effacerToken()
    }

    private suspend fun traiterReponse(reponse: retrofit2.Response<com.stockagecloud.app.data.model.AuthResponse>): ResultatAuth {
        if (reponse.isSuccessful && reponse.body() != null) {
            val corps = reponse.body()!!
            tokenDataStore.sauvegarderToken(corps.token)
            return ResultatAuth.Succes(corps.utilisateur.email)
        }

        val messageErreur = try {
            reponse.errorBody()?.string()?.let { Gson().fromJson(it, ErreurApi::class.java).erreur }
        } catch (e: Exception) {
            null
        } ?: "Une erreur est survenue (code ${reponse.code()})"

        return ResultatAuth.Echec(messageErreur)
    }
}
