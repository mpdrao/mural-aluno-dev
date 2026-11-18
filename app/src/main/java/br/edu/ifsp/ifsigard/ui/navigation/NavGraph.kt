package br.edu.ifsp.ifsigard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.edu.ifsp.ifsigard.data.repository.MuralRepository
import br.edu.ifsp.ifsigard.ui.screens.AvisoFormScreen
import br.edu.ifsp.ifsigard.ui.screens.LoginScreen
import br.edu.ifsp.ifsigard.ui.screens.MuralScreen
import br.edu.ifsp.ifsigard.ui.viewmodel.AuthViewModel
import br.edu.ifsp.ifsigard.ui.viewmodel.AvisoFormViewModel
import br.edu.ifsp.ifsigard.ui.viewmodel.MuralViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Mural : Screen("mural")
    object AvisoForm : Screen("aviso_form?avisoId={avisoId}") {
        fun createRoute(avisoId: Long = 0L) = "aviso_form?avisoId=$avisoId"
    }
}

@Composable
fun AppNavGraph(
    repository: MuralRepository,
    navController: NavHostController = rememberNavController()
) {
    val authViewModel = rememberViewModel { AuthViewModel(repository) }
    val muralViewModel = rememberViewModel { MuralViewModel(repository) }
    val currentUser by repository.currentUser.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (currentUser != null) Screen.Mural.route else Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Mural.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Mural.route) {
            val user = currentUser
            if (user == null) {
                // Sessao expirada ou logout
                navController.navigate(Screen.Login.route) {
                    popUpTo(0)
                }
            } else {
                MuralScreen(
                    viewModel = muralViewModel,
                    usuarioLogado = user,
                    onNovoAviso = {
                        navController.navigate(Screen.AvisoForm.createRoute(0L))
                    },
                    onEditarAviso = { id ->
                        navController.navigate(Screen.AvisoForm.createRoute(id))
                    },
                    onLogout = {
                        authViewModel.logout {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0)
                            }
                        }
                    }
                )
            }
        }

        composable(
            route = Screen.AvisoForm.route,
            arguments = listOf(
                navArgument("avisoId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val avisoId = backStackEntry.arguments?.getLong("avisoId") ?: 0L
            val user = currentUser
            if (user == null) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0)
                }
            } else {
                val formViewModel = rememberViewModel(key = "aviso_form_$avisoId") {
                    AvisoFormViewModel(repository)
                }

                AvisoFormScreen(
                    avisoId = avisoId,
                    usuarioLogado = user,
                    viewModel = formViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
@Composable
inline fun <reified VM : androidx.lifecycle.ViewModel> rememberViewModel(
    key: String? = null,
    crossinline creator: () -> VM
): VM {
    val factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return creator() as T
        }
    }
    return viewModel(key = key, factory = factory)
}
