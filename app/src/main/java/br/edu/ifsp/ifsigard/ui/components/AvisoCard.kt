package br.edu.ifsp.ifsigard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.ifsp.ifsigard.data.entity.Aviso
import br.edu.ifsp.ifsigard.data.entity.TipoAviso
import br.edu.ifsp.ifsigard.ui.theme.IfAmber
import br.edu.ifsp.ifsigard.ui.theme.IfAmberLight
import br.edu.ifsp.ifsigard.ui.theme.IfGray
import br.edu.ifsp.ifsigard.ui.theme.IfGrayBorder
import br.edu.ifsp.ifsigard.ui.theme.IfGrayLight
import br.edu.ifsp.ifsigard.ui.theme.IfGreen
import br.edu.ifsp.ifsigard.ui.theme.IfGreenLight
import br.edu.ifsp.ifsigard.ui.theme.IfRed
import br.edu.ifsp.ifsigard.ui.theme.IfRedLight
import br.edu.ifsp.ifsigard.ui.theme.TextPrimary
import br.edu.ifsp.ifsigard.ui.theme.TextSecondary

@Composable
fun AvisoCard(
    aviso: Aviso,
    isAutor: Boolean,
    onEditar: () -> Unit,
    onExcluir: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Linha superior: Turma + Chip do Tipo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = aviso.turmaNome,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                TipoChip(tipo = aviso.tipo)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Linha de data, horario e autor (HU02 Caso 02)
            val dataExibicao = br.edu.ifsp.ifsigard.util.DateUtils.formatForDisplay(aviso.data)
            Text(
                text = "$dataExibicao · ${aviso.horaInicio}–${aviso.horaFim} · por ${aviso.autorNome}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mensagem do aviso
            Text(
                text = aviso.mensagem,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )

            // Detalhe de troca se houver
            if (aviso.tipo == TipoAviso.TROCA_DE_AULA && !aviso.substituto.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(IfAmberLight)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Substituto: ${aviso.substituto}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IfAmber,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Badges: Aviso tardio (RN06) e Reposicao (RN09)
            if (aviso.isTardio || !aviso.reposicaoData.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (aviso.isTardio) {
                        BadgeTardio()
                    }
                    if (!aviso.reposicaoData.isNullOrBlank()) {
                        BadgeReposicao(
                            data = aviso.reposicaoData,
                            hora = aviso.reposicaoHora
                        )
                    }
                }
            }

            // Acoes do autor (RN07)
            if (isAutor) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onEditar,
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = IfGreen)
                    ) {
                        Text(text = "Editar", fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = onExcluir,
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = IfRed)
                    ) {
                        Text(text = "Excluir", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TipoChip(tipo: TipoAviso) {
    val (bgColor, textColor) = when (tipo) {
        TipoAviso.AULA_CANCELADA -> IfRedLight to IfRed
        TipoAviso.TROCA_DE_AULA -> IfAmberLight to IfAmber
        TipoAviso.OUTRO -> IfGrayLight to IfGray
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = tipo.label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun BadgeTardio() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(IfRed)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "Aviso tardio",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BadgeReposicao(data: String, hora: String?) {
    val horaTexto = if (!hora.isNullOrBlank()) " às $hora" else ""
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(IfGreenLight)
            .border(1.dp, IfGreen, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "Reposição: $data$horaTexto",
            color = IfGreen,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
