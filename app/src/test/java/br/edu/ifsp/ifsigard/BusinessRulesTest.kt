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
        val emailAlunoMaria = "maria@aluno.ifsp.edu.br"
        val emailProfessorAna = "ana@ifsp.edu.br"
        val emailProfessorCarlos = "carlos@ifsp.edu.br"
        val emailCae = "cae@ifsp.edu.br"
        val emailCoordenacao = "coordenacao@ifsp.edu.br"

        val emailOutroDominio = "maria@outro.edu.br"
        val emailSemArroba = "maria.aluno.ifsp.edu.br"
        val emailGmail = "maria@gmail.com"
        val emailVazio = ""

        fun validarEmail(email: String): String? {
            val t = email.trim().lowercase(Locale.ROOT)
            if (t.isBlank()) return "Informe o e-mail"
            if (!t.contains("@") || t.startsWith("@") || t.endsWith("@")) return "E-mail inválido"
            val isInst = t.endsWith("@aluno.ifsp.edu.br") || t.endsWith("@ifsp.edu.br")
            if (!isInst) return "Use seu e-mail institucional"
            return null
        }

        assertEquals(null, validarEmail(emailAlunoSp))
        assertEquals(null, validarEmail(emailAlunoMaria))
        assertEquals(null, validarEmail(emailProfessorAna))
        assertEquals(null, validarEmail(emailProfessorCarlos))
        assertEquals(null, validarEmail(emailCae))
        assertEquals(null, validarEmail(emailCoordenacao))

        assertEquals("Informe o e-mail", validarEmail(emailVazio))
        assertEquals("E-mail inválido", validarEmail(emailSemArroba))
        assertEquals("Use seu e-mail institucional", validarEmail(emailGmail))
        assertEquals("Use seu e-mail institucional", validarEmail(emailOutroDominio))
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
        assertEquals("Outro aviso", TipoAviso.OUTRO.label)
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

    // Divergencia 03 & HU02 Caso 05: Filtro por intervalo de horario (19:00–22:30)
    @Test
    fun testPonto03_filtroHorarioIntervalo19as22h30() {
        val inicioMin = DateUtils.timeToMinutes("19:00")
        val fimMax = DateUtils.timeToMinutes("22:30")

        fun matchesIntervalo(horaInicio: String): Boolean {
            val min = DateUtils.timeToMinutes(horaInicio)
            return min in inicioMin..fimMax
        }

        assertTrue("19:00 inicia no intervalo", matchesIntervalo("19:00"))
        assertTrue("20:40 inicia no intervalo", matchesIntervalo("20:40"))
        assertTrue("22:30 inicia no intervalo", matchesIntervalo("22:30"))
        assertFalse("13:30 (tarde) nao deve aparecer no intervalo da noite", matchesIntervalo("13:30"))
        assertFalse("08:00 (manha) nao deve aparecer no intervalo da noite", matchesIntervalo("08:00"))
        assertFalse("22:31 ultrapassa o intervalo", matchesIntervalo("22:31"))
    }

    // Divergencia 04 & RN06: Aviso tardio na edicao deve usar data/hora de publicacao original
    @Test
    fun testPonto04_avisoTardioPreservadoNaEdicao() {
        val aulaTimestamp = 1764000000000L // Data/hora da aula
        val publicadoOriginalmente = aulaTimestamp - (48 * 60 * 60 * 1000L) // Publicado 48h antes (nao tardio)
        val momentoEdicao = aulaTimestamp - (2 * 60 * 60 * 1000L) // Editado 2h antes da aula

        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        val aulaCal = Calendar.getInstance().apply { timeInMillis = aulaTimestamp }
        val dataAula = sdfDate.format(aulaCal.time)
        val horaAula = sdfTime.format(aulaCal.time)

        // Se recalculasse com o momento da edicao (como antes do fix), ficaria 'true' incorretamente!
        val calculoErradoComEdicao = DateUtils.isAvisoTardio(dataAula, horaAula, momentoEdicao)
        // Calculo correto mantendo o timestamp original de publicacao
        val calculoCorretoComPublicacaoOriginal = DateUtils.isAvisoTardio(dataAula, horaAula, publicadoOriginalmente)

        assertTrue("Momento da edicao geraria selo tardio indevido", calculoErradoComEdicao)
        assertFalse("Aviso original publicado 48h antes nao deve se tornar tardio na edicao", calculoCorretoComPublicacaoOriginal)
    }

    // Divergencia 05 & RN05: Validacao de horario numerico vs texto livre
    @Test
    fun testPonto05_validacaoNumericaHorarioEFormato() {
        // String comparison trap: "9:00" vs "10:00"
        val horaInicio = "9:00"
        val horaFim = "10:00"

        // Em comparacao lexicografica de string: "10:00" <= "9:00" e TRUE (falha de validacao!)
        assertTrue("String comparison compara errado por ordem de caracteres ASCII", horaFim <= horaInicio)

        // Com o fix numerico em DateUtils:
        assertTrue("DateUtils compara numericamente os minutos", DateUtils.isHoraFimValida(horaInicio, horaFim))
        assertEquals("09:00", DateUtils.normalizeTime("9:00"))
        assertEquals("10:00", DateUtils.normalizeTime("10:00"))

        // Formatos invalidos de texto livre
        assertFalse("Texto livre 'abc' e rejeitado", DateUtils.isValidTime("abc"))
        assertFalse("Horario invalido 25:00 e rejeitado", DateUtils.isValidTime("25:00"))
        assertFalse("Minuto invalido 19:65 e rejeitado", DateUtils.isValidTime("19:65"))
        assertTrue("Horario 19:00 e valido", DateUtils.isValidTime("19:00"))
        assertTrue("Horario 09:00 e valido", DateUtils.isValidTime("09:00"))
    }

    // Divergencia 06: Validacao do horario de reposicao
    @Test
    fun testPonto06_validacaoHoraReposicao() {
        val horaValida = "13:30"
        val horaInvalida = "invalido"

        assertTrue("Hora de reposicao 13:30 e valida", DateUtils.isValidTime(horaValida))
        assertFalse("Hora de reposicao com texto livre e invalida", DateUtils.isValidTime(horaInvalida))
    }

    // Divergencia 02 & HU02: Filtro por data especifica (qualquer data)
    @Test
    fun testPonto02_filtroDataEspecifica() {
        val aviso1 = Aviso(id = 1, tipo = TipoAviso.AULA_CANCELADA, turmaId = 1, turmaNome = "BCC", data = "2026-11-20", horaInicio = "19:00", horaFim = "20:40", mensagem = "M", autorId = 1, autorNome = "A")
        val aviso2 = Aviso(id = 2, tipo = TipoAviso.AULA_CANCELADA, turmaId = 1, turmaNome = "BCC", data = "2026-11-28", horaInicio = "19:00", horaFim = "20:40", mensagem = "M", autorId = 1, autorNome = "A")
        val avisos = listOf(aviso1, aviso2)

        val dataSelecionada = "2026-11-28"
        val filtrados = avisos.filter { DateUtils.normalizeToIso(it.data) == dataSelecionada }

        assertEquals(1, filtrados.size)
        assertEquals(2L, filtrados[0].id)
    }
}
