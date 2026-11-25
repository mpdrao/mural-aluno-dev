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
    OUTRA_DATA("Outra data"),
    TODOS("Todos")
}

enum class FiltroHorario(val label: String) {
    TODOS("Todos"),
    NOITE("19:00–22:30"),
    TARDE("13:30–17:10")
}

// Compatibilidade retroativa
typealias FiltroTurno = FiltroHorario

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

    var dataEspecificaIso by mutableStateOf<String?>(null)
        private set

    var filtroHorario by mutableStateOf(FiltroHorario.TODOS)
        private set

    val filtroTurno: FiltroHorario
        get() = filtroHorario

    var avisoParaExcluir by mutableStateOf<Aviso?>(null)
        private set

    var feedbackMensagem by mutableStateOf<String?>(null)
        private set

    fun setTurmaFiltro(turmaId: Long?) {
        filtroTurmaId = turmaId
    }

    fun setDataFiltro(filtro: FiltroData, dataIso: String? = null) {
        filtroData = filtro
        if (filtro == FiltroData.OUTRA_DATA && dataIso != null) {
            dataEspecificaIso = DateUtils.normalizeToIso(dataIso)
        } else if (filtro != FiltroData.OUTRA_DATA) {
            dataEspecificaIso = null
        }
    }

    fun setHorarioFiltro(filtro: FiltroHorario) {
        filtroHorario = filtro
    }

    fun setTurnoFiltro(filtro: FiltroHorario) {
        filtroHorario = filtro
    }

    fun avisosFiltrados(): List<Aviso> {
        val hoje = DateUtils.todayIso()
        val amanha = DateUtils.tomorrowIso()

        return todosAvisos.value.filter { aviso ->
            val dataIso = DateUtils.normalizeToIso(aviso.data)

            // Filtro por turma (HU02 Caso 03)
            val matchTurma = filtroTurmaId == null || aviso.turmaId == filtroTurmaId

            // Filtro por data (HU02 Caso 01 & Caso 04 & Data personalizada)
            val matchData = when (filtroData) {
                FiltroData.HOJE -> dataIso == hoje
                FiltroData.AMANHA -> dataIso == amanha
                FiltroData.OUTRA_DATA -> dataEspecificaIso != null && dataIso == dataEspecificaIso
                FiltroData.TODOS -> true
            }

            // Filtro por intervalo de horario (HU02 Caso 05: 19:00–22:30 | So avisos que comecam nesse intervalo aparecem)
            val matchHorario = when (filtroHorario) {
                FiltroHorario.TODOS -> true
                FiltroHorario.NOITE -> {
                    val minInicio = DateUtils.timeToMinutes(aviso.horaInicio)
                    minInicio in DateUtils.timeToMinutes("19:00")..DateUtils.timeToMinutes("22:30")
                }
                FiltroHorario.TARDE -> {
                    val minInicio = DateUtils.timeToMinutes(aviso.horaInicio)
                    minInicio in DateUtils.timeToMinutes("13:30")..DateUtils.timeToMinutes("17:10")
                }
            }

            matchTurma && matchData && matchHorario
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
