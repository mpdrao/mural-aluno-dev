package br.edu.ifsp.ifsigard.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PerfilUsuario {
    ALUNO,
    PROFESSOR,
    CAE,
    COORDENACAO
}

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val email: String,
    val senha: String,
    val perfil: PerfilUsuario
) {
    fun isServidor(): Boolean = perfil != PerfilUsuario.ALUNO
}
