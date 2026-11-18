package br.edu.ifsp.ifsigard.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.edu.ifsp.ifsigard.data.entity.Turma
import kotlinx.coroutines.flow.Flow

@Dao
interface TurmaDao {
    @Query("SELECT * FROM turmas ORDER BY nome ASC")
    fun getAll(): Flow<List<Turma>>

    @Query("SELECT * FROM turmas ORDER BY nome ASC")
    suspend fun getAllList(): List<Turma>

    @Query("SELECT * FROM turmas WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): Turma?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(turmas: List<Turma>)

    @Query("SELECT COUNT(*) FROM turmas")
    suspend fun count(): Int
}
