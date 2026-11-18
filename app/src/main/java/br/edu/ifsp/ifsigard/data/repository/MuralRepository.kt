package br.edu.ifsp.ifsigard.data.repository

import br.edu.ifsp.ifsigard.data.entity.Aviso
import br.edu.ifsp.ifsigard.data.entity.PerfilUsuario
import br.edu.ifsp.ifsigard.data.entity.TipoAviso
import br.edu.ifsp.ifsigard.data.entity.Turma
import br.edu.ifsp.ifsigard.data.entity.Usuario
import br.edu.ifsp.ifsigard.data.local.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MuralRepository(private val database: AppDatabase) {

    private val usuarioDao = database.usuarioDao()
    private val turmaDao = database.turmaDao()
    private val avisoDao = database.avisoDao()

    private val _currentUser = MutableStateFlow<Usuario?>(null)
    val currentUser: StateFlow<Usuario?> = _currentUser.asStateFlow()

    suspend fun login(email: String, senha: String): Result<Usuario> {
        val emailTrimmed = email.trim().lowercase(Locale.ROOT)
        
        // RN01: Validacao de dominio institucional
        if (!emailTrimmed.endsWith("@aluno.if.edu.br") && !emailTrimmed.endsWith("@if.edu.br")) {
            return Result.failure(IllegalArgumentException("E-mail deve ser institucional (@aluno.if.edu.br ou @if.edu.br)"))
        }

        val usuario = usuarioDao.findByEmail(emailTrimmed)
            ?: return Result.failure(IllegalArgumentException("Credenciais invalidas. Usuario nao cadastrado."))

        if (usuario.senha != senha) {
            return Result.failure(IllegalArgumentException("Senha incorreta."))
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
            return Result.failure(IllegalStateException("Apenas servidores (Professores, CAE, Coordenacao) podem publicar avisos."))
        }

        // RN03: Campos obrigatorios
        if (turmaId <= 0 || turmaNome.isBlank()) {
            return Result.failure(IllegalArgumentException("Selecione uma turma."))
        }
        if (data.isBlank()) {
            return Result.failure(IllegalArgumentException("A data e obrigatoria."))
        }
        if (horaInicio.isBlank() || horaFim.isBlank()) {
            return Result.failure(IllegalArgumentException("Horarios de inicio e fim sao obrigatorios."))
        }
        if (mensagem.isBlank()) {
            return Result.failure(IllegalArgumentException("A mensagem e obrigatoria."))
        }
        if (mensagem.length > 200) {
            return Result.failure(IllegalArgumentException("A mensagem deve ter no maximo 200 caracteres."))
        }
        if (tipo == TipoAviso.TROCA_DE_AULA && substituto.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Para Troca de aula, informe o docente/disciplina substituta."))
        }

        // RN04: Data nao pode ser anterior a data atual
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hojeStr = sdfDate.format(Date())
        if (data < hojeStr) {
            return Result.failure(IllegalArgumentException("A data do aviso nao pode ser anterior a hoje."))
        }

        // RN05: Hora de fim deve ser estritamente maior que hora de inicio
        if (horaFim <= horaInicio) {
            return Result.failure(IllegalArgumentException("O horario de termino deve ser posterior ao inicio."))
        }

        // RN09: Reposicao opcional
        if (!reposicaoData.isNullOrBlank()) {
            if (reposicaoData <= data) {
                return Result.failure(IllegalArgumentException("A data de reposicao deve ser posterior a data do aviso."))
            }
            if (reposicaoData > "2026-12-18") {
                return Result.failure(IllegalArgumentException("A data de reposicao deve ocorrer dentro do periodo letivo (ate 18/12/2026)."))
            }
        }

        // RN06: Calculo de aviso tardio (< 24h de antecedencia)
        val isTardio = calcularAvisoTardio(data, horaInicio)

        return if (id == 0L) {
            val novoAviso = Aviso(
                tipo = tipo,
                turmaId = turmaId,
                turmaNome = turmaNome,
                data = data,
                horaInicio = horaInicio,
                horaFim = horaFim,
                mensagem = mensagem.trim(),
                substituto = substituto?.trim()?.ifBlank { null },
                reposicaoData = reposicaoData?.trim()?.ifBlank { null },
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
                ?: return Result.failure(IllegalArgumentException("Aviso nao encontrado."))

            // RN07: Apenas autor pode editar
            if (avisoExistente.autorId != autor.id) {
                return Result.failure(IllegalStateException("Apenas o autor do aviso pode edita-lo."))
            }

            val avisoAtualizado = avisoExistente.copy(
                tipo = tipo,
                turmaId = turmaId,
                turmaNome = turmaNome,
                data = data,
                horaInicio = horaInicio,
                horaFim = horaFim,
                mensagem = mensagem.trim(),
                substituto = substituto?.trim()?.ifBlank { null },
                reposicaoData = reposicaoData?.trim()?.ifBlank { null },
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

    // RN06
    fun calcularAvisoTardio(dataStr: String, horaInicioStr: String, agoraTimestamp: Long = System.currentTimeMillis()): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val aulaDate = sdf.parse("$dataStr $horaInicioStr") ?: return false
            val diferencaMillis = aulaDate.time - agoraTimestamp
            val vinteQuatroHorasMillis = 24 * 60 * 60 * 1000L
            diferencaMillis < vinteQuatroHorasMillis
        } catch (e: Exception) {
            false
        }
    }
}
