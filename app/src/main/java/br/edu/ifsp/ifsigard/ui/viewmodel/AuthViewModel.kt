package br.edu.ifsp.ifsigard.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.ifsigard.data.entity.Usuario
import br.edu.ifsp.ifsigard.data.repository.MuralRepository
import kotlinx.coroutines.launch
import java.util.Locale

class AuthViewModel(private val repository: MuralRepository) : ViewModel() {

    var email by mutableStateOf("")
        private set

    var senha by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var erroEmail by mutableStateOf<String?>(null)
        private set

    var erroSenha by mutableStateOf<String?>(null)
        private set

    var erroGeral by mutableStateOf<String?>(null)
        private set

    val currentUser = repository.currentUser

    fun onEmailChange(newEmail: String) {
        email = newEmail
        erroEmail = null
        erroGeral = null
    }

    fun onSenhaChange(newSenha: String) {
        senha = newSenha
        erroSenha = null
        erroGeral = null
    }

    fun preencherCredenciais(seedEmail: String, seedSenha: String = "123") {
        email = seedEmail
        senha = seedSenha
        erroEmail = null
        erroSenha = null
        erroGeral = null
    }

    fun login(onSuccess: (Usuario) -> Unit) {
        var valido = true
        erroEmail = null
        erroSenha = null
        erroGeral = null

        val emailTrimmed = email.trim().lowercase(Locale.ROOT)

        // HU01 Caso 03: Campos vazios
        if (emailTrimmed.isBlank()) {
            erroEmail = "Informe o e-mail"
            valido = false
        }
        if (senha.isBlank()) {
            erroSenha = "Informe a senha"
            valido = false
        }

        if (!valido) return

        // HU01 Caso 04: Formato invalido
        if (!emailTrimmed.contains("@") || emailTrimmed.startsWith("@") || emailTrimmed.endsWith("@")) {
            erroEmail = "E-mail inválido"
            return
        }

        // HU01 Caso 05 & RN01: Dominio institucional
        val isDominioInstitucional = emailTrimmed.endsWith("@aluno.ifsp.edu.br") ||
                emailTrimmed.endsWith("@ifsp.edu.br") ||
                emailTrimmed.endsWith("@aluno.if.edu.br") ||
                emailTrimmed.endsWith("@if.edu.br")

        if (!isDominioInstitucional) {
            erroEmail = "Use seu e-mail institucional"
            return
        }

        viewModelScope.launch {
            isLoading = true
            val result = repository.login(emailTrimmed, senha)
            isLoading = false

            result.onSuccess { usuario ->
                onSuccess(usuario)
            }.onFailure { ex ->
                val msg = ex.message ?: "Erro ao autenticar."
                when {
                    msg.contains("institucional") -> erroEmail = msg
                    msg.contains("inválido") -> erroEmail = msg
                    msg.contains("incorretos") -> erroEmail = msg
                    else -> erroGeral = msg
                }
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        repository.logout()
        email = ""
        senha = ""
        erroEmail = null
        erroSenha = null
        erroGeral = null
        onLoggedOut()
    }
}
