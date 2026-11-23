package br.edu.ifsp.ifsigard.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.ifsigard.data.entity.Aviso
import br.edu.ifsp.ifsigard.data.entity.Turma
import br.edu.ifsp.ifsigard.data.entity.Usuario
import br.edu.ifsp.ifsigard.data.repository.MuralRepository
import br.edu.ifsp.ifsigard.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FiltroData(val label: String) {
    HOJE("Hoje"),
    AMANHA("Amanhã"),
    TODOS("Todos")
}

enum class FiltroTurno(val label: String) {
    TODOS("Todos"),
    TARDE("13:30–17:10"),
    NOITE("19:00–22:30"),
    MANHA("07:00–12:00")
}

class MuralViewModel(private val repository: MuralRepository) : ViewModel() {

    val currentUser = repository.currentUser

    val turmas: StateFlow<List<Turma>> = repository.getTurmas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todosAvisos: StateFlow<List<Aviso>> = repository.getAvisos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var filtroTurmaId by mutableStateOf<Long?>(null)
        private set

    var filtroData by mutableStateOf(FiltroData.HOJE)
        private set

    var filtroTurno by mutableStateOf(FiltroTurno.TODOS)
        private set

    var avisoParaExcluir by mutableStateOf<Aviso?>(null)
        private set

    var feedbackMensagem by mutableStateOf<String?>(null)
        private set

    fun setTurmaFiltro(turmaId: Long?) {
        filtroTurmaId = turmaId
    }

    fun setDataFiltro(filtro: FiltroData) {
        filtroData = filtro
    }

    fun setTurnoFiltro(filtro: FiltroTurno) {
        filtroTurno = filtro
    }

    fun avisosFiltrados(): List<Aviso> {
        val hoje = DateUtils.todayIso()
        val amanha = DateUtils.tomorrowIso()

        return todosAvisos.value.filter { aviso ->
            val dataIso = DateUtils.normalizeToIso(aviso.data)

            // Filtro por turma (HU02 Caso 03)
            val matchTurma = filtroTurmaId == null || aviso.turmaId == filtroTurmaId

            // Filtro por data (HU02 Caso 01 & Caso 04)
            val matchData = when (filtroData) {
                FiltroData.HOJE -> dataIso == hoje
                FiltroData.AMANHA -> dataIso == amanha
                FiltroData.TODOS -> true
            }

            // Filtro por turno / horario (HU02 Caso 05)
            val matchTurno = when (filtroTurno) {
                FiltroTurno.TODOS -> true
                FiltroTurno.MANHA -> aviso.horaInicio < "12:00"
                FiltroTurno.TARDE -> aviso.horaInicio >= "12:00" && aviso.horaInicio < "18:00"
                FiltroTurno.NOITE -> aviso.horaInicio >= "18:00"
            }

            matchTurma && matchData && matchTurno
        }.sortedWith(compareBy({ it.data }, { it.horaInicio }))
    }

    fun solicitarExclusao(aviso: Aviso) {
        avisoParaExcluir = aviso
    }

    fun cancelarExclusao() {
        avisoParaExcluir = null
    }

    fun confirmarExclusao(usuario: Usuario) {
        val aviso = avisoParaExcluir ?: return
        viewModelScope.launch {
            val result = repository.excluirAviso(aviso, usuario)
            avisoParaExcluir = null
            result.onSuccess {
                feedbackMensagem = "Aviso excluído"
            }.onFailure { ex ->
                feedbackMensagem = ex.message ?: "Erro ao excluir aviso."
            }
        }
    }

    fun definirFeedback(msg: String) {
        feedbackMensagem = msg
    }

    fun limparFeedback() {
        feedbackMensagem = null
    }
}
