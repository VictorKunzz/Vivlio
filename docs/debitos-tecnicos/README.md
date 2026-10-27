# Relatório de Débitos Técnicos — N2

Estado na versão **v2.0**. O relatório original da N1, com os cinco débitos herdados, está em [`Vivlio - Debitos Tecnicos (N1).docx`](Vivlio%20-%20Debitos%20Tecnicos%20(N1).docx).

## Resumo

| ID | Débito | Impacto | Situação | Onde foi tratado |
|---|---|---|---|---|
| DT-01 | Schema do banco sem versionamento (`ddl-auto=update`) | Alto | ✅ Resolvido | PR #17 · issue #8 |
| DT-02 | Ausência de pipeline de integração contínua | Alto | ✅ Resolvido | PR #2 |
| DT-03 | Segredo JWT versionado e sessão sem revogação | Alto | 🟡 Parcial | PR #16 · issue #9 |
| DT-04 | Frontend sem nenhum teste automatizado | Médio-Alto | ✅ Resolvido | PR #19 · issue #6 |
| DT-05 | Saldo de créditos duplicado, sem reconciliação | Médio | ✅ Resolvido | PR #18 · issue #10 |
| DT-06 | Telas divergem dos critérios de aceitação | Médio | ✅ Resolvido (textos aguardam aval da PO) | PR #15 · issue #11 |
| DT-07 | Ficha de avaliação diverge da Especificação | Médio | 🔴 Aberto | issue #21 |
| DT-08 | Conferência do sebo na entrega não existe no sistema | Alto | 🔴 Aberto | issue #22 |
| DT-09 | Livros doados antes da avaliação ficam sem nota | Baixo | 🔴 Aberto | issue #23 |
| DT-10 | Gênero não é validado contra a lista fixa | Baixo | 🔴 Aberto | issue #24 |
| DT-11 | Acervo sem paginação e busca sem índice | Médio | 🔴 Aberto | issue #25 |
| DT-12 | Usuário pode resgatar o próprio livro doado | Baixo | 🔴 Aberto | issue #26 |
| DT-13 | CORS restrito a localhost | Médio | 🔴 Aberto | issue #27 |

Os débitos DT-10 a DT-13 já constavam como "pontos de atenção" no handoff da N1 e foram promovidos a débito. DT-07 a DT-09 surgiram com a avaliação do livro (PR #13). As issues de DT-07 a DT-13 são as #21 a #27, com o rótulo `debito-tecnico`.

---

## Débitos resolvidos na N2

### DT-01 — Schema do banco sem versionamento ✅

**Problema (N1):** o Hibernate gerava o schema com `ddl-auto=update`, sem migrações versionadas.

**O que foi feito (PR #17):**
- Flyway com `V1__schema_inicial.sql` (MVP) e `V2__avaliacao_do_livro.sql` (N2). O diagrama está em [`docs/DER`](../DER/README.md).
- `ddl-auto=validate` por padrão no backend, no Docker, no `.env.example` e nos testes (as migrações rodam no H2 a cada `./mvnw verify`).
- Bancos existentes entram por *baseline* (`spring.flyway.baseline-on-migrate`), sem perda de dados. Os dois cenários (banco com dados e banco vazio) foram verificados no PostgreSQL 16.
- Novo `CHECK (saldo_creditos >= 0)`: a RN crítica também é garantida pelo banco.

### DT-02 — Ausência de pipeline de CI ✅

**O que foi feito (PR #2):** GitHub Actions com os jobs `convencoes` (branch, commits e título do PR), `backend` (`./mvnw verify` no JDK 17), `frontend` (lint, testes, Prettier e build) e `ci-ok` consolidando. O passo `npm test` foi incluído no PR #19.

### DT-03 — Segredo JWT versionado e sessão sem revogação 🟡

**O que foi feito (PR #16):**
- O valor padrão do segredo foi removido. Sem `JWT_SECRET`, ou com menos de 32 caracteres, o backend não inicializa e explica o motivo no log.
- A validade do token caiu de 24 horas para 2 horas.

**O que continua pendente:**
- Refresh token, para o usuário não precisar entrar de novo a cada 2 horas.
- Revogação no logout (hoje o logout só apaga o token do navegador).
- Troca do `localStorage` por cookie `httpOnly`, para reduzir o impacto de XSS.

Esses três itens mudam a arquitetura de autenticação e devem ser feitos antes de qualquer exposição pública.

### DT-04 — Frontend sem testes automatizados ✅

**O que foi feito (PR #19):** Vitest 5 + Testing Library, script `npm test` e passo na CI. São 20 testes cobrindo login, rota protegida, redirecionamento em 401 (TC19), resgate com saldo 0 (TC10), resgate com modal (TC09), doação (TC05, TC06 e eliminatórios) e cadastro (TC03).

### DT-05 — Saldo de créditos sem reconciliação ✅

**O que foi feito (PR #18):**
- `GET /api/creditos/extrato` com saldo, totais de entradas e saídas e as transações.
- Extrato na tela **Meu perfil**.
- O `CreditoService` está documentado como único ponto que altera o saldo.
- O teste `ExtratoControllerTest` garante saldo = entradas − saídas, inclusive com resgates simultâneos.

### DT-06 — Telas divergem dos critérios de aceitação ✅

**O que foi feito (PR #15):** botão "Resgatar (1 Crédito)" desabilitado com saldo 0, com a mensagem oficial (TC10); modal de detalhes e confirmação do resgate (TC09 / US_02); mensagens oficiais de sucesso (TC05 / TC09); todos os campos inválidos destacados em vermelho (TC03 / TC06).

**Pendente:** a PO precisa confirmar os textos, que seguem a Especificação. A US_02 foi atendida com um modal, e não com uma página de detalhes separada.

---

## Débitos abertos

### DT-07 — Ficha de avaliação diverge da Especificação

- **Impacto:** Médio
- **Onde:** Especificação (RF06), coluna `livros.nota_estado`
- **Problema:** a Especificação pede que o usuário escolha de 1 a 5 estrelas e prevê uma coluna `estado_conservacao`. A equipe implementou a ficha de avaliação: 6 perguntas, nota = menor critério, eliminatórios recusam o livro. A coluna se chama `nota_estado`.
- **Consequência:** QA e PO validam contra um documento que não descreve mais o sistema, e a próxima equipe pode "corrigir" o código de volta.
- **Ação recomendada:** a PO registrar a ficha como regra oficial do RF06 e atualizar a Especificação e o Plano de Testes. Os casos novos já estão em [`docs/testes/casos-de-teste-n2.md`](../testes/casos-de-teste-n2.md).

### DT-08 — Conferência do sebo na entrega não existe

- **Impacto:** Alto
- **Onde:** `LivroService`, `CreditoService`
- **Problema:** a ficha diz que o sebo confere o livro e confirma na entrega. Hoje o crédito é liberado no cadastro, só com base no que o doador respondeu. Não há papel de sebo/admin nem status de "aguardando conferência".
- **Consequência:** um doador pode mentir no questionário e ganhar crédito, e o livro entra no acervo antes de alguém ver o estado real.
- **Ação recomendada:**
  - Criar o papel `SEBO`.
  - Criar o status `AGUARDANDO_CONFERENCIA`: o livro fica fora do acervo e o crédito fica pendente.
  - Criar uma tela onde o sebo confirma, corrige a nota ou recusa.
  - Liberar o crédito só na confirmação, via `CreditoService`.

### DT-09 — Livros antigos sem nota

- **Impacto:** Baixo
- **Onde:** `livros.nota_estado` (nulo), componente `Estrelas`
- **Problema:** os livros doados antes da avaliação aparecem sem estrelas (no modal, como "Sem avaliação").
- **Ação recomendada:** a PO decidir entre manter, pedir que o sebo avalie esses livros (depende do DT-08) ou escondê-los até terem nota.

### DT-10 — Gênero não é validado contra a lista fixa

- **Impacto:** Baixo
- **Onde:** `DoacaoRequest`, `frontend/src/constants/generos.js`
- **Problema:** o select da tela usa uma lista fixa, mas o backend aceita qualquer texto. Uma chamada direta à API grava um gênero que não aparece no filtro.
- **Ação recomendada:** validar no backend contra uma única lista (enum ou tabela) e expô-la para o frontend, como já é feito com o questionário.

### DT-11 — Acervo sem paginação e busca sem índice

- **Impacto:** Médio
- **Onde:** `LivroRepository`, `GET /api/livros`
- **Problema:** a listagem devolve todos os livros de uma vez, com busca por `LIKE` sem índice.
- **Consequência:** é o primeiro gargalo do RNF de listagem abaixo de 500 ms conforme o acervo crescer.
- **Ação recomendada:** paginar a API e a tela, e criar o índice numa migração `V3`.

### DT-12 — Usuário pode resgatar o próprio livro

- **Impacto:** Baixo
- **Onde:** `ResgateService`
- **Problema:** nada impede o doador de resgatar o livro que ele mesmo doou. A operação é neutra em créditos, mas confunde o histórico.
- **Ação recomendada:** a PO decidir. Se não for permitido, bloquear com 409 no `ResgateService` e esconder o botão no card do próprio doador.

### DT-13 — CORS restrito a localhost

- **Impacto:** Médio
- **Onde:** `SecurityConfig`
- **Problema:** só a origem `http://localhost:*` é liberada.
- **Consequência:** ao publicar o frontend em outro domínio, todas as chamadas são bloqueadas pelo navegador.
- **Ação recomendada:** ler as origens de uma variável de ambiente (`CORS_ORIGENS`), com `localhost` como padrão.

---

## Ordem sugerida para a próxima equipe

1. **DT-08** (conferência do sebo): fecha a regra da ficha de avaliação e impede crédito por questionário falso.
2. **DT-07** (Especificação): alinhar o documento antes de qualquer nova mudança no RF06.
3. **DT-03** (o que falta): obrigatório antes de qualquer exposição pública.
4. **DT-13** (CORS): obrigatório para publicar o frontend.
5. **DT-11** (paginação): antes do acervo crescer.
6. **DT-10**, **DT-12** e **DT-09**: ajustes pequenos, que podem acompanhar outras entregas.
