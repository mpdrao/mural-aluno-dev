package br.edu.ifsp.ifsigard.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "turmas",
    foreignKeys = [
        ForeignKey(
            entity = Curso::class,
            parentColumns = ["id"],
            childColumns = ["cursoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cursoId"])]
)
data class Turma(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cursoId: Long,
    val nome: String
)
