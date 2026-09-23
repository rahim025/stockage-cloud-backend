package com.stockagecloud.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockagecloud.app.data.repository.AuthRepository
import com.stockagecloud.app.data.repository.ResultatAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class EtatAuth {
    object Inactif : EtatAuth()
    object Chargement : EtatAuth()
    object Succes : EtatAuth()
    data class Erreur(val message: String) : EtatAuth()
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _etat = MutableStateFlow<EtatAuth>(EtatAuth.Inactif)
    val etat: StateFlow<EtatAuth> = _etat

    fun connexion(email: String, motDePasse: String) {
        if (!champsValides(email, motDePasse)) return

        _etat.value = EtatAuth.Chargement
        viewModelScope.launch {
            when (val resultat = authRepository.connexion(email, motDePasse)) {
                is ResultatAuth.Succes -> _etat.value = EtatAuth.Succes
                is ResultatAuth.Echec -> _etat.value = EtatAuth.Erreur(resultat.message)
            }
        }
    }

    fun inscription(email: String, motDePasse: String) {
        if (!champsValides(email, motDePasse)) return

        _etat.value = EtatAuth.Chargement
        viewModelScope.launch {
            when (val resultat = authRepository.inscription(email, motDePasse)) {
                is ResultatAuth.Succes -> _etat.value = EtatAuth.Succes
                is ResultatAuth.Echec -> _etat.value = EtatAuth.Erreur(resultat.message)
            }
        }
    }

    private fun champsValides(email: String, motDePasse: String): Boolean {
        if (email.isBlank() || motDePasse.isBlank()) {
            _etat.value = EtatAuth.Erreur("Email et mot de passe requis")
            return false
        }
        if (motDePasse.length < 8) {
            _etat.value = EtatAuth.Erreur("Le mot de passe doit contenir au moins 8 caractères")
            return false
        }
        return true
    }

    fun reinitialiserEtat() {
        _etat.value = EtatAuth.Inactif
    }
}
