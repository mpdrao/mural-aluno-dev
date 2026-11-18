package br.edu.ifsp.ifsigard.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.ifsigard.data.entity.Usuario
import br.edu.ifsp.ifsigard.data.repository.MuralRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: MuralRepository) : ViewModel() {

    var email by mutableStateOf("")
        private set

    var senha by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    val currentUser = repository.currentUser

    fun onEmailChange(newEmail: String) {
        email = newEmail
        errorMessage = null
    }

    fun onSenhaChange(newSenha: String) {
        senha = newSenha
        errorMessage = null
    }

    fun preencherCredenciais(seedEmail: String, seedSenha: String = "123") {
        email = seedEmail
        senha = seedSenha
        errorMessage = null
    }

    fun login(onSuccess: (Usuario) -> Unit) {
        if (email.isBlank()) {
            errorMessage = "Informe o e-mail institucional."
            return
        }
        if (senha.isBlank()) {
            errorMessage = "Informe a senha."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = repository.login(email, senha)
            isLoading = false

            result.onSuccess { usuario ->
                onSuccess(usuario)
            }.onFailure { ex ->
                errorMessage = ex.message ?: "Erro ao autenticar."
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        repository.logout()
        email = ""
        senha = ""
        errorMessage = null
        onLoggedOut()
    }
}
