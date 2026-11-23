package br.edu.ifsp.ifsigard.data.repository

import br.edu.ifsp.ifsigard.data.entity.Aviso
import br.edu.ifsp.ifsigard.data.entity.TipoAviso
import br.edu.ifsp.ifsigard.data.entity.Turma
import br.edu.ifsp.ifsigard.data.entity.Usuario
import br.edu.ifsp.ifsigard.data.local.AppDatabase
import br.edu.ifsp.ifsigard.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class MuralRepository(
    private val database: AppDatabase,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val usuarioDao = database.usuarioDao()
    private val turmaDao = database.turmaDao()
    private val avisoDao = database.avisoDao()

    private val _currentUser = MutableStateFlow<Usuario?>(null)
    val currentUser: StateFlow<Usuario?> = _currentUser.asStateFlow()

    init {
        coroutineScope.launch {
            AppDatabase.ensureSeeded(database)
        }
    }

    suspend fun login(email: String, senha: String): Result<Usuario> {
        val emailTrimmed = email.trim().lowercase(Locale.ROOT)

        // Garante que o banco contenha os dados do seed caso seja a primeira chamada
        if (usuarioDao.count() == 0) {
            AppDatabase.ensureSeeded(database)
        }

        // HU01 Caso 03: Campos vazios
        if (emailTrimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("Informe o e-mail"))
        }
        if (senha.isBlank()) {
            return Result.failure(IllegalArgumentException("Informe a senha"))
        }

        // HU01 Caso 04: Formato invalido
        if (!emailTrimmed.contains("@") || emailTrimmed.startsWith("@") || emailTrimmed.endsWith("@")) {
            return Result.failure(IllegalArgumentException("E-mail inválido"))
        }

        // HU01 Caso 05 & RN01: Dominio institucional (@aluno.ifsp.edu.br, @ifsp.edu.br, @aluno.if.edu.br, @if.edu.br)
        val isDominioInstitucional = emailTrimmed.endsWith("@aluno.ifsp.edu.br") ||
                emailTrimmed.endsWith("@ifsp.edu.br") ||
                emailTrimmed.endsWith("@aluno.if.edu.br") ||
                emailTrimmed.endsWith("@if.edu.br")

        if (!isDominioInstitucional) {
            return Result.failure(IllegalArgumentException("Use seu e-mail institucional"))
        }

        // HU01 Caso 06 & RN01: Senha incorreta ou email nao cadastrado
        val usuario = usuarioDao.findByEmail(emailTrimmed)
        if (usuario == null || usuario.senha != senha) {
            return Result.failure(IllegalArgumentException("E-mail ou senha incorretos"))
        }

        _currentUser.value = usuario
        return Result.success(usuario)
    }

    fun logout() {
        _currentUser.value = null
    }

    fun getTurmas(): Flow<List<Turma>> = turmaDao.getAll()

    suspend fun getTurmasList(): List<Turma> = turmaDao.getAllList()

    fun getAvisos(): Flow<List<Aviso>> = avisoDao.getAllFlow()

    suspend fun getAvisoById(id: Long): Aviso? = avisoDao.findById(id)

    suspend fun salvarAviso(
        id: Long = 0,
        tipo: TipoAviso,
        turmaId: Long,
        turmaNome: String,
        data: String,
        horaInicio: String,
        horaFim: String,
        mensagem: String,
        substituto: String?,
        reposicaoData: String?,
        reposicaoHora: String?,
        autor: Usuario
    ): Result<Long> {
        // RN02: Apenas servidores podem publicar
        if (!autor.isServidor()) {
            return Result.failure(IllegalStateException("Apenas servidores (Professores, CAE, Coordenação) podem publicar avisos."))
        }

        // RN03: Campos obrigatorios
        if (turmaId <= 0 || turmaNome.isBlank()) {
            return Result.failure(IllegalArgumentException("Selecione a turma"))
        }
        if (data.isBlank()) {
            return Result.failure(IllegalArgumentException("Informe a data"))
        }
        if (horaInicio.isBlank()) {
            return Result.failure(IllegalArgumentException("Informe o horário de início"))
        }
        if (horaFim.isBlank()) {
            return Result.failure(IllegalArgumentException("Informe o horário de fim"))
        }
        if (mensagem.isBlank()) {
            return Result.failure(IllegalArgumentException("Informe a mensagem"))
        }
        if (mensagem.length > 200) {
            return Result.failure(IllegalArgumentException("A mensagem deve ter no máximo 200 caracteres"))
        }
        if (tipo == TipoAviso.TROCA_DE_AULA && substituto.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Informe a disciplina ou o professor substituto"))
        }

        val dataIso = DateUtils.normalizeToIso(data)

        // RN04: Data nao pode estar no passado
        if (DateUtils.isPastDate(dataIso)) {
            return Result.failure(IllegalArgumentException("A data não pode estar no passado"))
        }

        // RN05: Hora de fim deve ser maior que inicio
        if (horaFim <= horaInicio) {
            return Result.failure(IllegalArgumentException("A hora de fim deve ser maior que a de início"))
        }

        // RN09: Reposicao opcional
        val reposicaoIso = reposicaoData?.let { DateUtils.normalizeToIso(it) }
        if (!reposicaoIso.isNullOrBlank()) {
            if (reposicaoIso <= dataIso) {
                return Result.failure(IllegalArgumentException("A reposição deve ser posterior à data do aviso"))
            }
            if (reposicaoIso > "2026-12-18") {
                return Result.failure(IllegalArgumentException("A reposição deve ser até 18/12/2026"))
            }
        }

        // RN06: Calculo de aviso tardio (< 24h de antecedencia)
        val isTardio = DateUtils.isAvisoTardio(dataIso, horaInicio)

        return if (id == 0L) {
            val novoAviso = Aviso(
                tipo = tipo,
                turmaId = turmaId,
                turmaNome = turmaNome,
                data = dataIso,
                horaInicio = horaInicio,
                horaFim = horaFim,
                mensagem = mensagem.trim(),
                substituto = substituto?.trim()?.ifBlank { null },
                reposicaoData = reposicaoIso?.trim()?.ifBlank { null },
                reposicaoHora = reposicaoHora?.trim()?.ifBlank { null },
                autorId = autor.id,
                autorNome = autor.nome,
                publicadoEm = System.currentTimeMillis(),
                isTardio = isTardio
            )
            val insertedId = avisoDao.insert(novoAviso)
            Result.success(insertedId)
        } else {
            val avisoExistente = avisoDao.findById(id)
                ?: return Result.failure(IllegalArgumentException("Aviso não encontrado."))

            // RN07: Apenas autor pode editar
            if (avisoExistente.autorId != autor.id) {
                return Result.failure(IllegalStateException("Apenas o autor do aviso pode editá-lo."))
            }

            val avisoAtualizado = avisoExistente.copy(
                tipo = tipo,
                turmaId = turmaId,
                turmaNome = turmaNome,
                data = dataIso,
                horaInicio = horaInicio,
                horaFim = horaFim,
                mensagem = mensagem.trim(),
                substituto = substituto?.trim()?.ifBlank { null },
                reposicaoData = reposicaoIso?.trim()?.ifBlank { null },
                reposicaoHora = reposicaoHora?.trim()?.ifBlank { null },
                isTardio = isTardio
            )
            avisoDao.update(avisoAtualizado)
            Result.success(id)
        }
    }

    suspend fun excluirAviso(aviso: Aviso, solicitante: Usuario): Result<Unit> {
        // RN07: Apenas o autor do aviso pode excluir
        if (aviso.autorId != solicitante.id) {
            return Result.failure(IllegalStateException("Apenas o autor pode excluir este aviso."))
        }
        avisoDao.delete(aviso)
        return Result.success(Unit)
    }
}
