package com.stockagecloud.app.ui.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stockagecloud.app.data.model.Fichier
import com.stockagecloud.app.data.model.QuotaResponse
import com.stockagecloud.app.data.repository.FichierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EtatFichiers(
    val chargement: Boolean = true,
    val fichiers: List<Fichier> = emptyList(),
    val quota: QuotaResponse? = null,
    val erreur: String? = null
)

class FilesViewModel(private val fichierRepository: FichierRepository) : ViewModel() {

    private val _etat = MutableStateFlow(EtatFichiers())
    val etat: StateFlow<EtatFichiers> = _etat

    init {
        charger()
    }

    fun charger(categorie: String? = null) {
        _etat.value = _etat.value.copy(chargement = true, erreur = null)
        viewModelScope.launch {
            val resultatFichiers = fichierRepository.listerFichiers(categorie)
            val resultatQuota = fichierRepository.obtenirQuota()

            _etat.value = _etat.value.copy(
                chargement = false,
                fichiers = resultatFichiers.getOrDefault(emptyList()),
                quota = resultatQuota.getOrNull(),
                erreur = resultatFichiers.exceptionOrNull()?.message
                    ?: resultatQuota.exceptionOrNull()?.message
            )
        }
    }

    fun supprimer(id: String) {
        viewModelScope.launch {
            fichierRepository.supprimer(id)
            charger()
        }
    }
}

class FilesViewModelFactory(private val fichierRepository: FichierRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return FilesViewModel(fichierRepository) as T
    }
}
