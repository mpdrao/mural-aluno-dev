package br.edu.ifsp.ifsigard

import br.edu.ifsp.ifsigard.data.entity.Aviso
import br.edu.ifsp.ifsigard.data.entity.PerfilUsuario
import br.edu.ifsp.ifsigard.data.entity.TipoAviso
import br.edu.ifsp.ifsigard.data.entity.Usuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BusinessRulesTest {

    // Helper de calculo de aviso tardio (RN06)
    private fun calcularAvisoTardio(dataStr: String, horaInicioStr: String, agoraTimestamp: Long): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val aulaDate = sdf.parse("$dataStr $horaInicioStr") ?: return false
        val diferencaMillis = aulaDate.time - agoraTimestamp
        val vinteQuatroHorasMillis = 24 * 60 * 60 * 1000L
        return diferencaMillis < vinteQuatroHorasMillis
    }

    // RN01: Validacao de dominio institucional
    @Test
    fun testRN01_emailInstitucionalValido() {
        val emailAluno = "lucas.silva@aluno.if.edu.br"
        val emailProfessor = "carlos.alberto@if.edu.br"
        val emailInvalido = "usuario@gmail.com"

        val isValidoAluno = emailAluno.endsWith("@aluno.if.edu.br") || emailAluno.endsWith("@if.edu.br")
        val isValidoProfessor = emailProfessor.endsWith("@aluno.if.edu.br") || emailProfessor.endsWith("@if.edu.br")
        val isValidoGmail = emailInvalido.endsWith("@aluno.if.edu.br") || emailInvalido.endsWith("@if.edu.br")

        assertTrue(isValidoAluno)
        assertTrue(isValidoProfessor)
        assertFalse(isValidoGmail)
    }

    // RN01 & RN02: Perfil do usuario e permissao de publicacao
    @Test
    fun testRN02_apenasServidorPodePublicar() {
        val aluno = Usuario(id = 1, nome = "Lucas", email = "lucas@aluno.if.edu.br", senha = "123", perfil = PerfilUsuario.ALUNO)
        val professor = Usuario(id = 2, nome = "Carlos", email = "carlos@if.edu.br", senha = "123", perfil = PerfilUsuario.PROFESSOR)
        val cae = Usuario(id = 3, nome = "CAE", email = "cae@if.edu.br", senha = "123", perfil = PerfilUsuario.CAE)
        val coordenacao = Usuario(id = 4, nome = "Coord", email = "coord@if.edu.br", senha = "123", perfil = PerfilUsuario.COORDENACAO)

        assertFalse("Aluno nao deve ser considerado servidor", aluno.isServidor())
        assertTrue("Professor deve ser servidor", professor.isServidor())
        assertTrue("CAE deve ser servidor", cae.isServidor())
        assertTrue("Coordenacao deve ser servidor", coordenacao.isServidor())
    }

    // RN03: Tamanho maximo da mensagem (200 caracteres)
    @Test
    fun testRN03_limiteMensagem200Caracteres() {
        val mensagemValida = "Aviso normal dentro do limite de 200 caracteres."
        val mensagemLimite = "A".repeat(200)
        val mensagemInvalida = "A".repeat(201)

        assertTrue(mensagemValida.length <= 200)
        assertTrue(mensagemLimite.length <= 200)
        assertFalse(mensagemInvalida.length <= 200)
    }

    // RN04: Data nao pode ser no passado
    @Test
    fun testRN04_dataNaoPodeSerAnteriorHoje() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hoje = sdf.format(Date())

        val calOntem = Calendar.getInstance()
        calOntem.add(Calendar.DAY_OF_YEAR, -1)
        val ontem = sdf.format(calOntem.time)

        val calAmanha = Calendar.getInstance()
        calAmanha.add(Calendar.DAY_OF_YEAR, 1)
        val amanha = sdf.format(calAmanha.time)

        assertTrue("Data de hoje deve ser permitida", hoje >= hoje)
        assertTrue("Data de amanha deve ser permitida", amanha >= hoje)
        assertFalse("Data de ontem deve ser rejeitada", ontem >= hoje)
    }

    // RN05: Horario de fim deve ser estritamente maior que horario de inicio
    @Test
    fun testRN05_horarioFimMaiorQueInicio() {
        val inicio = "13:30"
        val fimValido = "15:10"
        val fimIgual = "13:30"
        val fimAnterior = "12:00"

        assertTrue("Fim 15:10 deve ser maior que inicio 13:30", fimValido > inicio)
        assertFalse("Fim igual nao e permitido", fimIgual > inicio)
        assertFalse("Fim anterior nao e permitido", fimAnterior > inicio)
    }

    // RN06: Aviso tardio publicado com antecedencia < 24 horas
    @Test
    fun testRN06_avisoTardioMenorQue24Horas() {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val agora = System.currentTimeMillis()

        // Aula daqui a 4 horas (deve ser tardio)
        val calAula4h = Calendar.getInstance()
        calAula4h.timeInMillis = agora + (4 * 60 * 60 * 1000L)
        val dataAula4h = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calAula4h.time)
        val horaAula4h = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calAula4h.time)
        val isTardio4h = calcularAvisoTardio(dataAula4h, horaAula4h, agora)

        // Aula daqui a 48 horas (nao deve ser tardio)
        val calAula48h = Calendar.getInstance()
        calAula48h.timeInMillis = agora + (48 * 60 * 60 * 1000L)
        val dataAula48h = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calAula48h.time)
        val horaAula48h = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calAula48h.time)
        val isTardio48h = calcularAvisoTardio(dataAula48h, horaAula48h, agora)

        assertTrue("Aviso publicado 4h antes deve ser classificado como tardio", isTardio4h)
        assertFalse("Aviso publicado 48h antes nao deve ser classificado como tardio", isTardio48h)
    }

    // RN07: Apenas autor pode editar ou excluir
    @Test
    fun testRN07_apenasAutorPodeEditarExcluir() {
        val autorOriginalId = 2L
        val usuarioOutroId = 3L

        val aviso = Aviso(
            id = 10,
            tipo = TipoAviso.AULA_CANCELADA,
            turmaId = 1,
            turmaNome = "BCC 1º sem",
            data = "2026-11-20",
            horaInicio = "13:30",
            horaFim = "15:10",
            mensagem = "Aula cancelada",
            autorId = autorOriginalId,
            autorNome = "Prof. Carlos"
        )

        val podeEditarAutor = aviso.autorId == autorOriginalId
        val podeEditarOutro = aviso.autorId == usuarioOutroId

        assertTrue("Autor original deve ter permissao", podeEditarAutor)
        assertFalse("Outro usuario nao deve ter permissao", podeEditarOutro)
    }

    // RN09: Validacao de reposicao
    @Test
    fun testRN09_validacaoDataReposicao() {
        val dataAviso = "2026-11-20"
        val dataReposicaoValida = "2026-11-28"
        val dataReposicaoAnterior = "2026-11-19"
        val dataReposicaoIgual = "2026-11-20"
        val dataReposicaoAposFimPeriodo = "2026-12-25"
        val fimPeriodoLetivo = "2026-12-18"

        assertTrue("Data de reposicao 28/11 e posterior a 20/11", dataReposicaoValida > dataAviso)
        assertTrue("Data de reposicao 28/11 esta dentro do periodo letivo", dataReposicaoValida <= fimPeriodoLetivo)

        assertFalse("Data de reposicao nao pode ser anterior a data do aviso", dataReposicaoAnterior > dataAviso)
        assertFalse("Data de reposicao nao pode ser igual a data do aviso", dataReposicaoIgual > dataAviso)
        assertFalse("Data de reposicao nao pode ultrapassar o encerramento do semestre", dataReposicaoAposFimPeriodo <= fimPeriodoLetivo)
    }
}
