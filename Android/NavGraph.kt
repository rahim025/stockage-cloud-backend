package com.stockagecloud.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stockagecloud.app.StockageCloudApp
import com.stockagecloud.app.ui.auth.AuthViewModel
import com.stockagecloud.app.ui.auth.AuthViewModelFactory
import com.stockagecloud.app.ui.auth.LoginScreen
import com.stockagecloud.app.ui.auth.RegisterScreen
import com.stockagecloud.app.ui.files.FilesScreen
import com.stockagecloud.app.ui.files.FilesViewModel
import com.stockagecloud.app.ui.files.FilesViewModelFactory

private object Ecrans {
    const val CONNEXION = "connexion"
    const val INSCRIPTION = "inscription"
    const val FICHIERS = "fichiers"
}

@Composable
fun NavGraph(app: StockageCloudApp, navController: NavHostController = rememberNavController()) {
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(app.authRepository))

    NavHost(navController = navController, startDestination = Ecrans.CONNEXION) {

        composable(Ecrans.CONNEXION) {
            LoginScreen(
                viewModel = authViewModel,
                onConnexionReussie = {
                    navController.navigate(Ecrans.FICHIERS) {
                        popUpTo(Ecrans.CONNEXION) { inclusive = true }
                    }
                },
                onAllerVersInscription = { navController.navigate(Ecrans.INSCRIPTION) }
            )
        }

        composable(Ecrans.INSCRIPTION) {
            RegisterScreen(
                viewModel = authViewModel,
                onInscriptionReussie = {
                    navController.navigate(Ecrans.FICHIERS) {
                        popUpTo(Ecrans.CONNEXION) { inclusive = true }
                    }
                },
                onAllerVersConnexion = { navController.popBackStack() }
            )
        }

        composable(Ecrans.FICHIERS) {
            val filesViewModel: FilesViewModel = viewModel(factory = FilesViewModelFactory(app.fichierRepository))
            FilesScreen(viewModel = filesViewModel)
        }
    }
}
