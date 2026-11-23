package br.edu.ifsp.ifsigard.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import br.edu.ifsp.ifsigard.data.dao.AvisoDao
import br.edu.ifsp.ifsigard.data.dao.CursoDao
import br.edu.ifsp.ifsigard.data.dao.TurmaDao
import br.edu.ifsp.ifsigard.data.dao.UsuarioDao
import br.edu.ifsp.ifsigard.data.entity.Aviso
import br.edu.ifsp.ifsigard.data.entity.Curso
import br.edu.ifsp.ifsigard.data.entity.PerfilUsuario
import br.edu.ifsp.ifsigard.data.entity.TipoAviso
import br.edu.ifsp.ifsigard.data.entity.Turma
import br.edu.ifsp.ifsigard.data.entity.Usuario
import br.edu.ifsp.ifsigard.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Database(
    entities = [Usuario::class, Curso::class, Turma::class, Aviso::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun cursoDao(): CursoDao
    abstract fun turmaDao(): TurmaDao
    abstract fun avisoDao(): AvisoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ifsigard_mural.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            scope.launch {
                                INSTANCE?.let { ensureSeeded(it) }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance

                // Garante a carga inicial logo apos a instanciacao
                scope.launch {
                    ensureSeeded(instance)
                }

                instance
            }
        }

        suspend fun ensureSeeded(database: AppDatabase) = withContext(Dispatchers.IO) {
            try {
                val usuarioDao = database.usuarioDao()
                val cursoDao = database.cursoDao()
                val turmaDao = database.turmaDao()
                val avisoDao = database.avisoDao()

                // 1. Usuarios (seed)
                if (usuarioDao.count() == 0) {
                    val usuarios = listOf(
                        // Alunos
                        Usuario(nome = "Lucas Silva", email = "lucas.silva@aluno.ifsp.edu.br", senha = "123", perfil = PerfilUsuario.ALUNO),
                        Usuario(nome = "Maria Oliveira", email = "maria@aluno.ifsp.edu.br", senha = "123", perfil = PerfilUsuario.ALUNO),
                        Usuario(nome = "Lucas Silva (IF)", email = "lucas.silva@aluno.if.edu.br", senha = "123", perfil = PerfilUsuario.ALUNO),
                        Usuario(nome = "Maria Oliveira (IF)", email = "maria@aluno.if.edu.br", senha = "123", perfil = PerfilUsuario.ALUNO),

                        // Professores
                        Usuario(nome = "Profª. Ana Beatriz", email = "ana.beatriz@ifsp.edu.br", senha = "123", perfil = PerfilUsuario.PROFESSOR),
                        Usuario(nome = "Profª. Ana Beatriz", email = "ana@ifsp.edu.br", senha = "123", perfil = PerfilUsuario.PROFESSOR),
                        Usuario(nome = "Profª. Ana Beatriz (IF)", email = "ana@if.edu.br", senha = "123", perfil = PerfilUsuario.PROFESSOR),
                        Usuario(nome = "Prof. Carlos Alberto", email = "carlos.alberto@ifsp.edu.br", senha = "123", perfil = PerfilUsuario.PROFESSOR),
                        Usuario(nome = "Prof. Carlos Alberto", email = "carlos@ifsp.edu.br", senha = "123", perfil = PerfilUsuario.PROFESSOR),
                        Usuario(nome = "Prof. Carlos Alberto (IF)", email = "carlos@if.edu.br", senha = "123", perfil = PerfilUsuario.PROFESSOR),

                        // CAE
                        Usuario(nome = "CAE - Apoio ao Estudante", email = "cae@ifsp.edu.br", senha = "123", perfil = PerfilUsuario.CAE),
                        Usuario(nome = "CAE - Apoio ao Estudante (IF)", email = "cae@if.edu.br", senha = "123", perfil = PerfilUsuario.CAE),

                        // Coordenacao
                        Usuario(nome = "Coordenação BCC", email = "coordenacao.bcc@ifsp.edu.br", senha = "123", perfil = PerfilUsuario.COORDENACAO),
                        Usuario(nome = "Coordenação BCC", email = "coordenacao@ifsp.edu.br", senha = "123", perfil = PerfilUsuario.COORDENACAO),
                        Usuario(nome = "Coordenação BCC (IF)", email = "coordenacao@if.edu.br", senha = "123", perfil = PerfilUsuario.COORDENACAO)
                    )
                    usuarioDao.insertAll(usuarios)
                }

                // 2. Cursos
                if (cursoDao.count() == 0) {
                    val cursos = listOf(
                        Curso(id = 1, nome = "Bacharelado em Ciência da Computação", sigla = "BCC"),
                        Curso(id = 2, nome = "Tecnologia em Análise e Desenv. de Sistemas", sigla = "ADS"),
                        Curso(id = 3, nome = "Técnico em Informática Integrado", sigla = "TII")
                    )
                    cursoDao.insertAll(cursos)
                }

                // 3. Turmas
                if (turmaDao.count() == 0) {
                    val turmas = listOf(
                        Turma(id = 1, cursoId = 1, nome = "BCC 1º sem"),
                        Turma(id = 2, cursoId = 1, nome = "BCC 3º sem"),
                        Turma(id = 3, cursoId = 1, nome = "BCC 5º sem"),
                        Turma(id = 4, cursoId = 2, nome = "ADS 1º sem"),
                        Turma(id = 5, cursoId = 2, nome = "ADS 3º sem"),
                        Turma(id = 6, cursoId = 3, nome = "Técnico 1º ano"),
                        Turma(id = 7, cursoId = 3, nome = "Técnico 2º ano")
                    )
                    turmaDao.insertAll(turmas)
                }

                // 4. Avisos de demonstracao dinamicos para "Hoje" e "Amanha" (HU02 Caso 01)
                val hoje = DateUtils.todayIso()
                val amanha = DateUtils.tomorrowIso()
                val seteDias = DateUtils.daysFromTodayIso(7)

                val avisosExistentes = avisoDao.getAllList()
                val temAvisosHoje = avisosExistentes.any { it.data == hoje }

                // Se nao ha avisos cadastrados para a data atual, insere os 4 avisos especificos de teste da HU02
                if (!temAvisosHoje) {
                    val profAna = usuarioDao.findByEmail("ana.beatriz@ifsp.edu.br")
                        ?: usuarioDao.findByEmail("ana@ifsp.edu.br")
                    val profCarlos = usuarioDao.findByEmail("carlos.alberto@ifsp.edu.br")
                        ?: usuarioDao.findByEmail("carlos@ifsp.edu.br")
                    val caeUser = usuarioDao.findByEmail("cae@ifsp.edu.br")

                    val avisosSeed = listOf(
                        // HU02 Caso 01 - Aviso 1 de hoje (13:30): Aula cancelada BCC 3º sem
                        Aviso(
                            tipo = TipoAviso.AULA_CANCELADA,
                            turmaId = 2,
                            turmaNome = "BCC 3º sem",
                            data = hoje,
                            horaInicio = "13:30",
                            horaFim = "15:10",
                            mensagem = "Não haverá aula de Engenharia de Software devido a reunião pedagógica extraordinária.",
                            reposicaoData = seteDias,
                            reposicaoHora = "13:30",
                            autorId = profAna?.id ?: 5,
                            autorNome = profAna?.nome ?: "Profª. Ana Beatriz",
                            isTardio = true // RN06
                        ),
                        // HU02 Caso 01 - Aviso 2 de hoje (19:00): Aula cancelada ADS 1º sem
                        Aviso(
                            tipo = TipoAviso.AULA_CANCELADA,
                            turmaId = 4,
                            turmaNome = "ADS 1º sem",
                            data = hoje,
                            horaInicio = "19:00",
                            horaFim = "20:40",
                            mensagem = "Prof. Carlos em banca de TCC.",
                            autorId = profCarlos?.id ?: 8,
                            autorNome = profCarlos?.nome ?: "Prof. Carlos Alberto",
                            isTardio = false
                        ),
                        // HU02 Caso 01 - Aviso 3 de hoje (20:50): Outro aviso BCC 1º sem
                        Aviso(
                            tipo = TipoAviso.OUTRO,
                            turmaId = 1,
                            turmaNome = "BCC 1º sem",
                            data = hoje,
                            horaInicio = "20:50",
                            horaFim = "22:30",
                            mensagem = "Acolhimento aos ingressantes no auditório com a equipe do CAE.",
                            autorId = caeUser?.id ?: 11,
                            autorNome = caeUser?.nome ?: "CAE - Apoio ao Estudante",
                            isTardio = false
                        ),
                        // HU02 Caso 01 & Caso 04 - Aviso para amanha (19:00): Troca de aula ADS 1º sem
                        Aviso(
                            tipo = TipoAviso.TROCA_DE_AULA,
                            turmaId = 4,
                            turmaNome = "ADS 1º sem",
                            data = amanha,
                            horaInicio = "19:00",
                            horaFim = "20:40",
                            mensagem = "Troca de aula: Banco de Dados no lugar de Algoritmos.",
                            substituto = "Banco de Dados - Prof. Carlos, Lab 2",
                            autorId = profCarlos?.id ?: 8,
                            autorNome = profCarlos?.nome ?: "Prof. Carlos Alberto",
                            isTardio = false
                        )
                    )
                    avisoDao.insertAll(avisosSeed)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
