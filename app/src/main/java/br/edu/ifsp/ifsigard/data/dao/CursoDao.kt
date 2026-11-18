package br.edu.ifsp.ifsigard.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.edu.ifsp.ifsigard.data.entity.Curso
import kotlinx.coroutines.flow.Flow

@Dao
interface CursoDao {
    @Query("SELECT * FROM cursos ORDER BY nome ASC")
    fun getAll(): Flow<List<Curso>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cursos: List<Curso>)

    @Query("SELECT COUNT(*) FROM cursos")
    suspend fun count(): Int
}
