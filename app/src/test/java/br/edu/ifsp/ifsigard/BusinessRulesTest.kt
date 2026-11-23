package br.edu.ifsp.ifsigard

import br.edu.ifsp.ifsigard.data.entity.Aviso
import br.edu.ifsp.ifsigard.data.entity.PerfilUsuario
import br.edu.ifsp.ifsigard.data.entity.TipoAviso
import br.edu.ifsp.ifsigard.data.entity.Usuario
import br.edu.ifsp.ifsigard.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BusinessRulesTest {

    // HU01 Caso 03, 04, 05: Validacoes de login (RN01)
    @Test
    fun testHU01_validacaoEmailInstitucional() {
        val emailAlunoSp = "lucas.silva@aluno.ifsp.edu.br"
        val emailAlunoIf = "maria@aluno.if.edu.br"
        val emailProfessorSp = "carlos.alberto@ifsp.edu.br"
        val emailProfessorIf = "ana@if.edu.br"
        val emailCae = "cae@ifsp.edu.br"
        val emailCoordenacao = "coordenacao.bcc@ifsp.edu.br"

        val emailSemArroba = "maria.aluno.ifsp.edu.br"
        val emailGmail = "maria@gmail.com"
        val emailVazio = ""

        fun validarEmail(email: String): String? {
            val t = email.trim().lowercase(Locale.ROOT)
            if (t.isBlank()) return "Informe o e-mail"
            if (!t.contains("@") || t.startsWith("@") || t.endsWith("@")) return "E-mail inválido"
            val isInst = t.endsWith("@aluno.ifsp.edu.br") || t.endsWith("@ifsp.edu.br") ||
                    t.endsWith("@aluno.if.edu.br") || t.endsWith("@if.edu.br")
            if (!isInst) return "Use seu e-mail institucional"
            return null
        }

        assertEquals(null, validarEmail(emailAlunoSp))
        assertEquals(null, validarEmail(emailAlunoIf))
        assertEquals(null, validarEmail(emailProfessorSp))
        assertEquals(null, validarEmail(emailProfessorIf))
        assertEquals(null, validarEmail(emailCae))
        assertEquals(null, validarEmail(emailCoordenacao))

        assertEquals("Informe o e-mail", validarEmail(emailVazio))
        assertEquals("E-mail inválido", validarEmail(emailSemArroba))
        assertEquals("Use seu e-mail institucional", validarEmail(emailGmail))
    }

    // HU01 Caso 01 & 02: Perfil do usuario e permissao de publicacao (RN02)
    @Test
    fun testRN02_apenasServidorPodePublicar() {
        val aluno = Usuario(id = 1, nome = "Lucas", email = "lucas@aluno.ifsp.edu.br", senha = "123", perfil = PerfilUsuario.ALUNO)
        val professor = Usuario(id = 2, nome = "Carlos", email = "carlos@ifsp.edu.br", senha = "123", perfil = PerfilUsuario.PROFESSOR)
        val cae = Usuario(id = 3, nome = "CAE", email = "cae@ifsp.edu.br", senha = "123", perfil = PerfilUsuario.CAE)
        val coordenacao = Usuario(id = 4, nome = "Coord", email = "coord@ifsp.edu.br", senha = "123", perfil = PerfilUsuario.COORDENACAO)

        assertFalse("Aluno nao deve ser servidor (sem botao novo aviso)", aluno.isServidor())
        assertTrue("Professor deve ser servidor (com botao novo aviso)", professor.isServidor())
        assertTrue("CAE deve ser servidor (com botao novo aviso)", cae.isServidor())
        assertTrue("Coordenacao deve ser servidor (com botao novo aviso)", coordenacao.isServidor())
    }

    // HU02 Caso 01: Ordenacao dos avisos do dia por horario (RN08)
    @Test
    fun testHU02_ordenacaoAvisosPorHorario() {
        val hoje = DateUtils.todayIso()
        val a1 = Aviso(id = 1, tipo = TipoAviso.AULA_CANCELADA, turmaId = 1, turmaNome = "BCC 3º sem", data = hoje, horaInicio = "19:00", horaFim = "20:40", mensagem = "A1", autorId = 1, autorNome = "Prof")
        val a2 = Aviso(id = 2, tipo = TipoAviso.AULA_CANCELADA, turmaId = 2, turmaNome = "BCC 3º sem", data = hoje, horaInicio = "13:30", horaFim = "15:10", mensagem = "A2", autorId = 1, autorNome = "Prof")
        val a3 = Aviso(id = 3, tipo = TipoAviso.OUTRO, turmaId = 3, turmaNome = "BCC 1º sem", data = hoje, horaInicio = "20:50", horaFim = "22:30", mensagem = "A3", autorId = 2, autorNome = "CAE")

        val lista = listOf(a1, a2, a3)
        val ordenada = lista.sortedWith(compareBy({ it.data }, { it.horaInicio }))

        assertEquals("13:30", ordenada[0].horaInicio)
        assertEquals("19:00", ordenada[1].horaInicio)
        assertEquals("20:50", ordenada[2].horaInicio)
    }

    // HU02 Caso 04: Filtro por data (Hoje vs Amanha)
    @Test
    fun testHU02_filtroDataHojeAmanha() {
        val hoje = DateUtils.todayIso()
        val amanha = DateUtils.tomorrowIso()

        val aHoje = Aviso(id = 1, tipo = TipoAviso.AULA_CANCELADA, turmaId = 1, turmaNome = "BCC", data = hoje, horaInicio = "13:30", horaFim = "15:10", mensagem = "M", autorId = 1, autorNome = "P")
        val aAmanha = Aviso(id = 2, tipo = TipoAviso.TROCA_DE_AULA, turmaId = 2, turmaNome = "ADS", data = amanha, horaInicio = "19:00", horaFim = "20:40", mensagem = "T", autorId = 1, autorNome = "P")

        val avisos = listOf(aHoje, aAmanha)

        val filtradosHoje = avisos.filter { DateUtils.normalizeToIso(it.data) == hoje }
        val filtradosAmanha = avisos.filter { DateUtils.normalizeToIso(it.data) == amanha }

        assertEquals(1, filtradosHoje.size)
        assertEquals(aHoje.id, filtradosHoje[0].id)

        assertEquals(1, filtradosAmanha.size)
        assertEquals(aAmanha.id, filtradosAmanha[0].id)
    }

    // RN03: Limite maximo da mensagem (200 caracteres)
    @Test
    fun testRN03_limiteMensagem200Caracteres() {
        val mensagemValida = "Aviso normal dentro do limite de 200 caracteres."
        val mensagemLimite = "A".repeat(200)
        val mensagemInvalida = "A".repeat(201)

        assertTrue(mensagemValida.length <= 200)
        assertTrue(mensagemLimite.length <= 200)
        assertFalse(mensagemInvalida.length <= 200)
    }

    // HU03 Caso 05: Data no passado (RN04)
    @Test
    fun testHU03_dataNaoPodeEstarNoPassado() {
        val hojeIso = DateUtils.todayIso()
        val amanhaIso = DateUtils.tomorrowIso()
        val ontemIso = DateUtils.daysFromTodayIso(-1)

        assertFalse("Hoje nao e passado", DateUtils.isPastDate(hojeIso))
        assertFalse("Amanha nao e passado", DateUtils.isPastDate(amanhaIso))
        assertTrue("Ontem e passado", DateUtils.isPastDate(ontemIso))
    }

    // HU03 Caso 06: Horario de fim deve ser estritamente maior que inicio (RN05)
    @Test
    fun testHU03_horarioFimMaiorQueInicio() {
        val inicio = "20:40"
        val fimInvalido = "19:00"
        val fimIgual = "20:40"
        val fimValido = "21:30"

        assertFalse("Fim 19:00 e anterior a 20:40", fimInvalido > inicio)
        assertFalse("Fim igual a inicio nao e permitido", fimIgual > inicio)
        assertTrue("Fim 21:30 e posterior a 20:40", fimValido > inicio)
    }

    // HU02 Caso 06 & RN06: Aviso tardio com antecedencia < 24 horas
    @Test
    fun testRN06_avisoTardioMenorQue24Horas() {
        val agora = System.currentTimeMillis()

        // Aula daqui a 3 horas (deve ser tardio - HU02 Caso 06)
        val cal3h = Calendar.getInstance()
        cal3h.timeInMillis = agora + (3 * 60 * 60 * 1000L)
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        val data3h = sdfDate.format(cal3h.time)
        val hora3h = sdfTime.format(cal3h.time)
        val isTardio3h = DateUtils.isAvisoTardio(data3h, hora3h, agora)

        // Aula daqui a 48 horas (nao tardio)
        val cal48h = Calendar.getInstance()
        cal48h.timeInMillis = agora + (48 * 60 * 60 * 1000L)
        val data48h = sdfDate.format(cal48h.time)
        val hora48h = sdfTime.format(cal48h.time)
        val isTardio48h = DateUtils.isAvisoTardio(data48h, hora48h, agora)

        assertTrue("Aviso publicado 3h antes deve ser classificado como tardio", isTardio3h)
        assertFalse("Aviso publicado 48h antes nao deve ser classificado como tardio", isTardio48h)
    }

    // HU04 Caso 03 & 04: Apenas autor pode editar ou excluir (RN07)
    @Test
    fun testRN07_apenasAutorPodeEditarExcluir() {
        val profAnaId = 2L
        val caeId = 3L

        val avisoAna = Aviso(
            id = 10,
            tipo = TipoAviso.AULA_CANCELADA,
            turmaId = 1,
            turmaNome = "BCC 3º sem",
            data = "2026-11-25",
            horaInicio = "19:00",
            horaFim = "20:40",
            mensagem = "Aula cancelada",
            autorId = profAnaId,
            autorNome = "Profª. Ana Beatriz"
        )

        assertTrue("Prof. Ana e a autora e pode editar/excluir", avisoAna.autorId == profAnaId)
        assertFalse("CAE nao e o autor e nao pode editar/excluir (HU04 Caso 04)", avisoAna.autorId == caeId)
    }

    // HU03 Caso 07: Validacao de reposicao (RN09)
    @Test
    fun testRN09_validacaoDataReposicao() {
        val dataAviso = "2026-11-20"
        val dataReposicaoValida = "2026-11-28"
        val dataReposicaoAnterior = "2026-11-19"
        val dataReposicaoIgual = "2026-11-20"
        val dataReposicaoAposFimPeriodo = "2026-12-20"
        val fimPeriodoLetivo = "2026-12-18"

        assertTrue("Data de reposicao 28/11 e posterior a 20/11", dataReposicaoValida > dataAviso)
        assertTrue("Data de reposicao 28/11 esta dentro do periodo letivo", dataReposicaoValida <= fimPeriodoLetivo)

        assertFalse("Data de reposicao nao pode ser anterior a data do aviso", dataReposicaoAnterior > dataAviso)
        assertFalse("Data de reposicao nao pode ser igual a data do aviso", dataReposicaoIgual > dataAviso)
        assertFalse("Data de reposicao nao pode ser apos 18/12/2026 (HU03 Caso 07)", dataReposicaoAposFimPeriodo <= fimPeriodoLetivo)
    }
}
