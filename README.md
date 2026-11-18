# IFSIGARD - Mural da Turma (mural-aluno-dev)

Implementação do sistema **IFSIGARD - Mural da Turma (avisos de ausência e troca de aula)** para o Instituto Federal de Educação, Ciência e Tecnologia de São Paulo (IFSP), conforme especificado nas pastas `1.Requisitos` e `2.Analise e Design`.

---

## 1. Tecnologias Utilizadas

- **Linguagem**: Kotlin (JVM 17 / 21)
- **UI Toolkit**: Jetpack Compose com Material Design 3
- **Persistência Local**: Room Database (SQLite)
- **Arquitetura**: MVVM (`Composables` -> `ViewModel` -> `Repository` -> `Room DAO` -> SQLite)
- **Gerenciamento de Estado**: `mutableStateOf`, `StateFlow`, `collectAsState`
- **Navegação**: Navigation Compose (`NavHost`, `composable`)

---

## 2. Estrutura do Projeto

```text
app/src/main/
├── AndroidManifest.xml
├── java/br/edu/ifsp/ifsigard/
│   ├── MainActivity.kt                      # Ponto de entrada e injecao de dependencias
│   ├── data/
│   │   ├── entity/
│   │   │   ├── Usuario.kt                   # Entidade Usuario (perfis ALUNO, PROFESSOR, CAE, COORDENACAO)
│   │   │   ├── Curso.kt                     # Entidade Curso (BCC, ADS, Tecnico)
│   │   │   ├── Turma.kt                     # Entidade Turma vinculada ao Curso
│   │   │   └── Aviso.kt                     # Entidade Aviso (tipos, horarios, tardio, reposicao)
│   │   ├── dao/
│   │   │   ├── UsuarioDao.kt                # Consultas e insercoes de usuarios
│   │   │   ├── CursoDao.kt                  # Consultas de cursos
│   │   │   ├── TurmaDao.kt                  # Consultas de turmas
│   │   │   └── AvisoDao.kt                  # CRUD completo de avisos ordenados
│   │   ├── local/
│   │   │   ├── Converters.kt                # TypeConverters para Enums
│   │   │   └── AppDatabase.kt               # Banco Room com carga automatica de dados iniciais (Seed)
│   │   └── repository/
│   │       └── MuralRepository.kt           # Centralizacao das regras de negocio e operacoes de dados
│   ├── ui/
│   │   ├── theme/
│   │   │   ├── Color.kt                     # Paleta oficial IFSP (Verde IF, Vermelho, Ambar, Cinza)
│   │   │   ├── Type.kt                      # Tipografia do sistema
│   │   │   └── Theme.kt                     # Tema Material 3
│   │   ├── components/
│   │   │   └── AvisoCard.kt                 # Card do aviso com chips de status, badges e acoes
│   │   ├── screens/
│   │   │   ├── LoginScreen.kt               # Tela T1: Autenticacao com validacao de dominio
│   │   │   ├── MuralScreen.kt               # Tela T2: Mural com filtros de Turma, Data e Turno
│   │   │   └── AvisoFormScreen.kt           # Tela T3: Cadastro e edicao de aviso com validacoes
│   │   ├── viewmodel/
│   │   │   ├── AuthViewModel.kt             # Gestao do estado de autenticacao e sessao
│   │   │   ├── MuralViewModel.kt            # Gestao dos filtros e dialog de exclusao
│   │   │   └── AvisoFormViewModel.kt        # Validacoes de formulario e persistencia
│   │   └── navigation/
│   │       └── NavGraph.kt                  # Rotas de navegacao (login, mural, aviso_form)
└── res/
    ├── values/
    │   ├── colors.xml
    │   ├── strings.xml
    │   └── themes.xml
app/src/test/
└── java/br/edu/ifsp/ifsigard/
    └── BusinessRulesTest.kt                 # Testes unitarios cobrindo RN01 a RN09
```

---

## 3. Conformidade com as Regras de Negócio

| Regra | Descrição | Implementação |
|---|---|---|
| **RN01 (Login)** | E-mail institucional obrigatório. Alunos possuem acesso apenas de consulta ao mural. | Validado em `MuralRepository.login` e no `AuthViewModel`. Alunos não visualizam botão de novo aviso. |
| **RN02 (Publicação)** | Apenas perfis Servidor (Professor, CAE, Coordenação) podem publicar. | Verificação de `usuario.isServidor()` no FAB da `MuralScreen` e no `MuralRepository.salvarAviso`. |
| **RN03 (Obrigatoriedade)** | Tipo, turma, data, horários e mensagem (máx 200 caracteres) são obrigatórios. Se troca de aula, substituto obrigatório. | Validações de campo em `AvisoFormViewModel.validar()` e contador de caracteres na tela. |
| **RN04 (Data)** | Data do aviso não pode ser anterior à data atual. | Verificação `data >= hoje` no formulário e no repositório. |
| **RN05 (Horário)** | Horário de fim deve ser estritamente maior que o de início. | Verificação `horaFim > horaInicio` no formulário. |
| **RN06 (Aviso Tardio)** | Publicado com antecedência menor que 24 horas do início da aula recebe selo "Aviso tardio". | Cálculo automático em `MuralRepository.calcularAvisoTardio` e exibição de badge vermelho no card. |
| **RN07 (Autoria)** | Apenas o autor do aviso pode editá-lo ou excluí-lo (com diálogo de confirmação). | Botões "Editar" e "Excluir" exibidos condicionalmente apenas quando `usuarioLogado.id == aviso.autorId`. |
| **RN08 (Mural Padrão)** | Abre nos avisos de "Hoje", ordenados pelo horário de início. Filtros por Turma, Data e Turno. | Padrão `FiltroData.HOJE` no `MuralViewModel` com ordenação por `data` e `horaInicio`. |
| **RN09 (Reposição)** | Reposição é opcional. Se informada, data deve ser posterior à do aviso e dentro do período letivo (até 18/12/2026). | Validação no formulário e exibição de badge de reposição no card. |

---

## 4. Usuários Pré-Cadastrados (Seed para Testes)

Todos os usuários abaixo possuem a senha padrão `123`:

- **Aluno**: `lucas.silva@aluno.ifsp.edu.br` (perfil Aluno - apenas consulta)
- **Professor 1**: `ana.beatriz@ifsp.edu.br` (perfil Professor - cria e gerencia seus avisos)
- **Professor 2**: `carlos.alberto@ifsp.edu.br` (perfil Professor - cria e gerencia seus avisos)
- **CAE**: `cae@ifsp.edu.br` (perfil CAE - Apoio ao Estudante)
- **Coordenação**: `coordenacao.bcc@ifsp.edu.br` (perfil Coordenação BCC)

Na tela de Login, há um painel de atalhos rápidos ("Quem é quem") para preencher as credenciais de teste com um toque.

---

## 5. Como Executar o Projeto

1. Abra o **Android Studio** (Koala, Ladybug ou superior).
2. Selecione **Open** e navegue até a pasta `3.Implementacao/mural-aluno-dev`.
3. Aguarde o Gradle sincronizar as dependências.
4. Execute o aplicativo em um emulador Android (API 24+) ou dispositivo físico via USB.
5. Para rodar os testes unitários das regras de negócio:
   ```bash
   ./gradlew testDebugUnitTest
   ```
