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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class FiltroData(val label: String) {
    HOJE("Hoje"),
    PROXIMOS_7_DIAS("Próximos 7 dias"),
    TODOS("Todos")
}

enum class FiltroTurno(val label: String) {
    TODOS("Todos"),
    MANHA("Manhã"),
    TARDE("Tarde"),
    NOITE("Noite")
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
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hoje = sdf.format(Date())

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 7)
        val daquiASeteDias = sdf.format(cal.time)

        return todosAvisos.value.filter { aviso ->
            // Filtro por turma
            val matchTurma = filtroTurmaId == null || aviso.turmaId == filtroTurmaId

            // Filtro por data (RN08)
            val matchData = when (filtroData) {
                FiltroData.HOJE -> aviso.data == hoje
                FiltroData.PROXIMOS_7_DIAS -> aviso.data >= hoje && aviso.data <= daquiASeteDias
                FiltroData.TODOS -> true
            }

            // Filtro por turno / horario
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
                feedbackMensagem = "Aviso excluído com sucesso."
            }.onFailure { ex ->
                feedbackMensagem = ex.message ?: "Erro ao excluir aviso."
            }
        }
    }

    fun limparFeedback() {
        feedbackMensagem = null
    }
}
