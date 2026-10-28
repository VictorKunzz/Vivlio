# Vivlio — Documento de Handoff (N2)

**Versão entregue:** `v2.0` · **Branch de integração:** `develop` · **Outubro/2026**

Este é o guia para a equipe que vai receber o Vivlio. Ele explica como subir o sistema do zero, o que foi entregue, o que ficou de fora, onde estão as partes frágeis e onde encontrar cada coisa no código. O handoff da N1, da equipe anterior, continua em [`Vivlio - Handoff (N1).pdf`](Vivlio%20-%20Handoff%20(N1).pdf) como histórico.

**Documentos complementares:**
- [Relatório de Débitos Técnicos](../debitos-tecnicos/README.md): situação de cada débito e a ordem sugerida.
- [DER](../DER/README.md): modelo de dados e migrações.
- [Casos de teste da N2](../testes/casos-de-teste-n2.md): o que testar e onde cada caso está automatizado.
- [Ambiente local](../devops/ambiente-local.md): Docker, variáveis e o caminho sem Docker.

---

## 1. Visão geral do sistema

O Vivlio é uma plataforma de troca de livros usados baseada em créditos. Doar 1 livro gera 1 crédito, e 1 crédito permite resgatar 1 livro do acervo. A **regra crítica** é que ninguém resgata sem ter pelo menos 1 crédito ativo. Ela é validada no backend, na tela e no banco (`CHECK saldo_creditos >= 0`).

Na N2, toda doação passa a ter uma **avaliação do estado do livro**. O doador responde a um questionário de 6 critérios, o sistema calcula uma nota de 1 a 5 e recusa livros que não podem entrar no acervo.

| Camada | Tecnologia |
|---|---|
| Backend | Java 17 + Spring Boot 3.3.5 (Web, Data JPA, Security, Validation) + JJWT + **Flyway** |
| Frontend | React 19 + Vite 8 + React Router 7 + Axios |
| Banco | PostgreSQL 16 (Docker); H2 em memória nos testes |
| Testes | JUnit 5 + MockMvc (backend) · **Vitest + Testing Library** (frontend) |
| CI | GitHub Actions: convenções, backend, frontend |
| Repositório | Monorepo `backend/` + `frontend/` + `docs/`; integração na `develop` |

## 2. Como rodar o projeto do zero

### 2.1 Pré-requisitos

- **Docker e Docker Compose.** É o caminho recomendado, e não exige JDK nem Node.
- Para rodar fora do Docker: JDK **17** (o Lombok da versão usada falha em silêncio no JDK 23) e Node 20.19+ ou 22.12+.

### 2.2 Subir tudo

```bash
cp .env.example .env      # troque JWT_SECRET por uma chave sua, com 32+ caracteres
docker compose up -d
```

| Serviço | Endereço |
|---|---|
| Frontend | http://localhost:5173 |
| API | http://localhost:8080 |
| PostgreSQL | `localhost:5432`. Se a porta estiver ocupada, mude `POSTGRES_PORT` no `.env` |

As tabelas são criadas pelas **migrações Flyway** na primeira subida. Um banco antigo, criado pelo Hibernate, é migrado sem perder dados (*baseline*). Quando `pom.xml` ou `package.json` mudarem, rode `docker compose build`.

> **Windows:** se o build do backend falhar com código **127**, o `backend/mvnw` está com quebra de linha CRLF. Recrie o arquivo com `rm backend/mvnw && git checkout -- backend/mvnw`. O `.gitattributes` já força LF para clones novos.

### 2.3 Rodar os testes

```bash
cd backend  && ./mvnw verify     # JUnit; usa H2 e roda as migrações
cd frontend && npm ci && npm test  # Vitest
```

### 2.4 Variáveis de ambiente

| Variável | Padrão | Observação |
|---|---|---|
| `JWT_SECRET` | **nenhum** | **Obrigatória**, com pelo menos 32 caracteres. Sem ela o backend não inicializa |
| `JWT_EXPIRACAO_MS` | `7200000` (2h) | Validade do token |
| `JPA_DDL_AUTO` | `validate` | O schema vem do Flyway; o Hibernate só confere |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | Postgres do compose | Conexão do backend |
| `POSTGRES_PORT` / `BACKEND_PORT` / `FRONTEND_PORT` | `5432` / `8080` / `5173` | Portas publicadas |
| `VITE_API_URL` | `http://localhost:8080` | Endereço da API visto pelo navegador |

### 2.5 Primeiro teste de ponta a ponta

O banco sobe vazio. Para validar a instalação:

1. Crie uma conta e doe um livro respondendo ao questionário. Você ganha 1 crédito e o livro aparece com estrelas no Acervo.
2. Crie uma segunda conta, doe outro livro e resgate o primeiro pelo botão **Resgatar (1 Crédito)**.
3. Confira o histórico em **Meu perfil → Extrato de créditos**.

A coleção [`backend/postman/Vivlio.postman_collection.json`](../../backend/postman/Vivlio.postman_collection.json) cobre esses cenários pela API, incluindo os casos de erro, e deve ser rodada contra um banco vazio.

## 3. O que está pronto

| RF | Funcionalidade | Situação |
|---|---|---|
| RF01 | Gestão de contas | Cadastro, login com JWT (2h) e perfil. Senha com BCrypt. Formulário destaca todos os campos inválidos |
| RF02 | Cadastro de doação | Título, autor, gênero e **questionário de avaliação obrigatório** |
| RF03 | Carteira de créditos | +1 na doação, −1 no resgate, sempre pelo `CreditoService`. **Extrato** no perfil |
| RF04 | Resgate de livros | RN crítica no backend (lock pessimista), na tela (botão travado com saldo 0) e no banco. Modal de confirmação com os detalhes do livro |
| RF05 | Catálogo público | Busca por título/autor e filtro por gênero. Cards com as estrelas do estado |
| **RF06** | **Avaliação do estado** (N2) | Ficha de 6 critérios, nota = menor critério, eliminatórios recusam (422), regra do didático (máx. 2), sobrecapa registrada |

### 3.1 Regra da avaliação (RF06)

- **Critérios:** capa, lombada, páginas, marcações, manchas e odor. Cada opção vale de 1 a 5, mas o usuário **só vê a descrição**.
- **Nota final:** a **menor** nota entre os critérios, porque um defeito grave não é compensado pelos outros.
- **Eliminatórios** (página faltando, mofo, dano por água, infestação, texto ilegível, capa separada): o livro é recusado com 422, não entra no acervo e não gera crédito.
- **Casos especiais:** didático com exercícios respondidos fica com no máximo 2. Sobrecapa ausente não reduz a nota. Dedicatória conta como "nome do dono" (4).
- **Onde mudar:** textos e notas ficam nos enums de `backend/.../avaliacao/entity/`. O frontend lê tudo de `GET /api/avaliacao/questionario`, então não há texto duplicado.

### 3.2 O que mudou na N2

| Entrega | PR |
|---|---|
| Avaliação do estado do livro (evolução) | #13 |
| Telas alinhadas aos critérios de aceitação (TC03, TC05, TC06, TC09, TC10) | #15 |
| `JWT_SECRET` obrigatório e token de 2h | #16 |
| Migrações Flyway e `ddl-auto=validate` | #17 |
| Extrato de créditos | #18 |
| Testes de frontend e `npm test` na CI | #19 |
| Documentação, DER, Postman e este handoff | PR de documentação |

## 4. O que não está pronto

| Item | O que falta | Débito |
|---|---|---|
| Conferência do sebo na entrega | Papel de sebo, status "aguardando conferência", crédito só depois da confirmação | DT-08 |
| Especificação do RF06 | Documento ainda descreve "escolher de 1 a 5 estrelas" | DT-07 |
| Sessão | Refresh token, revogação no logout, cookie `httpOnly` | DT-03 (parcial) |
| Livros antigos sem nota | Decisão da PO | DT-09 |
| Fotos do livro | Não iniciado. Recomendado só se a troca passar a ser direta entre leitores | — |
| RF07 — Clubes de leitura (N3) | Nada implementado | — |

## 5. Pontos de atenção

Nada é mock: o frontend conversa com a API real, e a API grava no PostgreSQL.

**5.1 O schema só muda por migração.** Crie `V3__descricao.sql` em `backend/src/main/resources/db/migration`, **nunca** edite a V1 ou a V2, e atualize o [DER](../DER/README.md) no mesmo PR. Com `ddl-auto=validate`, uma entidade que não bate com o banco impede o backend de subir. Isso é de propósito.

**5.2 O saldo existe em dois lugares.** `usuarios.saldo_creditos` e `transacoes_credito` precisam satisfazer saldo = entradas − saídas. Toda alteração de saldo passa por `CreditoService.creditar()` ou `debitar()`. O `ExtratoControllerTest` quebra se isso deixar de valer.

**5.3 A concorrência no resgate depende de lock pessimista.** O `ResgateService` usa `PESSIMISTIC_WRITE` em usuário e livro. Se refatorar o resgate, mantenha os locks e rode o `ResgateConcorrenteTest` e o `ExtratoControllerTest`.

**5.4 A nota é calculada só no servidor.** O frontend nunca envia a estrela. Não exponha a nota das opções no endpoint do questionário, porque isso deixaria o usuário "escolher" a nota. O teste AV09 cobre esse ponto.

**5.5 O crédito sai antes da conferência.** Enquanto o DT-08 não for feito, o crédito depende da honestidade do doador.

**5.6 Ainda pendentes:** gênero sem validação no backend (DT-10), acervo sem paginação (DT-11) e CORS só para `localhost` (DT-13, obrigatório antes de publicar).

## 6. Onde encontrar as coisas

O backend é organizado por domínio (`auth`, `usuario`, `livro`, `avaliacao`, `credito`, `resgate`, `shared`, `config`), cada um com `entity`, `repository`, `service`, `controller` e `dto`.

| O que você procura | Onde está |
|---|---|
| Ficha de avaliação: opções, notas, eliminatórios | `backend/.../avaliacao/entity/` |
| Cálculo da nota e questionário | `backend/.../avaliacao/service/AvaliacaoService.java` |
| Regra dos créditos e extrato | `backend/.../credito/service/CreditoService.java` |
| Fluxo do resgate e RN crítica | `backend/.../resgate/service/ResgateService.java` |
| Fluxo da doação | `backend/.../livro/service/LivroService.java` |
| Login, registro e hash de senha | `backend/.../auth/service/AuthService.java` |
| Token JWT e validação do segredo | `backend/.../auth/service/JwtService.java` |
| Rotas públicas x protegidas e CORS | `backend/.../config/SecurityConfig.java` |
| Tratamento de erros da API | `backend/.../shared/exception/ApiExceptionHandler.java` |
| Migrações do banco | `backend/src/main/resources/db/migration/` |
| Testes do backend | `backend/src/test/java/br/com/vivlio/` |
| Rotas do frontend | `frontend/src/App.jsx` |
| Sessão, login e saldo em memória | `frontend/src/context/AuthProvider.jsx` |
| Cliente HTTP e tratamento de 401 | `frontend/src/services/api.js` e `navegacao.js` |
| Telas | `frontend/src/pages/` (Login, Cadastro, Acervo, Doacao, Perfil, NaoEncontrada) |
| Estrelas e modal de resgate | `frontend/src/components/Estrelas.jsx` e `ModalResgate.jsx` |
| Testes do frontend | `frontend/src/**/*.test.jsx` e `frontend/src/test/` |
| Pipeline | `.github/workflows/ci.yml` |

### 6.1 Endpoints da API

| Método e rota | Acesso | Função |
|---|---|---|
| `POST /api/auth/registrar` | Público | Cria conta (saldo inicia em 0) e devolve token |
| `POST /api/auth/login` | Público | Autentica e devolve token |
| `GET /api/usuarios/me` | Token | Perfil e saldo do usuário logado |
| `GET /api/livros` | Público | Acervo disponível (params `busca`, `genero`), com `nota`, `sobrecapaAusente` e `doadorId` |
| `POST /api/livros` | Token | Doa um livro com questionário. **201** aprovado (+1 crédito), **422** eliminatório, **400** inválido |
| `GET /api/avaliacao/questionario` | Token | **N2.** Perguntas, opções e eliminatórios, sem as notas |
| `POST /api/resgates` | Token | Resgata um livro e debita 1 crédito. **409** sem saldo, livro indisponível ou livro do próprio doador (DT-12) |
| `GET /api/creditos/extrato` | Token | **N2.** Saldo, total de entradas e saídas e transações (mais recente primeiro) |

### 6.2 Contrato dos endpoints novos

**`POST /api/livros`** (corpo):

```json
{
  "titulo": "Dom Casmurro",
  "autor": "Machado de Assis",
  "genero": "Romance",
  "avaliacao": {
    "capa": "CANTOS_LEVEMENTE_GASTOS",
    "lombada": "RETA",
    "paginas": "VARIAS_ORELHAS",
    "marcacoes": "NOME_DO_DONO",
    "manchas": "NENHUMA",
    "odor": "NENHUM",
    "eliminatorios": [],
    "didaticoComExerciciosRespondidos": false,
    "sobrecapaAusente": false
  }
}
```

Resposta **201**: `{ "livro": { ..., "nota": 3, "sobrecapaAusente": false }, "saldoCreditos": 1 }`

**`GET /api/creditos/extrato`** (resposta):

```json
{
  "saldo": 1,
  "totalEntradas": 3,
  "totalSaidas": 2,
  "transacoes": [
    { "id": 5, "tipo": "SAIDA", "descricao": "Resgate de livro", "data": "2026-10-27T17:05:12.3Z" }
  ]
}
```

Os códigos válidos de cada critério estão nos enums `EstadoCapa`, `EstadoLombada`, `EstadoPaginas`, `EstadoMarcacoes`, `EstadoManchas`, `EstadoOdor` e `Eliminatorio`, e também são devolvidos pelo endpoint do questionário.

## 7. Sugestão de por onde começar

1. Subir o ambiente (passo 2.2) e validar o fluxo doar → resgatar → extrato, pela tela e pela coleção do Postman.
2. Rodar `./mvnw verify` e `npm test` para confirmar que as duas suítes passam na máquina de vocês.
3. Ler `AvaliacaoService`, `CreditoService` e `ResgateService`. É onde estão as regras que não podem quebrar.
4. Resolver o **DT-08** (conferência do sebo) antes de qualquer outra mudança no RF06.
5. Só então seguir para o RF07 (clubes de leitura), criando as tabelas numa migração `V3`.
