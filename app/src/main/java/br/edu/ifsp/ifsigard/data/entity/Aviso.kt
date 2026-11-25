package br.edu.ifsp.ifsigard.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TipoAviso(val label: String) {
    AULA_CANCELADA("Aula cancelada"),
    TROCA_DE_AULA("Troca de aula"),
    OUTRO("Outro aviso")
}

@Entity(tableName = "avisos")
data class Aviso(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tipo: TipoAviso,
    val turmaId: Long,
    val turmaNome: String,
    val data: String, // formato YYYY-MM-DD
    val horaInicio: String, // formato HH:mm
    val horaFim: String, // formato HH:mm
    val mensagem: String, // max 200 chars
    val substituto: String? = null,
    val reposicaoData: String? = null,
    val reposicaoHora: String? = null,
    val autorId: Long,
    val autorNome: String,
    val publicadoEm: Long = System.currentTimeMillis(),
    val isTardio: Boolean = false
)
