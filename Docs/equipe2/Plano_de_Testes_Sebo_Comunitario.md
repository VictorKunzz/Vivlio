# Plano de Testes: Sebo Comunitário

**Projeto:** Sebo | **Responsável:** Daniela (QA)

> Revisão de outubro/2026 (equipe 2). A versão original da equipe anterior está preservada em
> `Docs/documentação_defasada/`. Alterações: rotas reais da API, nomes reais das tabelas, códigos HTTP
> esperados e nova seção [5.1 de rastreabilidade](#51-rastreabilidade-com-os-testes-automatizados-out2026)
> com os testes automatizados.

**Documentos de referência:**

- Acordo de Manutenibilidade e Engenharia de Software (não incluído no repositório)
- [Documento de Especificação de Requisitos e DoR](Documento%20de%20Especifica%C3%A7%C3%A3o%20de%20Requisitos%20e%20DoR.md)
- [Documento de Handoff, seção 6.2 — Contrato da API](Vivlio%20-%20Handoff.md#62-contrato-da-api)

## 1. Objetivo

Definir a estratégia, o ambiente e os casos de teste necessários para garantir que o sistema, na sua versão N1 (cadastro de usuários, acervo de livros, sistema de créditos e solicitação de troca), atenda às regras de negócio especificadas, com atenção especial à RN Crítica: um usuário só pode resgatar um livro se tiver pelo menos 1 crédito ativo na conta.

## 2. Escopo

### Dentro do escopo (N1)

| RF | Funcionalidade |
| :--- | :--- |
| **RF01** | Gestão de Contas (cadastro, login) |
| **RF02** | Cadastro de Doação de Livro |
| **RF03** | Carteira de Créditos |
| **RF04** | Resgate de Livros |
| **RF05** | Catálogo Público |

### Requisitos Não Funcionais cobertos

| RNF | Categoria |
| :--- | :--- |
| **RNF01** | Confiabilidade / Atomicidade das transações |
| **RNF02** | Segurança / Autenticação |
| **RNF03** | Segurança / Criptografia de senha |
| **RNF05** | Desempenho do catálogo (< 500ms) |

## 3. Estratégia de Testes

Conforme definido no Acordo de Manutenibilidade (Seção 4):

- **Foco primário:** regras de negócio no backend/domain, com destaque para a lógica de créditos (doação e resgate).
- **Frequência:** antes de cada entrega oficial, será executado o roteiro completo cobrindo o fluxo principal e as regras de negócio críticas.
- **Níveis de teste:** funcionais manuais/exploratórios (cadastro, doação, resgate); testes de API via Postman/Insomnia para validar regras server-side; testes de concorrência para a RN Crítica (RNF01).
- **Testes automatizados de backend:** suíte JUnit + MockMvc com H2 em memória. Roda localmente com `docker compose exec backend ./mvnw -B -ntp test` (ou `./mvnw test` na pasta `backend/`, sem Docker) e automaticamente no CI a cada Pull Request (job `backend` do `.github/workflows/ci.yml`). A relação entre casos e testes está na seção 5.1. O frontend não possui testes automatizados (DT-04).

### Definition of Done: validação do QA

Nenhuma tarefa é considerada concluída sem:

- Validação pelo roteiro de testes deste plano, sem erros críticos.
- Merge na branch main/develop.

## 4. Ambiente de Testes

- **Ferramenta de API:** Postman ou Insomnia, com a coleção dedicada às rotas do backend em `backend/postman/Vivlio.postman_collection.json` (`POST /api/auth/registrar`, `POST /api/auth/login`, `GET /api/usuarios/me`, `GET /api/livros`, `POST /api/livros`, `POST /api/resgates`).
- **Pré-requisito:** API e banco no ar com `docker compose up`, conforme `Docs/devops/ambiente-local.md` (a API é real, não há mock).
- **Banco de dados:** PostgreSQL, validação direta das tabelas `usuarios`, `livros` e `transacoes_credito` quando necessário.
- **Ponto de sincronização:** reunião 'Daniela + Devs', rodando as coleções do Postman contra a API para validar a RN Crítica.

## 5. Matriz de Casos de Teste

### RF01: Gestão de Contas (Cadastro de Usuário)

| ID | Cenário | Pré-condição | Passos | Resultado Esperado | Prior. |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC01** | Cadastro com dados válidos | Nenhuma | Preencher nome, e-mail e senha válidos e confirmar | Conta criada com sucesso; `saldo_creditos` = 0 | Alta |
| **TC02** | Cadastro com e-mail já existente | Já existe usuário com o e-mail X | Tentar cadastrar novamente com o mesmo e-mail | Sistema exibe: "E-mail já cadastrado. Por favor, faça login." Conta não é duplicada (API: HTTP 409) | Alta |
| **TC03** | Cadastro com campos obrigatórios vazios | Nenhuma | Enviar formulário sem preencher nome/e-mail/senha | Sistema bloqueia o envio e sinaliza os campos obrigatórios (API: HTTP 400 com os campos em `campos`) | Média |
| **TC04** | Senha armazenada com segurança (RNF03) | Usuário cadastrado | Inspecionar coluna `senha_hash` no banco | Senha salva com hash (BCrypt), nunca em texto puro | Alta |

### RF02 / RF03: Doação de Livro e Crédito

| ID | Cenário | Pré-condição | Passos | Resultado Esperado | Prior. |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC05** | Doação com dados completos (cenário feliz) | Usuário logado | Preencher título, autor e gênero; confirmar doação | Livro salvo com status DISPONIVEL; saldo +1; mensagem: "Livro cadastrado! Você ganhou 1 crédito." (API: HTTP 201) | Alta |
| **TC06** | Doação com campo obrigatório faltando | Usuário logado | Enviar formulário sem título/autor/gênero | Sistema bloqueia a ação e destaca os campos em vermelho; nenhum crédito é gerado (API: HTTP 400) | Média |
| **TC07** | Registro da transação de entrada | Usuário logado, doação concluída | Verificar tabela `transacoes_credito` após doação | Registro criado com tipo: ENTRADA, descricao: 'Doação de livro' | Alta |
| **TC08** | Proporção estrita 1:1 | Usuário logado | Doar 3 livros em sequência | Saldo aumenta exatamente 3 (nunca mais, nunca menos) | Alta |

### RF04: Resgate de Livros (RN CRÍTICA)

| ID | Cenário | Pré-condição | Passos | Resultado Esperado | Prior. |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC09** | Resgate com saldo suficiente (cenário feliz) | Usuário com saldo ≥ 1, livro DISPONIVEL | Acessar livro e clicar em "Resgatar (1 Crédito)" | Saldo debitado em -1; livro muda para RESGATADO e é vinculado ao usuário; mensagem de confirmação (API: HTTP 200) | Alta |
| **TC10** | Resgate com saldo = 0 (bloqueio frontend) | Usuário com saldo = 0 | Clicar em "Resgatar" | Sistema bloqueia no frontend; exibe: "Você precisa de pelo menos 1 crédito ativo para resgatar um livro. Doe um livro para ganhar créditos." | Crítica |
| **TC11** | Resgate com saldo = 0 via chamada direta à API (bypass do frontend) | Usuário com saldo = 0 | Enviar requisição `POST /api/resgates` diretamente (Postman/Insomnia) | Backend também bloqueia (validação server-side); mesma mensagem de erro; nenhuma alteração no banco (API: HTTP 409) | Crítica |
| **TC12** | Registro da transação de saída | Resgate concluído | Verificar tabela `transacoes_credito` | Registro criado com tipo: SAIDA, descricao: 'Resgate de livro' | Alta |
| **TC13** | Tentativa de resgatar livro já resgatado | Livro com status RESGATADO | Outro usuário tenta resgatar o mesmo livro | Sistema impede o resgate; livro não aparece mais como disponível (API: HTTP 409, "Este livro não está mais disponível para resgate.") | Alta |
| **TC14** | Condição de corrida: 2 resgates simultâneos, saldo = 1 (RNF01) | Usuário com saldo = 1, dois livros disponíveis | Disparar duas requisições de resgate ao mesmo tempo | Apenas uma transação é confirmada; saldo final = 0 (nunca negativo); transação atômica no PostgreSQL | Crítica |

### RF05: Catálogo Público

| ID | Cenário | Pré-condição | Passos | Resultado Esperado | Prior. |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC15** | Listagem exibe apenas livros disponíveis | Existem livros DISPONIVEL e RESGATADO | Acessar catálogo | Somente livros com status DISPONIVEL aparecem na listagem pública | Média |
| **TC16** | Busca/filtro por título, autor ou gênero | Catálogo com múltiplos livros | Buscar por termo específico | Resultados filtrados corretamente | Média |
| **TC17** | Tempo de resposta do catálogo (RNF05) | Catálogo populado | Medir tempo de resposta da requisição de listagem | Retorno em menos de 500ms | Baixa |

### RNF02: Segurança / Autenticação

| ID | Cenário | Pré-condição | Passos | Resultado Esperado | Prior. |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC18** | Doação/resgate sem token de sessão | Visitante não autenticado | Tentar `POST /api/livros` ou `POST /api/resgates` sem token | Requisição bloqueada (API: HTTP 401) | Crítica |
| **TC19** | Token inválido ou expirado | Usuário com token expirado | Tentar doar ou resgatar | Requisição bloqueada (API: HTTP 401); sessão deve ser renovada/login exigido | Alta |

### Legenda de Prioridade

- **Crítica:** bloqueia o release se falhar (RN Crítica ou segurança).
- **Alta:** deve ser corrigido antes da entrega, mas não bloqueia sozinho.
- **Média/Baixa:** registrado como bug, priorizado conforme severidade e capacidade do sprint.

### Observação de testabilidade

TC10, TC11 e TC14 validam a RN Crítica sob três ângulos diferentes (frontend, backend isolado, concorrência). Nenhum substitui o outro. Um bug de saldo negativo tipicamente só aparece no TC14, mesmo que TC10 e TC11 passem.

### 5.1 Rastreabilidade com os testes automatizados (out/2026)

Classes em `backend/src/test/java/br/com/vivlio/`; cada teste traz o ID do caso no `@DisplayName`. A coluna "Situação na interface" registra o que a execução manual encontra hoje nas telas; as divergências estão detalhadas no DT-06 do [Relatório de Débitos Técnicos](Vivlio%20-%20Debitos%20Tecnicos.md).

| ID | Teste automatizado (backend) | Situação na interface |
| :--- | :--- | :--- |
| **TC01** | `auth/controller/AuthControllerTest` | Atende. |
| **TC02** | `auth/controller/AuthControllerTest` | Atende. |
| **TC03** | `auth/controller/AuthControllerTest` | Parcial: o navegador bloqueia campos vazios (atributo `required`), sem destaque em vermelho; erros da API mostram só a primeira mensagem. |
| **TC04** | `auth/controller/AuthControllerTest` | Não se aplica (verificação no banco). |
| **TC05** | `livro/controller/DoacaoControllerTest` | Diverge: a mensagem exibida é "'&lt;título&gt;' entrou no acervo. Seu saldo agora é de N." |
| **TC06** | `livro/controller/DoacaoControllerTest` | Parcial: mesmo comportamento do TC03. |
| **TC07** | `livro/controller/DoacaoControllerTest` | Não se aplica (verificação no banco). |
| **TC08** | `livro/controller/DoacaoControllerTest` | Atende. |
| **TC09** | `resgate/controller/ResgateControllerTest` | Diverge: o botão do card é "Resgatar" (sem página de detalhes nem modal) e a mensagem exibida é "Resgate concluído. O livro '&lt;título&gt;' é seu. Saldo atual: N." |
| **TC10** | Sem automação (frontend sem testes, DT-04) | **Falha:** o botão continua habilitado com saldo 0; a mensagem esperada só aparece depois que a API responde 409. Como o caso é Crítico, bloqueia o critério de saída. |
| **TC11** | `resgate/controller/ResgateControllerTest` | Não se aplica (chamada direta à API). |
| **TC12** | `resgate/controller/ResgateControllerTest` | Não se aplica (verificação no banco). |
| **TC13** | `resgate/controller/ResgateControllerTest` | Atende. |
| **TC14** | `resgate/service/ResgateConcorrenteTest` | Não se aplica (concorrência). |
| **TC15** | `livro/controller/LivroControllerTest` | Atende. |
| **TC16** | `livro/controller/LivroControllerTest` (título, autor e gênero) | Atende. |
| **TC17** | Sem automação (não há teste de desempenho) | Executar manualmente. |
| **TC18** | `DoacaoControllerTest` e `ResgateControllerTest` | Não se aplica (chamada direta à API). |
| **TC19** | `AuthControllerTest` cobre token inválido; token expirado não tem teste automatizado | Atende: em qualquer 401 o frontend descarta o token e redireciona para o login. |

## 6. Registro e Resolução de Bugs

Conforme o Acordo de Manutenibilidade (Seção 4.2), todo bug identificado deve ser documentado com:

- Passos para reproduzir
- Resultado esperado vs. Resultado obtido
- Severidade (Crítica / Alta / Média / Baixa)

**Regra de bloqueio:** bugs de alta severidade (e todos os classificados como Crítica nesta matriz) bloqueiam o encerramento da tarefa e impedem o merge/release até serem corrigidos.

## 7. Critério de Saída (Exit Criteria)

A versão N1 é considerada pronta para entrega quando:

- 100% dos casos de prioridade Crítica (TC10, TC11, TC14, TC18) passarem.
- Nenhum bug de severidade Alta ou Crítica estiver em aberto.
- Os casos de prioridade Alta tiverem taxa de sucesso ≥ 90%.
