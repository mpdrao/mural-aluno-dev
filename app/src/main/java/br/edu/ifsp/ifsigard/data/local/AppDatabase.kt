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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Database(
    entities = [Usuario::class, Curso::class, Turma::class, Aviso::class],
    version = 1,
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
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val usuarioDao = database.usuarioDao()
            val cursoDao = database.cursoDao()
            val turmaDao = database.turmaDao()
            val avisoDao = database.avisoDao()

            if (usuarioDao.count() == 0) {
                val usuarios = listOf(
                    Usuario(id = 1, nome = "Lucas Silva", email = "lucas.silva@aluno.ifsp.edu.br", senha = "123", perfil = PerfilUsuario.ALUNO),
                    Usuario(id = 2, nome = "Prof. Carlos Alberto", email = "carlos.alberto@ifsp.edu.br", senha = "123", perfil = PerfilUsuario.PROFESSOR),
                    Usuario(id = 3, nome = "Profª. Ana Beatriz", email = "ana.beatriz@ifsp.edu.br", senha = "123", perfil = PerfilUsuario.PROFESSOR),
                    Usuario(id = 4, nome = "CAE - Apoio ao Estudante", email = "cae@ifsp.edu.br", senha = "123", perfil = PerfilUsuario.CAE),
                    Usuario(id = 5, nome = "Coordenação BCC", email = "coordenacao.bcc@ifsp.edu.br", senha = "123", perfil = PerfilUsuario.COORDENACAO)
                )
                usuarioDao.insertAll(usuarios)
            }

            if (cursoDao.count() == 0) {
                val cursos = listOf(
                    Curso(id = 1, nome = "Bacharelado em Ciência da Computação", sigla = "BCC"),
                    Curso(id = 2, nome = "Tecnologia em Análise e Desenv. de Sistemas", sigla = "ADS"),
                    Curso(id = 3, nome = "Técnico em Informática Integrado", sigla = "TII")
                )
                cursoDao.insertAll(cursos)
            }

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

            if (avisoDao.count() == 0) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = sdf.format(Date())

                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, 7)
                val repoDateStr = sdf.format(cal.time)

                val avisos = listOf(
                    Aviso(
                        id = 1,
                        tipo = TipoAviso.AULA_CANCELADA,
                        turmaId = 2,
                        turmaNome = "BCC 3º sem",
                        data = todayStr,
                        horaInicio = "13:30",
                        horaFim = "15:10",
                        mensagem = "Não haverá aula de Engenharia de Software devido a reunião pedagógica extraordinária.",
                        reposicaoData = repoDateStr,
                        reposicaoHora = "13:30",
                        autorId = 3,
                        autorNome = "Profª. Ana Beatriz",
                        isTardio = true
                    ),
                    Aviso(
                        id = 2,
                        tipo = TipoAviso.TROCA_DE_AULA,
                        turmaId = 4,
                        turmaNome = "ADS 1º sem",
                        data = todayStr,
                        horaInicio = "15:30",
                        horaFim = "17:10",
                        mensagem = "Troca de aula de Algoritmos por Banco de Dados no Laboratório 2.",
                        substituto = "Banco de Dados com Prof. Carlos, Lab 2",
                        autorId = 2,
                        autorNome = "Prof. Carlos Alberto",
                        isTardio = false
                    ),
                    Aviso(
                        id = 3,
                        tipo = TipoAviso.OUTRO,
                        turmaId = 1,
                        turmaNome = "BCC 1º sem",
                        data = todayStr,
                        horaInicio = "19:00",
                        horaFim = "20:30",
                        mensagem = "Acolhimento aos ingressantes no auditório principal com palestra do CAE.",
                        autorId = 4,
                        autorNome = "CAE - Apoio ao Estudante",
                        isTardio = false
                    )
                )
                avisoDao.insertAll(avisos)
            }
        }
    }
}
