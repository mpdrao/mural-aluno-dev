package br.edu.ifsp.ifsigard.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.ifsp.ifsigard.data.entity.TipoAviso
import br.edu.ifsp.ifsigard.data.entity.Usuario
import br.edu.ifsp.ifsigard.ui.theme.IfGrayBorder
import br.edu.ifsp.ifsigard.ui.theme.IfGrayLight
import br.edu.ifsp.ifsigard.ui.theme.IfGreen
import br.edu.ifsp.ifsigard.ui.theme.IfRed
import br.edu.ifsp.ifsigard.ui.theme.TextPrimary
import br.edu.ifsp.ifsigard.ui.theme.TextSecondary
import br.edu.ifsp.ifsigard.ui.viewmodel.AvisoFormViewModel
import br.edu.ifsp.ifsigard.util.DateUtils
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvisoFormScreen(
    avisoId: Long,
    usuarioLogado: Usuario,
    viewModel: AvisoFormViewModel,
    onNavigateBack: () -> Unit,
    onSaveSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val turmas by viewModel.turmas.collectAsState()
    var turmaMenuExpanded by remember { mutableStateOf(false) }

    fun abrirSeletorHora(horaAtual: String, onHoraSelecionada: (String) -> Unit) {
        val cal = Calendar.getInstance()
        var h = 19
        var m = 0
        if (DateUtils.isValidTime(horaAtual)) {
            val parts = horaAtual.trim().split(":")
            h = parts[0].toIntOrNull() ?: 19
            m = parts[1].toIntOrNull() ?: 0
        }
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                onHoraSelecionada(String.format(Locale.ROOT, "%02d:%02d", hourOfDay, minute))
            },
            h,
            m,
            true
        ).show()
    }

    fun abrirSeletorData(dataAtual: String, onDataSelecionada: (String) -> Unit) {
        val cal = Calendar.getInstance()
        var ano = cal.get(Calendar.YEAR)
        var mes = cal.get(Calendar.MONTH)
        var dia = cal.get(Calendar.DAY_OF_MONTH)
        val iso = DateUtils.normalizeToIso(dataAtual)
        if (iso.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
            val parts = iso.split("-")
            ano = parts[0].toIntOrNull() ?: ano
            mes = (parts[1].toIntOrNull() ?: (mes + 1)) - 1
            dia = parts[2].toIntOrNull() ?: dia
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDataSelecionada(String.format(Locale.ROOT, "%04d-%02d-%02d", year, month + 1, dayOfMonth))
            },
            ano,
            mes,
            dia
        ).show()
    }

    LaunchedEffect(avisoId) {
        if (avisoId > 0) {
            viewModel.carregarAviso(avisoId)
        } else {
            viewModel.resetForm()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (viewModel.isEdicao) "Editar aviso" else "Novo aviso",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IfGreen)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 1. Tipo de Aviso (RN03)
            Text(
                text = "Tipo de aviso *",
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            Column {
                TipoAviso.values().forEach { tipo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onTipoChange(tipo) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = viewModel.tipo == tipo,
                            onClick = { viewModel.onTipoChange(tipo) },
                            colors = RadioButtonDefaults.colors(selectedColor = IfGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tipo.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Turma (RN03)
            Text(
                text = "Turma *",
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = viewModel.turmaNome.ifBlank { "Selecione uma turma" },
                    onValueChange = {},
                    readOnly = true,
                    isError = viewModel.erroTurma != null,
                    trailingIcon = {
                        IconButton(onClick = { turmaMenuExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Selecionar turma", tint = IfGreen)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { turmaMenuExpanded = true }
                )

                DropdownMenu(
                    expanded = turmaMenuExpanded,
                    onDismissRequest = { turmaMenuExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    turmas.forEach { turma ->
                        DropdownMenuItem(
                            text = { Text(turma.nome) },
                            onClick = {
                                viewModel.onTurmaSelected(turma)
                                turmaMenuExpanded = false
                            }
                        )
                    }
                }
            }
            if (viewModel.erroTurma != null) {
                Text(text = viewModel.erroTurma ?: "", color = IfRed, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Data do Aviso (RN04)
            Text(
                text = "Data da ocorrência *",
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = viewModel.data,
                onValueChange = { viewModel.onDataChange(it) },
                placeholder = { Text("Ex: 25/11/2026 ou 2026-11-25") },
                isError = viewModel.erroData != null,
                leadingIcon = {
                    IconButton(onClick = { abrirSeletorData(viewModel.data) { viewModel.onDataChange(it) } }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Selecionar data", tint = IfGreen)
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (viewModel.erroData != null) {
                Text(text = viewModel.erroData ?: "", color = IfRed, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Horarios Inicio e Fim (RN05)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Hora início *", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = viewModel.horaInicio,
                        onValueChange = { viewModel.onHoraInicioChange(it) },
                        placeholder = { Text("19:00") },
                        isError = viewModel.erroHoraInicio != null,
                        leadingIcon = {
                            IconButton(onClick = { abrirSeletorHora(viewModel.horaInicio) { viewModel.onHoraInicioChange(it) } }) {
                                Icon(Icons.Default.Schedule, contentDescription = "Selecionar hora início", tint = IfGreen)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (viewModel.erroHoraInicio != null) {
                        Text(text = viewModel.erroHoraInicio ?: "", color = IfRed, fontSize = 12.sp)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Hora término *", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = viewModel.horaFim,
                        onValueChange = { viewModel.onHoraFimChange(it) },
                        placeholder = { Text("20:40") },
                        isError = viewModel.erroHoraFim != null,
                        leadingIcon = {
                            IconButton(onClick = { abrirSeletorHora(viewModel.horaFim) { viewModel.onHoraFimChange(it) } }) {
                                Icon(Icons.Default.Schedule, contentDescription = "Selecionar hora término", tint = IfGreen)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (viewModel.erroHoraFim != null) {
                        Text(text = viewModel.erroHoraFim ?: "", color = IfRed, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Mensagem do Aviso (RN03 - max 200 caracteres)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Mensagem *", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    text = "${viewModel.mensagem.length}/200",
                    fontSize = 12.sp,
                    color = if (viewModel.mensagem.length >= 190) IfRed else TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = viewModel.mensagem,
                onValueChange = { viewModel.onMensagemChange(it) },
                placeholder = { Text("Descreva o motivo da ausência ou informações da aula...") },
                isError = viewModel.erroMensagem != null,
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )
            if (viewModel.erroMensagem != null) {
                Text(text = viewModel.erroMensagem ?: "", color = IfRed, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
            }

            // 6. Substituto (Obrigatorio se TROCA_DE_AULA - RN03)
            if (viewModel.tipo == TipoAviso.TROCA_DE_AULA) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Docente / Disciplina substituta *",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = viewModel.substituto,
                    onValueChange = { viewModel.onSubstitutoChange(it) },
                    placeholder = { Text("Ex: Banco de Dados - Prof. Carlos, Lab 2") },
                    isError = viewModel.erroSubstituto != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (viewModel.erroSubstituto != null) {
                    Text(text = viewModel.erroSubstituto ?: "", color = IfRed, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 7. Reposicao de Aula (Opcional - RN09)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(IfGrayLight)
                    .border(1.dp, IfGrayBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onTemReposicaoChange(!viewModel.temReposicao) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = viewModel.temReposicao,
                            onCheckedChange = { viewModel.onTemReposicaoChange(it) },
                            colors = CheckboxDefaults.colors(checkedColor = IfGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Agendar reposição de aula (opcional)",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    if (viewModel.temReposicao) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Data reposição *", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(2.dp))
                                OutlinedTextField(
                                    value = viewModel.reposicaoData,
                                    onValueChange = { viewModel.onReposicaoDataChange(it) },
                                    placeholder = { Text("Ex: 28/11/2026") },
                                    trailingIcon = {
                                        IconButton(onClick = { abrirSeletorData(viewModel.reposicaoData) { viewModel.onReposicaoDataChange(it) } }) {
                                            Icon(Icons.Default.DateRange, contentDescription = "Selecionar data", tint = IfGreen)
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Horário", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(2.dp))
                                OutlinedTextField(
                                    value = viewModel.reposicaoHora,
                                    onValueChange = { viewModel.onReposicaoHoraChange(it) },
                                    placeholder = { Text("13:30") },
                                    trailingIcon = {
                                        IconButton(onClick = { abrirSeletorHora(viewModel.reposicaoHora) { viewModel.onReposicaoHoraChange(it) } }) {
                                            Icon(Icons.Default.Schedule, contentDescription = "Selecionar horário", tint = IfGreen)
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        if (viewModel.erroReposicao != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = viewModel.erroReposicao ?: "", color = IfRed, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Erro Geral
            if (viewModel.erroGeral != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = viewModel.erroGeral ?: "", color = IfRed, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botao Publicar / Salvar (HU03)
            Button(
                onClick = {
                    viewModel.salvar(usuarioLogado, onSuccess = onSaveSuccess)
                },
                enabled = !viewModel.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IfGreen)
            ) {
                if (viewModel.isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.height(24.dp).width(24.dp)
                    )
                } else {
                    Text(
                        text = if (viewModel.isEdicao) "Salvar alterações" else "Publicar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
