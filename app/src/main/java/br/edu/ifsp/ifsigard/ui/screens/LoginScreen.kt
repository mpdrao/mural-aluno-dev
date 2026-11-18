package br.edu.ifsp.ifsigard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.ifsp.ifsigard.data.entity.Usuario
import br.edu.ifsp.ifsigard.ui.theme.IfGrayBorder
import br.edu.ifsp.ifsigard.ui.theme.IfGrayLight
import br.edu.ifsp.ifsigard.ui.theme.IfGreen
import br.edu.ifsp.ifsigard.ui.theme.IfGreenDark
import br.edu.ifsp.ifsigard.ui.theme.IfGreenLight
import br.edu.ifsp.ifsigard.ui.theme.IfRed
import br.edu.ifsp.ifsigard.ui.theme.TextPrimary
import br.edu.ifsp.ifsigard.ui.theme.TextSecondary
import br.edu.ifsp.ifsigard.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (Usuario) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Cabecalho Institucional
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(IfGreen)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "IFSP · IFSIGARD",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Mural da Turma",
                style = MaterialTheme.typography.titleLarge,
                color = IfGreenDark,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Avisos de ausência e troca de aulas",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Campo E-mail Institucional (RN01)
            OutlinedTextField(
                value = viewModel.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("E-mail institucional") },
                placeholder = { Text("nome@aluno.ifsp.edu.br ou nome@ifsp.edu.br") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = IfGreen)
                },
                isError = viewModel.errorMessage != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Campo Senha
            OutlinedTextField(
                value = viewModel.senha,
                onValueChange = { viewModel.onSenhaChange(it) },
                label = { Text("Senha") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = IfGreen)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Ocultar senha" else "Mostrar senha"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = viewModel.errorMessage != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            // Feedback de Erro (RN01)
            if (viewModel.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = viewModel.errorMessage ?: "",
                    color = IfRed,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botao Entrar
            Button(
                onClick = {
                    viewModel.login(onSuccess = onLoginSuccess)
                },
                enabled = !viewModel.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IfGreen)
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.height(24.dp).width(24.dp)
                    )
                } else {
                    Text(text = "Entrar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Card informativo "Quem e quem" com preenchimento rapido
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(IfGrayLight)
                    .border(1.dp, IfGrayBorder, RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "Usuários para demonstração (Seed):",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.preencherCredenciais("lucas.silva@aluno.ifsp.edu.br") },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(text = "Aluno", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.preencherCredenciais("ana.beatriz@ifsp.edu.br") },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(text = "Profª. Ana", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.preencherCredenciais("carlos.alberto@ifsp.edu.br") },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(text = "Prof. Carlos", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.preencherCredenciais("cae@ifsp.edu.br") },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(text = "CAE", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.preencherCredenciais("coordenacao.bcc@ifsp.edu.br") },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(text = "Coordenação", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
