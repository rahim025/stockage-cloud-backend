package com.stockagecloud.app.ui.files

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stockagecloud.app.data.model.Fichier

@Composable
fun FilesScreen(viewModel: FilesViewModel) {
    val etat by viewModel.etat.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Mes fichiers", style = MaterialTheme.typography.headlineSmall)

        etat.quota?.let { quota ->
            val fraction = if (quota.quota_octets > 0) {
                (quota.espace_utilise.toFloat() / quota.quota_octets.toFloat()).coerceIn(0f, 1f)
            } else 0f

            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                LinearProgressIndicator(progress = fraction, modifier = Modifier.fillMaxWidth())
                Text(
                    text = "${formaterTaille(quota.espace_utilise)} / ${formaterTaille(quota.quota_octets)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (etat.erreur != null) {
            Text(text = etat.erreur!!, color = MaterialTheme.colorScheme.error)
        }

        if (etat.chargement) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (etat.fichiers.isEmpty()) {
            Text("Aucun fichier pour l'instant.")
        } else {
            LazyColumn {
                items(etat.fichiers) { fichier ->
                    LigneFichier(fichier = fichier, onSupprimer = { viewModel.supprimer(fichier.id) })
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun LigneFichier(fichier: Fichier, onSupprimer: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = fichier.nom_original, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "${fichier.categorie} · ${formaterTaille(fichier.taille_octets)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        IconButton(onClick = onSupprimer) {
            Icon(imageVector = Icons.Filled.Delete, contentDescription = "Supprimer")
        }
    }
}

private fun formaterTaille(octets: Long): String {
    val unites = listOf("o", "Ko", "Mo", "Go", "To")
    var valeur = octets.toDouble()
    var index = 0
    while (valeur >= 1024 && index < unites.size - 1) {
        valeur /= 1024
        index++
    }
    return "%.1f %s".format(valeur, unites[index])
}
