package br.edu.ifsp.ifsigard.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cursos")
data class Curso(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val sigla: String
)
