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
import br.edu.ifsp.ifsigard.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    var data by mutableStateOf(DateUtils.todayBr())
        private set

    var horaInicio by mutableStateOf("19:00")
        private set

    var horaFim by mutableStateOf("20:40")
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

    // Erros de validacao individuais por campo (HU03)
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

    fun resetForm() {
        avisoId = 0L
        tipo = TipoAviso.AULA_CANCELADA
        turmaId = 0L
        turmaNome = ""
        data = DateUtils.todayBr()
        horaInicio = "19:00"
        horaFim = "20:40"
        mensagem = ""
        substituto = ""
        temReposicao = false
        reposicaoData = ""
        reposicaoHora = ""
        limparErros()
    }

    private fun limparErros() {
        erroTurma = null
        erroData = null
        erroHoraInicio = null
        erroHoraFim = null
        erroMensagem = null
        erroSubstituto = null
        erroReposicao = null
        erroGeral = null
    }

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
        if (id <= 0) {
            resetForm()
            return
        }
        avisoId = id
        limparErros()
        viewModelScope.launch {
            val aviso = repository.getAvisoById(id) ?: return@launch
            tipo = aviso.tipo
            turmaId = aviso.turmaId
            turmaNome = aviso.turmaNome
            data = DateUtils.formatForDisplay(aviso.data).let {
                if (it == "Hoje" || it == "Amanhã") DateUtils.normalizeToIso(aviso.data) else it
            }
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
        limparErros()

        // HU03 Caso 04: Sem turma
        if (turmaId <= 0) {
            erroTurma = "Selecione a turma"
            valido = false
        }

        // HU03 Caso 04: Sem data
        if (data.isBlank()) {
            erroData = "Informe a data"
            valido = false
        } else {
            // HU03 Caso 05: Data no passado (RN04)
            val dataIso = DateUtils.normalizeToIso(data)
            if (DateUtils.isPastDate(dataIso)) {
                erroData = "A data não pode estar no passado"
                valido = false
            }
        }

            // HU03: Horarios obrigatorios e validacao de formato (HH:mm)
        if (horaInicio.isBlank()) {
            erroHoraInicio = "Informe o horário de início"
            valido = false
        } else if (!DateUtils.isValidTime(horaInicio)) {
            erroHoraInicio = "Horário de início inválido (HH:mm)"
            valido = false
        }

        if (horaFim.isBlank()) {
            erroHoraFim = "Informe o horário de fim"
            valido = false
        } else if (!DateUtils.isValidTime(horaFim)) {
            erroHoraFim = "Horário de fim inválido (HH:mm)"
            valido = false
        } else if (erroHoraInicio == null && !DateUtils.isHoraFimValida(horaInicio, horaFim)) {
            // HU03 Caso 06: Horario invalido (RN05) - comparacao numerica
            erroHoraFim = "A hora de fim deve ser maior que a de início"
            valido = false
        }

        // HU03 Caso 04: Sem mensagem
        if (mensagem.isBlank()) {
            erroMensagem = "Informe a mensagem"
            valido = false
        } else if (mensagem.length > 200) {
            erroMensagem = "A mensagem deve ter no máximo 200 caracteres"
            valido = false
        }

        // HU03 Caso 03: Troca sem substituto (RN03)
        if (tipo == TipoAviso.TROCA_DE_AULA && substituto.isBlank()) {
            erroSubstituto = "Informe a disciplina ou o professor substituto"
            valido = false
        }

        // HU03 Caso 07: Reposicao opcional (RN09)
        if (temReposicao) {
            if (reposicaoData.isBlank()) {
                erroReposicao = "Informe a data da reposição"
                valido = false
            } else {
                val dataIso = DateUtils.normalizeToIso(data)
                val repoIso = DateUtils.normalizeToIso(reposicaoData)

                if (repoIso <= dataIso) {
                    erroReposicao = "A reposição deve ser posterior à data do aviso"
                    valido = false
                } else if (repoIso > "2026-12-18") {
                    erroReposicao = "A reposição deve ser até 18/12/2026"
                    valido = false
                } else if (reposicaoHora.isNotBlank() && !DateUtils.isValidTime(reposicaoHora)) {
                    erroReposicao = "Horário de reposição inválido (use o formato HH:mm)"
                    valido = false
                }
            }
        }

        return valido
    }

    fun salvar(usuario: Usuario, onSuccess: (String) -> Unit) {
        if (!validar()) return

        viewModelScope.launch {
            isSaving = true
            val dataIso = DateUtils.normalizeToIso(data)
            val repoIso = if (temReposicao && reposicaoData.isNotBlank()) DateUtils.normalizeToIso(reposicaoData) else null
            val hInicioNorm = DateUtils.normalizeTime(horaInicio)
            val hFimNorm = DateUtils.normalizeTime(horaFim)
            val hRepoNorm = if (temReposicao && reposicaoHora.isNotBlank()) DateUtils.normalizeTime(reposicaoHora) else null

            val result = repository.salvarAviso(
                id = avisoId,
                tipo = tipo,
                turmaId = turmaId,
                turmaNome = turmaNome,
                data = dataIso,
                horaInicio = hInicioNorm,
                horaFim = hFimNorm,
                mensagem = mensagem,
                substituto = if (tipo == TipoAviso.TROCA_DE_AULA) substituto else null,
                reposicaoData = repoIso,
                reposicaoHora = hRepoNorm,
                autor = usuario
            )
            isSaving = false

            result.onSuccess {
                val mensagemFeedback = if (isEdicao) "Aviso atualizado" else "Aviso publicado"
                onSuccess(mensagemFeedback)
            }.onFailure { ex ->
                erroGeral = ex.message ?: "Não foi possível salvar. Tente novamente."
            }
        }
    }
}
