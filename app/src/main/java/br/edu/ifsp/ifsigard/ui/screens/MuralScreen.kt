package br.edu.ifsp.ifsigard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.ifsp.ifsigard.data.entity.Aviso
import br.edu.ifsp.ifsigard.data.entity.Usuario
import br.edu.ifsp.ifsigard.ui.components.AvisoCard
import br.edu.ifsp.ifsigard.ui.theme.IfGreen
import br.edu.ifsp.ifsigard.ui.theme.IfGreenDark
import br.edu.ifsp.ifsigard.ui.theme.IfGreenLight
import br.edu.ifsp.ifsigard.ui.theme.IfRed
import br.edu.ifsp.ifsigard.ui.theme.TextPrimary
import br.edu.ifsp.ifsigard.ui.theme.TextSecondary
import br.edu.ifsp.ifsigard.ui.viewmodel.FiltroData
import br.edu.ifsp.ifsigard.ui.viewmodel.FiltroTurno
import br.edu.ifsp.ifsigard.ui.viewmodel.MuralViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuralScreen(
    viewModel: MuralViewModel,
    usuarioLogado: Usuario,
    onNovoAviso: () -> Unit,
    onEditarAviso: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val turmas by viewModel.turmas.collectAsState()
    val avisos = viewModel.avisosFiltrados()
    val snackbarHostState = remember { SnackbarHostState() }

    var turmaMenuExpanded by remember { mutableStateOf(false) }

    val dataFormatada = remember {
        val sdf = SimpleDateFormat("EEE, dd/MM", Locale("pt", "BR"))
        sdf.format(Date())
    }

    LaunchedEffect(viewModel.feedbackMensagem) {
        viewModel.feedbackMensagem?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.limparFeedback()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Mural da Turma",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "${usuarioLogado.nome} (${usuarioLogado.perfil.name}) · $dataFormatada",
                            fontSize = 12.sp,
                            color = IfGreenLight
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sair",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IfGreen,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            // RN01 / RN02: FAB visivel apenas para servidores
            if (usuarioLogado.isServidor()) {
                ExtendedFloatingActionButton(
                    onClick = onNovoAviso,
                    containerColor = IfGreen,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = "Novo aviso") },
                    text = { Text("Novo aviso") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Secao de Filtros (RN08)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Filtro de Turma
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val turmaSelecionadaNome = turmas.find { it.id == viewModel.filtroTurmaId }?.nome ?: "Todas as turmas"

                    Box {
                        OutlinedButton(
                            onClick = { turmaMenuExpanded = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp), tint = IfGreen)
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(text = "Turma: $turmaSelecionadaNome", color = TextPrimary, fontSize = 13.sp)
                        }

                        DropdownMenu(
                            expanded = turmaMenuExpanded,
                            onDismissRequest = { turmaMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todas as turmas") },
                                onClick = {
                                    viewModel.setTurmaFiltro(null)
                                    turmaMenuExpanded = false
                                }
                            )
                            turmas.forEach { turma ->
                                DropdownMenuItem(
                                    text = { Text(turma.nome) },
                                    onClick = {
                                        viewModel.setTurmaFiltro(turma.id)
                                        turmaMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (viewModel.filtroTurmaId != null) {
                        TextButton(onClick = { viewModel.setTurmaFiltro(null) }) {
                            Text("Limpar", fontSize = 12.sp, color = IfGreen)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filtro por Data (RN08: padrao "Hoje")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FiltroData.values().forEach { opcao ->
                        FilterChip(
                            selected = viewModel.filtroData == opcao,
                            onClick = { viewModel.setDataFiltro(opcao) },
                            label = { Text(opcao.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IfGreenLight,
                                selectedLabelColor = IfGreenDark
                            )
                        )
                    }

                    Spacer(modifier = Modifier.size(8.dp))

                    // Filtro por Turno
                    FiltroTurno.values().forEach { turno ->
                        if (turno != FiltroTurno.TODOS || viewModel.filtroTurno != FiltroTurno.TODOS) {
                            FilterChip(
                                selected = viewModel.filtroTurno == turno,
                                onClick = { viewModel.setTurnoFiltro(turno) },
                                label = { Text(turno.label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IfGreenLight,
                                    selectedLabelColor = IfGreenDark
                                )
                            )
                        }
                    }
                }
            }

            // Lista de Avisos
            if (avisos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Nenhum aviso encontrado",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Não há avisos cadastrados para os filtros selecionados.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(avisos, key = { it.id }) { aviso ->
                        AvisoCard(
                            aviso = aviso,
                            isAutor = usuarioLogado.id == aviso.autorId, // RN07
                            onEditar = { onEditarAviso(aviso.id) },
                            onExcluir = { viewModel.solicitarExclusao(aviso) }
                        )
                    }
                }
            }
        }

        // Dialog de Confirmacao de Exclusao (RN07)
        if (viewModel.avisoParaExcluir != null) {
            val aviso = viewModel.avisoParaExcluir!!
            AlertDialog(
                onDismissRequest = { viewModel.cancelarExclusao() },
                title = { Text(text = "Confirmar exclusão") },
                text = {
                    Text(
                        text = "Deseja realmente excluir o aviso de ${aviso.tipo.label} da turma ${aviso.turmaNome} (${aviso.data} das ${aviso.horaInicio} às ${aviso.horaFim})?"
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.confirmarExclusao(usuarioLogado) }
                    ) {
                        Text(text = "Excluir", color = IfRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelarExclusao() }) {
                        Text(text = "Cancelar", color = TextSecondary)
                    }
                }
            )
        }
    }
}
