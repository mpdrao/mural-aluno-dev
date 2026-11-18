package br.edu.ifsp.ifsigard.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.ifsigard.data.entity.TipoAviso
import br.edu.ifsp.ifsigard.data.entity.Turma
import br.edu.ifsp.ifsigard.data.entity.Usuario
import br.edu.ifsp.ifsigard.data.repository.MuralRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AvisoFormViewModel(private val repository: MuralRepository) : ViewModel() {

    val turmas: StateFlow<List<Turma>> = repository.getTurmas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var avisoId by mutableStateOf(0L)
        private set

    var tipo by mutableStateOf(TipoAviso.AULA_CANCELADA)
        private set

    var turmaId by mutableStateOf(0L)
        private set

    var turmaNome by mutableStateOf("")
        private set

    var data by mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
        private set

    var horaInicio by mutableStateOf("13:30")
        private set

    var horaFim by mutableStateOf("15:10")
        private set

    var mensagem by mutableStateOf("")
        private set

    var substituto by mutableStateOf("")
        private set

    var temReposicao by mutableStateOf(false)
        private set

    var reposicaoData by mutableStateOf("")
        private set

    var reposicaoHora by mutableStateOf("")
        private set

    // Validacoes individuais
    var erroTurma by mutableStateOf<String?>(null)
        private set
    var erroData by mutableStateOf<String?>(null)
        private set
    var erroHoraInicio by mutableStateOf<String?>(null)
        private set
    var erroHoraFim by mutableStateOf<String?>(null)
        private set
    var erroMensagem by mutableStateOf<String?>(null)
        private set
    var erroSubstituto by mutableStateOf<String?>(null)
        private set
    var erroReposicao by mutableStateOf<String?>(null)
        private set
    var erroGeral by mutableStateOf<String?>(null)
        private set

    var isSaving by mutableStateOf(false)
        private set

    val isEdicao: Boolean
        get() = avisoId > 0

    fun onTipoChange(novoTipo: TipoAviso) {
        tipo = novoTipo
        erroSubstituto = null
    }

    fun onTurmaSelected(turma: Turma) {
        turmaId = turma.id
        turmaNome = turma.nome
        erroTurma = null
    }

    fun onDataChange(novaData: String) {
        data = novaData
        erroData = null
    }

    fun onHoraInicioChange(hora: String) {
        horaInicio = hora
        erroHoraInicio = null
        erroHoraFim = null
    }

    fun onHoraFimChange(hora: String) {
        horaFim = hora
        erroHoraFim = null
    }

    fun onMensagemChange(novaMensagem: String) {
        if (novaMensagem.length <= 200) {
            mensagem = novaMensagem
            erroMensagem = null
        }
    }

    fun onSubstitutoChange(novoSubstituto: String) {
        substituto = novoSubstituto
        erroSubstituto = null
    }

    fun onTemReposicaoChange(checked: Boolean) {
        temReposicao = checked
        if (!checked) {
            reposicaoData = ""
            reposicaoHora = ""
            erroReposicao = null
        }
    }

    fun onReposicaoDataChange(novaData: String) {
        reposicaoData = novaData
        erroReposicao = null
    }

    fun onReposicaoHoraChange(novaHora: String) {
        reposicaoHora = novaHora
        erroReposicao = null
    }

    fun carregarAviso(id: Long) {
        if (id <= 0) return
        avisoId = id
        viewModelScope.launch {
            val aviso = repository.getAvisoById(id) ?: return@launch
            tipo = aviso.tipo
            turmaId = aviso.turmaId
            turmaNome = aviso.turmaNome
            data = aviso.data
            horaInicio = aviso.horaInicio
            horaFim = aviso.horaFim
            mensagem = aviso.mensagem
            substituto = aviso.substituto ?: ""
            temReposicao = !aviso.reposicaoData.isNullOrBlank()
            reposicaoData = aviso.reposicaoData ?: ""
            reposicaoHora = aviso.reposicaoHora ?: ""
        }
    }

    fun validar(): Boolean {
        var valido = true
        erroTurma = null
        erroData = null
        erroHoraInicio = null
        erroHoraFim = null
        erroMensagem = null
        erroSubstituto = null
        erroReposicao = null
        erroGeral = null

        if (turmaId <= 0) {
            erroTurma = "Selecione uma turma."
            valido = false
        }

        if (data.isBlank()) {
            erroData = "Data é obrigatória."
            valido = false
        } else {
            val hoje = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            if (data < hoje) {
                erroData = "Data não pode ser anterior a hoje."
                valido = false
            }
        }

        if (horaInicio.isBlank()) {
            erroHoraInicio = "Horário de início é obrigatório."
            valido = false
        }

        if (horaFim.isBlank()) {
            erroHoraFim = "Horário de término é obrigatório."
            valido = false
        } else if (horaFim <= horaInicio) {
            erroHoraFim = "Término deve ser maior que início."
            valido = false
        }

        if (mensagem.isBlank()) {
            erroMensagem = "Mensagem é obrigatória."
            valido = false
        } else if (mensagem.length > 200) {
            erroMensagem = "Máximo de 200 caracteres."
            valido = false
        }

        if (tipo == TipoAviso.TROCA_DE_AULA && substituto.isBlank()) {
            erroSubstituto = "Informe o docente ou disciplina substituta."
            valido = false
        }

        if (temReposicao) {
            if (reposicaoData.isBlank()) {
                erroReposicao = "Informe a data da reposição."
                valido = false
            } else {
                if (reposicaoData <= data) {
                    erroReposicao = "Data de reposição deve ser posterior à data do aviso."
                    valido = false
                } else if (reposicaoData > "2026-12-18") {
                    erroReposicao = "Data deve estar no período letivo (até 18/12/2026)."
                    valido = false
                }
            }
        }

        return valido
    }

    fun salvar(usuario: Usuario, onSuccess: () -> Unit) {
        if (!validar()) return

        viewModelScope.launch {
            isSaving = true
            val result = repository.salvarAviso(
                id = avisoId,
                tipo = tipo,
                turmaId = turmaId,
                turmaNome = turmaNome,
                data = data,
                horaInicio = horaInicio,
                horaFim = horaFim,
                mensagem = mensagem,
                substituto = if (tipo == TipoAviso.TROCA_DE_AULA) substituto else null,
                reposicaoData = if (temReposicao) reposicaoData else null,
                reposicaoHora = if (temReposicao) reposicaoHora else null,
                autor = usuario
            )
            isSaving = false

            result.onSuccess {
                onSuccess()
            }.onFailure { ex ->
                erroGeral = ex.message ?: "Erro ao salvar aviso."
            }
        }
    }
}
