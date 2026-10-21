# Vivlio — Documento de Handoff

*Entrega da N1 • Setembro/2026 • branch develop*

> Revisão de outubro/2026 (equipe 2). A versão original da equipe anterior está preservada em
> `Docs/documentação_defasada/`. Alterações: "Como rodar" atualizado para o ambiente Docker da raiz,
> pipeline de CI registrado, novo ponto de atenção 5.9, contrato completo da API (6.2), resumo do modelo
> de dados (6.3), faixa correta de casos de teste e caminhos dos documentos atualizados.

Este documento é o guia para o grupo que vai receber o Vivlio. Ele cobre como subir o sistema do zero, o que foi entregue, o que ficou de fora, onde estão as partes frágeis e onde encontrar cada coisa no código. O relatório de débitos técnicos é complementar a este documento.

## 1. Visão geral do sistema

O Vivlio é uma plataforma de troca de livros usados baseada em créditos: doar 1 livro gera 1 crédito, e 1 crédito permite resgatar 1 livro do acervo. A regra de negócio crítica é que ninguém resgata sem ter pelo menos 1 crédito ativo, e essa validação existe no backend (não só na tela).

| Camada | Tecnologia |
| :--- | :--- |
| Backend | Java 17 + Spring Boot 3.3.5 (Web, Data JPA, Security, Validation) + JJWT |
| Frontend | React 19 + Vite 8 + React Router 7 + Axios |
| Banco | PostgreSQL 16 (via Docker Compose); H2 em memória apenas nos testes |
| Repositório | Monorepo: pastas `backend/` e `frontend/`; branch de integração é a develop |

## 2. Como rodar o projeto do zero

O guia completo (comandos, variáveis, limitações conhecidas e o motivo de cada versão fixada) está em
`Docs/devops/ambiente-local.md`. Resumo:

### 2.1 Caminho recomendado: Docker

Pré-requisito: Docker e Docker Compose. Na raiz do repositório:

```bash
cp .env.example .env
docker compose up
```

Sobem três serviços: PostgreSQL 16 (`vivlio-postgres`, porta 5432), backend (`http://localhost:8080`) e frontend (`http://localhost:5173`). Não é preciso ter JDK, Maven ou Node instalados. O `.env` é obrigatório: o compose não sobe o backend sem `JWT_SECRET` definido.

As tabelas são criadas automaticamente pelo Hibernate na primeira execução do backend — não há script SQL nem dados de exemplo.

Para rodar os testes do backend (usam H2, não dependem do banco):

```bash
docker compose exec backend ./mvnw -B -ntp test
```

### 2.2 Sem Docker

Funciona, mas as versões passam a ser responsabilidade de quem roda:

- **JDK 17** exatamente: o Lombok resolvido pelo Spring Boot 3.3.5 não funciona em JDK 23 e a compilação falha sem mensagem clara.
- Node.js 20.19+ ou 22.12+ (exigência do Vite 8) e npm.
- PostgreSQL 16 em `localhost:5432`, com banco, usuário e senha `vivlio`.

```bash
cd backend && ./mvnw spring-boot:run      # Windows: mvnw.cmd spring-boot:run
cd backend && ./mvnw test                 # testes
cd frontend && cp .env.example .env && npm install && npm run dev
```

Use sempre o wrapper (`mvnw`), nunca `mvn`. Existe também o perfil `application-local.properties`, que só troca as credenciais do banco para postgres/postgres. Ative com `--spring.profiles.active=local`.

### 2.3 Variáveis de ambiente

No caminho Docker, as variáveis ficam no `.env` da raiz (modelo em `.env.example`; tabela completa em `Docs/devops/ambiente-local.md`). A aplicação lê:

| Variável | Padrão na aplicação | Observação |
| :--- | :--- | :--- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/vivlio` | No Docker, o compose aponta para o serviço `postgres` |
| `DB_USERNAME` / `DB_PASSWORD` | `vivlio` / `vivlio` | No Docker, vêm de `POSTGRES_USER` / `POSTGRES_PASSWORD` |
| `JPA_DDL_AUTO` | `update` | Ver ponto de atenção 5.1 |
| `JPA_SHOW_SQL` | `false` | Exibe o SQL gerado no log |
| `JWT_SECRET` | chave fixa de desenvolvimento | No Docker é obrigatória e sem padrão; ver 5.2 |
| `JWT_EXPIRACAO_MS` | `86400000` (24h) | Validade do token |
| `SERVER_PORT` | `8080` | Porta da API |
| `VITE_API_URL` | `http://localhost:8080` | Endereço da API usado pelo navegador |

### 2.6 Primeiro teste de ponta a ponta

O banco sobe vazio, então o acervo aparece sem livros e não é possível resgatar nada de cara. O caminho para validar a instalação é: cadastrar um usuário, doar um livro (ganha 1 crédito), cadastrar um segundo usuário, doar outro livro e então resgatar. A coleção `backend/postman/Vivlio.postman_collection.json` cobre esses cenários pela API, incluindo os casos de erro.

## 3. O que está pronto (entregue na N1)

| RF | Funcionalidade | Situação |
| :--- | :--- | :--- |
| RF01 | Gestão de Contas | Cadastro, login com JWT e perfil (`/api/usuarios/me`). Telas de Login, Cadastro e Perfil. Senha com BCrypt. |
| RF02 | Cadastro de Doação | `POST /api/livros` com título, autor e gênero validados. Tela de Doação. |
| RF03 | Carteira de Créditos | +1 crédito na doação e -1 no resgate, com registro em `transacoes_credito`. Saldo exibido no Perfil. |
| RF04 | Resgate de Livros | `POST /api/resgates` com a RN crítica validada no backend, transação atômica e lock pessimista contra concorrência. |
| RF05 | Catálogo Público | `GET /api/livros` público, com busca por título/autor e filtro por gênero. Tela de Acervo. |

Testes automatizados existentes: suíte JUnit no backend cobrindo autenticação, listagem, doação, resgate e um teste específico de resgate concorrente. Rodam com `./mvnw test`.

Pipeline de CI: `.github/workflows/ci.yml` roda a cada Pull Request para `develop` e `main`. Valida nome de branch, mensagens de commit e título do PR (Conventional Branch e Conventional Commits), executa `./mvnw -B -ntp verify` no backend e `npm run lint`, `prettier --check` e `npm run build` no frontend. O check obrigatório para o merge é o `ci-ok`. Regras completas no `CONTRIBUTING.md`.

## 4. O que não está pronto

| Item | Escopo | O que falta |
| :--- | :--- | :--- |
| RF06 — Avaliação de Conservação | N2 | Nota inteira de 1 a 5 no cadastro do livro, exibida no card. Exige nova coluna `estado_conservacao` em `livros`, validação no backend (rejeitar 0 ou 6 com 400) e componente de estrelas no frontend. |
| RF07 — Clubes de Leitura | N3 | Nada implementado. Exige novas tabelas de clubes e participantes, além de telas próprias. |
| Migrações de banco | — | Não há Flyway/Liquibase; o schema é gerado pelo Hibernate. |
| Testes de frontend | — | Nenhum framework de teste instalado no frontend; o CI só faz lint, formatação e build. |
| Extrato de créditos | — | As transações são gravadas, mas não há endpoint nem tela para consultá-las. |
| Aderência das telas aos critérios | — | Mensagens e bloqueio de resgate sem saldo diferentes da especificação (ver 5.9). |

## 5. Pontos de atenção

Nada no sistema é mock: o frontend conversa com a API real e a API grava no PostgreSQL. Os pontos abaixo são os lugares frágeis que merecem cuidado antes de mexer.

### 5.1 Schema gerado por ddl-auto=update

O Hibernate cria e altera as tabelas sozinho. Ele adiciona colunas novas, mas não remove nem altera as existentes, e não há histórico de versões. Como a avaliação de conservação exige uma coluna nova, o recomendado é adotar Flyway com um baseline do schema atual antes de qualquer mudança estrutural.

### 5.2 Segredo JWT com valor padrão no repositório

A chave de assinatura tem um fallback fixo em `application.properties`. Serve para desenvolvimento, mas qualquer ambiente exposto precisa definir `JWT_SECRET`. O token vale 24 horas, fica em localStorage e não há revogação: o logout só apaga o token no navegador.

No caminho Docker o compose exige `JWT_SECRET` no `.env` e não usa o fallback. Rodando sem Docker, o fallback continua valendo. A correção no código segue pendente (DT-03).

### 5.3 Saldo de créditos existe em dois lugares

O valor corrente fica em `usuarios.saldo_creditos` e o histórico em `transacoes_credito`. Quem alterar saldo fora do `CreditoService` vai criar divergência silenciosa na regra crítica. Toda operação de crédito deve passar por `CreditoService.creditar()` ou `debitar()`.

### 5.4 Concorrência no resgate depende de lock pessimista

`ResgateService` usa `PESSIMISTIC_WRITE` ao buscar usuário e livro (`buscarParaAtualizarSaldo` e `buscarParaResgate`). É o que impede saldo negativo e resgate duplicado, e é coberto pelo `ResgateConcorrenteTest`. Se refatorar o resgate, mantenha os locks e rode esse teste.

### 5.5 Gênero não é validado contra lista fixa

O frontend oferece um select com a lista de `frontend/src/constants/generos.js`, mas o backend aceita qualquer texto não vazio. Uma requisição direta à API pode gravar um gênero fora da lista e ele não aparecerá no filtro da tela.

### 5.6 Listagem do acervo sem paginação

`GET /api/livros` devolve todos os livros disponíveis de uma vez, com busca por LIKE sem índice. Funciona no volume atual, mas é o primeiro gargalo do RNF05 (resposta abaixo de 500ms) conforme o acervo crescer.

### 5.7 Usuário pode resgatar o próprio livro doado

Não há regra impedindo isso. A operação é neutra em créditos (+1 na doação, -1 no resgate), mas se isso for considerado um comportamento indevido, a validação precisa ser adicionada no `ResgateService`.

### 5.8 CORS restrito a localhost

A configuração em `SecurityConfig` libera apenas origens `http://localhost:*`. Ao publicar o frontend em outro domínio, é preciso incluir essa origem, senão as chamadas são bloqueadas pelo navegador.

### 5.9 Telas divergem dos critérios de aceitação

As mensagens de sucesso da doação e do resgate não são as da especificação, o botão "Resgatar" fica habilitado com saldo 0 (o bloqueio só vem da resposta 409 da API), não há página de detalhes do livro nem modal de confirmação e os formulários não destacam os campos inválidos. Com isso, TC03, TC05, TC06, TC09 e TC10 falham na execução manual; o TC10 é Crítico e bloqueia o critério de saída do Plano de Testes. Detalhes e ação recomendada no DT-06.

## 6. Onde encontrar as coisas

O backend é organizado por domínio (`auth`, `usuario`, `livro`, `credito`, `resgate`, `shared`, `config`), cada um com entity, repository, service, controller e dto.

| O que você procura | Onde está |
| :--- | :--- |
| Regra dos créditos (+1 / -1) e bloqueio sem saldo | `backend/.../credito/service/CreditoService.java` |
| Fluxo do resgate e RN crítica | `backend/.../resgate/service/ResgateService.java` |
| Fluxo da doação | `backend/.../livro/service/LivroService.java` |
| Login, registro e hash de senha | `backend/.../auth/service/AuthService.java` |
| Geração e leitura do token JWT | `backend/.../auth/service/JwtService.java` |
| Quem lê o token a cada requisição | `backend/.../auth/security/JwtAuthenticationFilter.java` |
| Rotas públicas x protegidas e CORS | `backend/.../config/SecurityConfig.java` |
| Tratamento de erros da API | `backend/.../shared/exception/ApiExceptionHandler.java` |
| Consultas e locks de banco | `backend/.../livro/repository/LivroRepository.java` e `usuario/repository/UsuarioRepository.java` |
| Configuração e variáveis | `backend/src/main/resources/application.properties` |
| Testes automatizados | `backend/src/test/java/br/com/vivlio/` |
| Rotas do frontend | `frontend/src/App.jsx` |
| Sessão, login e saldo em memória | `frontend/src/context/AuthProvider.jsx` |
| Cliente HTTP, token e tratamento de 401 | `frontend/src/services/api.js` |
| Extração da mensagem de erro da API | `frontend/src/services/erros.js` |
| Telas | `frontend/src/pages/` (Login, Cadastro, Acervo, Doacao, Perfil, NaoEncontrada) |
| Proteção de rota | `frontend/src/components/RotaProtegida.jsx` |
| Ambiente Docker | `docker-compose.yml`, `.env.example`, `backend/Dockerfile`, `frontend/Dockerfile` |
| Pipeline de CI | `.github/workflows/ci.yml` |
| Convenções de branch, commit e PR | `CONTRIBUTING.md`, `commitlint.config.js`, `.github/pull_request_template.md` |
| Donos de cada área (revisão obrigatória) | `.github/CODEOWNERS` |

### 6.1 Endpoints da API

| Método e rota | Acesso | Função |
| :--- | :--- | :--- |
| `POST /api/auth/registrar` | Público | Cria conta (saldo inicia em 0) e devolve token |
| `POST /api/auth/login` | Público | Autentica e devolve token |
| `GET /api/usuarios/me` | Token | Perfil e saldo do usuário logado |
| `GET /api/livros` | Público | Lista o acervo disponível (params: busca, genero) |
| `POST /api/livros` | Token | Doa um livro e credita +1 |
| `POST /api/resgates` | Token | Resgata um livro e debita 1 crédito |

### 6.2 Contrato da API

**Autenticação.** Rotas com acesso "Token" exigem o cabeçalho `Authorization: Bearer <token>`. O token é um JWT assinado com `JWT_SECRET`, cujo `sub` é o e-mail do usuário e cuja validade é `JWT_EXPIRACAO_MS`. Sem token, com token inválido ou com token expirado, a API responde **401 sem corpo**. No frontend, qualquer 401 fora de `/api/auth` apaga o token salvo e redireciona para `/login`.

**Formato de erro.** Erros de regra de negócio e de validação usam o mesmo corpo:

```json
{
  "mensagem": "Existem campos obrigatórios ou inválidos no formulário.",
  "campos": {
    "titulo": "O título é obrigatório.",
    "autor": "O autor é obrigatório."
  }
}
```

`campos` só aparece nos erros 400 de validação (um item por campo, com a primeira mensagem de cada). Nos demais erros o corpo tem apenas `mensagem`. Erros não tratados pelo `ApiExceptionHandler` (por exemplo, JSON malformado) seguem o formato padrão do Spring Boot, sem mensagem detalhada.

**Objetos de resposta.**

```json
// Usuario
{ "id": 1, "nome": "Ana Souza", "email": "ana@vivlio.com", "saldoCreditos": 0 }

// Livro (doador é o nome de quem doou)
{ "id": 1, "titulo": "Dom Casmurro", "autor": "Machado de Assis",
  "genero": "Romance", "status": "DISPONIVEL", "doador": "Ana Souza" }
```

#### `POST /api/auth/registrar`

Corpo: `{ "nome": "...", "email": "...", "senha": "..." }`. O e-mail é gravado sem espaços e em minúsculas.

| Status | Quando | Corpo |
| :--- | :--- | :--- |
| 201 | Conta criada | `{ "token": "...", "usuario": Usuario }` |
| 400 | Nome vazio; e-mail vazio ou inválido; senha vazia ou com menos de 6 caracteres | Erro com `campos` |
| 409 | E-mail já cadastrado | `"E-mail já cadastrado. Por favor, faça login."` |

#### `POST /api/auth/login`

Corpo: `{ "email": "...", "senha": "..." }`.

| Status | Quando | Corpo |
| :--- | :--- | :--- |
| 200 | Credenciais válidas | `{ "token": "...", "usuario": Usuario }` |
| 400 | E-mail ou senha vazios | Erro com `campos` |
| 401 | E-mail inexistente ou senha incorreta | `"E-mail ou senha inválidos."` |

#### `GET /api/usuarios/me`

| Status | Quando | Corpo |
| :--- | :--- | :--- |
| 200 | Token válido | `Usuario` |
| 401 | Sem token ou token inválido | — |

#### `GET /api/livros`

Parâmetros opcionais: `busca` (trecho do título **ou** do autor) e `genero` (trecho do gênero). Ambos ignoram maiúsculas/minúsculas. Retorna somente livros `DISPONIVEL`, do mais recente para o mais antigo, sem paginação.

| Status | Quando | Corpo |
| :--- | :--- | :--- |
| 200 | Sempre | `[ Livro, ... ]` (lista vazia quando nada é encontrado) |

#### `POST /api/livros`

Corpo: `{ "titulo": "...", "autor": "...", "genero": "..." }`. Os três campos são gravados sem espaços nas pontas.

| Status | Quando | Corpo |
| :--- | :--- | :--- |
| 201 | Doação registrada e +1 crédito | `{ "livro": Livro, "saldoCreditos": 1 }` |
| 400 | Título, autor ou gênero vazios | Erro com `campos` |
| 401 | Sem token ou token inválido | — |

#### `POST /api/resgates`

Corpo: `{ "livroId": 1 }`.

| Status | Quando | Corpo |
| :--- | :--- | :--- |
| 200 | Resgate concluído e -1 crédito | `{ "livro": Livro (status RESGATADO), "saldoCreditos": 0 }` |
| 400 | `livroId` ausente | Erro com `campos` (`"Informe o livro que deseja resgatar."`) |
| 401 | Sem token ou token inválido | — |
| 404 | Livro inexistente | `"Livro não encontrado."` |
| 409 | Livro já resgatado | `"Este livro não está mais disponível para resgate."` |
| 409 | Saldo 0 (RN crítica) | `"Você precisa de pelo menos 1 crédito ativo para resgatar um livro. Doe um livro para ganhar créditos."` |

### 6.3 Modelo de dados

Três tabelas, criadas pelo Hibernate a partir das entidades. O detalhamento coluna a coluna está na seção "Modelo de Dados Implementado" do Documento de Especificação.

| Tabela | Entidade | Colunas | Relacionamentos |
| :--- | :--- | :--- | :--- |
| `usuarios` | `usuario/entity/Usuario` | `id`, `nome`, `email` (único), `senha_hash`, `saldo_creditos` | — |
| `livros` | `livro/entity/Livro` | `id`, `titulo`, `autor`, `genero`, `status` (`DISPONIVEL`/`RESGATADO`) | `id_usuario_doador` (obrigatório) e `id_usuario_recebedor` → `usuarios` |
| `transacoes_credito` | `credito/entity/TransacaoCredito` | `id`, `tipo` (`ENTRADA`/`SAIDA`), `descricao`, `criado_em` | `id_usuario` → `usuarios` |

## 7. Sugestão de por onde começar

1. Subir o ambiente pelo passo 2 (`docker compose up`) e validar o fluxo doar → resgatar usando a coleção do Postman.
2. Rodar `docker compose exec backend ./mvnw -B -ntp test` para confirmar que a suíte passa na máquina de vocês.
3. Ler `CreditoService` e `ResgateService` — é onde está a regra que não pode quebrar.
4. Adotar Flyway com baseline do schema atual antes de criar a coluna de avaliação do RF06.
5. Só então implementar o RF06, aproveitando que o formulário de doação e o card do acervo já existem.

## Documentos complementares

- `README.md` — visão geral, escopo e equipe.
- `CONTRIBUTING.md` — branches (Conventional Branch), commits (Conventional Commits), Pull Request, merge e CI.
- `Docs/devops/ambiente-local.md` — ambiente Docker, caminho sem Docker, variáveis e limitações conhecidas.
- `Docs/devops/registro-de-configuracao.md` — o que está configurado no GitHub e por quê.
- `frontend/README.md` — como rodar, scripts e estrutura do frontend.
- `Docs/equipe2/Documento de Especificação de Requisitos e DoR.md` — RFs, RNFs, histórias, critérios de aceitação e modelo de dados.
- `Docs/equipe2/Plano_de_Testes_Sebo_Comunitario.md` — casos de teste (TC01 a TC19) e rastreabilidade com os testes automatizados.
- `Docs/equipe2/Vivlio - Debitos Tecnicos.md` — os seis débitos técnicos priorizados.
- `Docs/documentação_defasada/` — versões originais da equipe anterior (.docx/.pdf), mantidas sem alteração.
- `backend/postman/Vivlio.postman_collection.json` — coleção pronta de requisições da API.
