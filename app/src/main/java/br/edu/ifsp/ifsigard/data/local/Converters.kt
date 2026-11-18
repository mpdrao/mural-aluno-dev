package br.edu.ifsp.ifsigard.data.local

import androidx.room.TypeConverter
import br.edu.ifsp.ifsigard.data.entity.PerfilUsuario
import br.edu.ifsp.ifsigard.data.entity.TipoAviso

class Converters {
    @TypeConverter
    fun fromPerfilUsuario(perfil: PerfilUsuario?): String? = perfil?.name

    @TypeConverter
    fun toPerfilUsuario(value: String?): PerfilUsuario? =
        value?.let { enumValueOf<PerfilUsuario>(it) }

    @TypeConverter
    fun fromTipoAviso(tipo: TipoAviso?): String? = tipo?.name

    @TypeConverter
    fun toTipoAviso(value: String?): TipoAviso? =
        value?.let { enumValueOf<TipoAviso>(it) }
}
