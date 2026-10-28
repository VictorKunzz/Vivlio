# Documento de Especificação de Requisitos e DoR

> Revisão de outubro/2026 (equipe 2), por Luis Fernando, atualizada para a **v2.0 (N2)**. A versão
> original da equipe anterior continua neste diretório (`.docx`). Alterações: nomes de tabelas
> alinhados ao banco real, situação dos protótipos registrada, validações e situação de cada tarefa
> detalhadas. O modelo de dados está no [DER](../DER/README.md).
>
> O texto dos requisitos e critérios de aceitação é responsabilidade da PO e não foi alterado. Onde a
> implementação diverge, a linha **Situação na v2.0** explica.

## Checklist Padrão de DoR (Aplicado a todos os cards)

*Antes de qualquer tarefa ir para a coluna "To Do" dos desenvolvedores, ela deve conter:*

- [x] **História de Usuário (US)** no formato padrão.
- [x] **Critérios de Aceitação e Exceções** definidos.
- [x] **Regras de Negócio (RN)** vinculadas.
- [x] **Link do Protótipo (Figma).**
- [x] **Mapeamento de Entidades do Banco de Dados (PostgreSQL).**
- [x] **Requisito Funcional (RF) mapeado:** A tarefa está vinculada a uma funcionalidade listada no escopo.
- [x] **Requisito Não Funcional (RNF) mapeado:** As restrições técnicas (desempenho, segurança, persistência) para esta tarefa foram documentadas.

## Detalhamento das Tarefas (Backlog Refinado)

### Tarefa 1: Cadastro de Doação de Livro (Ganhar Crédito)

**História de Usuário (US_01):**

> *"Como **usuário cadastrado**, eu quero **cadastrar um livro meu para doação no acervo**, para que **eu possa ganhar 1 crédito no sistema.**"*

- **Critérios de Aceitação:**
  - **Cenário Feliz:** Ao preencher os dados do livro (título, autor, gênero) e confirmar, o sistema salva o livro com o status "Disponível". Imediatamente, o saldo do usuário logado é incrementado em +1. Uma mensagem de sucesso é exibida: *"Livro cadastrado! Você ganhou 1 crédito."*
  - **Cenário de Erro (Exceção):** Se o usuário tentar enviar o formulário sem os campos obrigatórios, o sistema bloqueia a ação e destaca os campos em vermelho.
- **Regra de Negócio (RN):** Doar 1 livro = ganha 1 crédito (Proporção estrita de 1:1).
- **Validações implementadas:** título, autor e gênero são obrigatórios (espaços nas pontas são removidos); sem eles a API responde 400 e nenhum crédito é gerado. O gênero é texto livre: o backend não valida contra a lista exibida no formulário.
- **Situação na v2.0:** implementado conforme o critério. A tela exibe *"Livro cadastrado! Você ganhou 1 crédito."* seguida da nota do estado e destaca em vermelho todos os campos inválidos (PR #15). Desde a N2 a doação também exige o questionário de avaliação (Tarefa 4).
- **Protótipo / Interface:** `[Link Figma - Tela de Nova Doação]` — link não fornecido pela equipe anterior; protótipo pendente.
- **Mapeamento de Banco de Dados:**
  - Inserir em `livros` (status: `'DISPONIVEL'`).
  - Atualizar `usuarios` (`saldo_creditos`: +1).
  - Inserir em `transacoes_credito` (tipo: `'ENTRADA'`, descricao: `'Doação de livro'`).

### Tarefa 2: Solicitação de Troca/Resgate de Livro (RN CRÍTICA)

**História de Usuário (US_02):**

> *"Como **usuário cadastrado**, eu quero **solicitar o resgate de um livro do acervo**, para que **eu possa adquirir uma nova leitura utilizando meu saldo.**"*

- **Critérios de Aceitação:**
  - **Cenário Feliz:** O usuário acessa a página de um livro "Disponível" e clica em "Resgatar (1 Crédito)". O sistema debita 1 crédito do saldo do usuário, altera o status do livro para "Resgatado" e vincula o livro ao usuário. Mensagem: *"Resgate confirmado! Combine a entrega na biblioteca."*
  - **Cenário de Erro (Exceção):** O usuário clica em "Resgatar" possuindo saldo de 0 créditos. O sistema bloqueia a requisição no frontend e no backend.
  - **Mensagem de Erro Esperada:** *"Você precisa de pelo menos 1 crédito ativo para resgatar um livro. Doe um livro para ganhar créditos."*
- **Regra de Negócio (RN): [RN CRÍTICA]** Um usuário só pode resgatar um livro se tiver pelo menos 1 crédito ativo na conta.
- **Validações implementadas:** livro não informado → 400; livro inexistente → 404; livro já resgatado → 409 *"Este livro não está mais disponível para resgate."*; saldo 0 → 409 com a mensagem de erro esperada. Usuário e livro são travados com lock pessimista durante o resgate (RNF01).
- **Situação na v2.0:** implementado no backend e no frontend (PR #15). O botão "Resgatar (1 Crédito)" do card abre um **modal** com os detalhes do livro (estado, sobrecapa, doador) e a confirmação; não há página de detalhes separada. Com saldo 0 o botão fica desabilitado, a mensagem de erro esperada aparece na tela e nenhuma chamada é feita à API. Depois da confirmação: *"Resgate confirmado! Combine a entrega na biblioteca."*
- **Protótipo / Interface:** `[Link Figma - Tela de Detalhes do Livro Modal de Confirmação]` — link não fornecido pela equipe anterior; protótipo pendente.
- **Mapeamento de Banco de Dados:**
  - Atualizar `livros` (status: `'RESGATADO'`, `id_usuario_recebedor`).
  - Atualizar `usuarios` (`saldo_creditos`: -1).
  - Inserir em `transacoes_credito` (tipo: `'SAIDA'`, descricao: `'Resgate de livro'`).

### Tarefa 3: Cadastro de Novo Usuário

**História de Usuário (US_03):**

> *"Como **visitante**, eu quero **criar uma conta na plataforma**, para que **eu possa começar a doar e trocar livros.**"*

- **Critérios de Aceitação:**
  - **Cenário Feliz:** O visitante preenche nome, e-mail e senha. O sistema cria a conta e inicia o saldo de créditos zerado (0).
  - **Cenário de Erro (Exceção):** Se o e-mail já estiver cadastrado, o sistema exibe: *"E-mail já cadastrado. Por favor, faça login."*
- **Regra de Negócio (RN):** Todo novo usuário inicia obrigatoriamente com saldo = 0 créditos.
- **Validações implementadas:** nome obrigatório; e-mail obrigatório, em formato válido e gravado em minúsculas; senha obrigatória com no mínimo 6 caracteres, salva com hash BCrypt. E-mail duplicado → 409.
- **Situação na v2.0:** implementado. Após o cadastro o usuário já fica autenticado (a API devolve o token JWT, válido por 2 horas). Campos vazios ou inválidos são destacados em vermelho no formulário.
- **Protótipo / Interface:** `[Link Figma - Tela de Registro]` — link não fornecido pela equipe anterior; protótipo pendente.
- **Mapeamento de Banco de Dados:**
  - Inserir em `usuarios` (`nome`, `email`, `senha_hash`, `saldo_creditos` = 0).

### Tarefa 4 (Evolução N2): Avaliação do Estado do Livro

**História de Usuário (US_04):**

> *"Como **usuário doador**, eu quero **avaliar o estado de conservação do livro de 1 a 5 estrelas no momento do cadastro**, para que **os interessados saibam a qualidade física da obra.**"*

- **Critérios de Aceitação:**
  - **Cenário Feliz:** No formulário de doação, o usuário seleciona de 1 a 5 estrelas. A avaliação é salva e passa a ser exibida publicamente no card do livro.
  - **Cenário de Erro (Exceção):** O usuário tenta inserir um valor fora do intervalo (ex: 0 ou 6 via manipulação de requisição). O backend (Spring Boot) rejeita com erro 400 Bad Request.
- **Regra de Negócio (RN):** A nota de conservação deve ser um número inteiro, mínimo de 1 e máximo de 5.
- **Situação na v2.0:** implementado de forma **diferente** do critério acima (PR #13), seguindo a ficha de avaliação definida pela equipe:
  - o doador **não escolhe a estrela**: responde 6 perguntas (capa, lombada, páginas, marcações, manchas, odor) e o servidor calcula a nota como a **menor** entre elas;
  - eliminatórios (página faltando, mofo, dano por água, infestação, texto ilegível, capa separada) recusam o livro com **422**, sem gerar crédito;
  - didático com exercícios respondidos fica com no máximo 2; sobrecapa ausente só é registrada;
  - código de opção inexistente é recusado com 400; a nota nunca vem do cliente;
  - a nota fica na coluna `nota_estado` (não `estado_conservacao`), criada pela migração Flyway `V2`.

  **Pendente com a PO (DT-07):** registrar a ficha como regra oficial deste RF e reescrever o critério de aceitação. Casos de teste em [`casos-de-teste-n2.md`](../testes/casos-de-teste-n2.md).
- **Protótipo / Interface:** `[Link Figma - Componente de Estrelas / Card do Livro]` — link não fornecido pela equipe anterior; protótipo pendente.
- **Mapeamento de Banco de Dados:**
  - Nova coluna ou tabela: atualizar a tabela `livros` adicionando coluna `estado_conservacao` (Integer 1-5).

## Requisitos Funcionais (RF) do Sebo Comunitário

| ID | Funcionalidade | Descrição | Escopo |
| :--- | :--- | :--- | :--- |
| **RF01** | Gestão de Contas | O sistema deve permitir o cadastro, login e gerenciamento de perfil dos usuários. | N1 |
| **RF02** | Cadastro de Doação | O sistema deve permitir que um usuário cadastre um livro informando título, autor e gênero. | N1 |
| **RF03** | Carteira de Créditos | O sistema deve creditar +1 no saldo do usuário automaticamente ao confirmar uma doação. | N1 |
| **RF04** | Resgate de Livros | O sistema deve permitir o resgate de um livro disponível, debitando 1 crédito do saldo do usuário. | N1 |
| **RF05** | Catálogo Público | O sistema deve listar o acervo de livros disponíveis, permitindo busca e filtros. | N1 |
| **RF06** | Avaliação de Conservação | O sistema deve permitir a classificação do estado físico do livro de 1 a 5 estrelas. | N2 |
| **RF07** | Clubes de Leitura | O sistema deve permitir a criação de grupos de discussão locais vinculados aos usuários. | N3 |

## Requisitos Não Funcionais (RNF) do Sebo Comunitário

| ID | Categoria | Descrição Técnica e Critério |
| :--- | :--- | :--- |
| **RNF01** | Confiabilidade (Transação) | As operações de resgate de livro e débito de crédito devem ser tratadas de forma atômica no PostgreSQL (ACID) para impedir concorrência e saldos negativos. |
| **RNF02** | Segurança (Autenticação) | As rotas de doação e resgate no backend (Java Spring) devem ser bloqueadas para visitantes, exigindo token de sessão válido (ex: JWT). |
| **RNF03** | Segurança (Criptografia) | As senhas dos usuários devem ser salvas no banco de dados com hash (ex: BCrypt), nunca em texto limpo. |
| **RNF04** | Usabilidade (Responsividade) | O frontend em React deve ser totalmente responsivo, garantindo navegação fluida em dispositivos móveis para facilitar o uso pela comunidade. |
| **RNF05** | Desempenho (Tempo de Resposta) | A listagem do catálogo de livros deve retornar os dados em menos de 500ms para o usuário final. |

## Modelo de Dados Implementado (PostgreSQL)

O modelo de dados está no [DER](../DER/README.md), mantido junto com as migrações Flyway em
`backend/src/main/resources/db/migration`.
