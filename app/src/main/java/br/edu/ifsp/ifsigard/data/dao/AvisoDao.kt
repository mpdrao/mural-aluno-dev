package br.edu.ifsp.ifsigard.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.edu.ifsp.ifsigard.data.entity.Aviso
import kotlinx.coroutines.flow.Flow

@Dao
interface AvisoDao {
    @Query("SELECT * FROM avisos ORDER BY data ASC, horaInicio ASC")
    fun getAllFlow(): Flow<List<Aviso>>

    @Query("SELECT * FROM avisos ORDER BY data ASC, horaInicio ASC")
    suspend fun getAllList(): List<Aviso>

    @Query("SELECT * FROM avisos WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): Aviso?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(aviso: Aviso): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(avisos: List<Aviso>)

    @Update
    suspend fun update(aviso: Aviso)

    @Delete
    suspend fun delete(aviso: Aviso)

    @Query("SELECT COUNT(*) FROM avisos")
    suspend fun count(): Int
}
