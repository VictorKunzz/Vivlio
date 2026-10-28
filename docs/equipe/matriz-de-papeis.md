# Matriz de Papéis — Equipe Horangotango

Atualizada para a entrega da **N2 (v2.0)**. O papel indica quem responde pela área (ver "Quem decide o que" no [CONTRIBUTING](../../CONTRIBUTING.md) e o [CODEOWNERS](../../.github/CODEOWNERS)); ele não limita quem pode executar atividades daquela área.

> **Como preencher:** as evidências abaixo foram levantadas no GitHub (PRs, issues, commits e revisões). Atividades fora do repositório (reuniões, Jira/Project, pitch, testes manuais, alinhamentos com a PO) precisam ser registradas por cada integrante nos campos marcados com ✏️.

## Papéis

| Integrante | GitHub | Papel | Área no repositório |
|---|---|---|---|
| Gustavo Taques | [@gustavotaques](https://github.com/gustavotaques) | Product Owner (PO) | `docs/` (DoR, DoD, prioridades) |
| Vynicyus Cândido | [@VynicyusCandido](https://github.com/VynicyusCandido) | Engenheiro de Requisitos | `docs/` (requisitos, débitos técnicos) |
| João Pedro Angélico | [@joaopedroangelico](https://github.com/joaopedroangelico) | Dev Frontend e QA | `/frontend/`, `docs/testes/` |
| Luis Fernando Pereira | [@Luis-Fernando-Pereira](https://github.com/Luis-Fernando-Pereira) | Dev Backend | `/backend/` |
| Victor Kunz | [@VictorKunzz](https://github.com/VictorKunzz) | DevOps | `.github/`, Docker, `docs/devops/`, convenções |

## Histórico de contribuições na N2

### João Pedro Angélico — Dev Frontend e QA

| Atividade | Evidência |
|---|---|
| **Evolução da N2:** avaliação do estado do livro (questionário de 6 critérios, nota = menor critério, eliminatórios, estrelas no acervo), backend e frontend | [PR #13](https://github.com/VictorKunzz/Vivlio/pull/13) (merged) · [issue #14](https://github.com/VictorKunzz/Vivlio/issues/14) |
| DT-06: telas alinhadas aos critérios de aceitação (TC03, TC05, TC06, TC09, TC10) e modal de resgate | [PR #15](https://github.com/VictorKunzz/Vivlio/pull/15) |
| DT-03: `JWT_SECRET` obrigatório e token de 2h | [PR #16](https://github.com/VictorKunzz/Vivlio/pull/16) |
| DT-01: migrações Flyway e `ddl-auto=validate` | [PR #17](https://github.com/VictorKunzz/Vivlio/pull/17) |
| DT-05: extrato de créditos no perfil | [PR #18](https://github.com/VictorKunzz/Vivlio/pull/18) |
| DT-04: testes de frontend (Vitest), consolidando os testes do #12 | [PR #19](https://github.com/VictorKunzz/Vivlio/pull/19) |
| Documentação da N2: DER, débitos, handoff, casos de teste, Postman; consolidação do #5 | [PR #20](https://github.com/VictorKunzz/Vivlio/pull/20) |
| Merge dos PRs da N2 | Merge de [#16](https://github.com/VictorKunzz/Vivlio/pull/16), [#17](https://github.com/VictorKunzz/Vivlio/pull/17), [#19](https://github.com/VictorKunzz/Vivlio/pull/19), [#5](https://github.com/VictorKunzz/Vivlio/pull/5) e [#7](https://github.com/VictorKunzz/Vivlio/pull/7) |
| **QA:** casos de teste da N2 (AV, EX, SEC) e rastreabilidade do Plano de Testes com os testes automatizados | [`casos-de-teste-n2.md`](../testes/casos-de-teste-n2.md) · [Plano de Testes, seção 5.1](../testes/Plano_de_Testes_Sebo_Comunitario.md) |
| **QA:** verificação com todos os PRs integrados: 53 testes de backend, 29 de frontend, Postman 45/45 em banco vazio, roteiros de tela TC03/05/06/09/10 | Descrição do [PR #20](https://github.com/VictorKunzz/Vivlio/pull/20) |
| **QA:** correção da TC11 da coleção do Postman, que falhava desde a N1 | [PR #20](https://github.com/VictorKunzz/Vivlio/pull/20) |
| Registro dos débitos DT-07 a DT-13 e atualização do GitHub Project | Issues [#21](https://github.com/VictorKunzz/Vivlio/issues/21) a [#27](https://github.com/VictorKunzz/Vivlio/issues/27) |
| Pitch da N2 | PPTX entregue |
| ✏️ Roteiro manual da QA (TC10, TC11, TC14, AV03, EX02, EX03) e outras atividades | ✏️ |

Os PRs #13 e #15 a #20 foram desenvolvidos com apoio do Claude Code (assistente de IA), registrado como coautor nos commits.

### Luis Fernando Pereira — Dev Backend

| Atividade | Evidência |
|---|---|
| Revisão e aprovação da evolução da N2 | Revisão e merge do [PR #13](https://github.com/VictorKunzz/Vivlio/pull/13) |
| Merge das correções da N2 | Merge de [#15](https://github.com/VictorKunzz/Vivlio/pull/15) (DT-06) e [#18](https://github.com/VictorKunzz/Vivlio/pull/18) (DT-05) |
| Registro dos débitos técnicos herdados como issues | [#6](https://github.com/VictorKunzz/Vivlio/issues/6), [#8](https://github.com/VictorKunzz/Vivlio/issues/8), [#9](https://github.com/VictorKunzz/Vivlio/issues/9), [#10](https://github.com/VictorKunzz/Vivlio/issues/10), [#11](https://github.com/VictorKunzz/Vivlio/issues/11) |
| Template de issue para débito técnico | [PR #7](https://github.com/VictorKunzz/Vivlio/pull/7) |
| Testes de frontend (DT-04): `AuthProvider` e `erros` incorporados ao #19 | [PR #12](https://github.com/VictorKunzz/Vivlio/pull/12) (fechado, consolidado) → [#19](https://github.com/VictorKunzz/Vivlio/pull/19) (coautor) |
| Revisão em markdown da Especificação, do Plano de Testes e do README do frontend | [PR #5](https://github.com/VictorKunzz/Vivlio/pull/5) (merged), atualizada para a v2.0 no [#20](https://github.com/VictorKunzz/Vivlio/pull/20) (coautor) |
| ✏️ Outras atividades | |

### Victor Kunz — DevOps

| Atividade | Evidência |
|---|---|
| Migração do repositório da equipe anterior com o histórico completo | [README, seção Procedência](../../README.md#procedência) |
| Ambiente de desenvolvimento containerizado (PostgreSQL, backend, frontend) | [PR #1](https://github.com/VictorKunzz/Vivlio/pull/1) |
| Pipeline de CI com validação de convenções (resolveu o DT-02) | [PR #2](https://github.com/VictorKunzz/Vivlio/pull/2) |
| Convenções de branch/commit, CODEOWNERS e registro de configuração | [PR #3](https://github.com/VictorKunzz/Vivlio/pull/3) |
| GitHub Project da equipe | [Horangotango - Vivlio (2° Projeto)](https://github.com/users/VictorKunzz/projects/4) |
| ✏️ Outras atividades | |

### Vynicyus Cândido — Engenheiro de Requisitos

| Atividade | Evidência |
|---|---|
| ✏️ Requisitos da N2 e política de débito técnico | ✏️ |
| ✏️ Outras atividades | ✏️ |

### Gustavo Taques — Product Owner (PO)

| Atividade | Evidência |
|---|---|
| ✏️ Definição da evolução da N2 e da ficha de avaliação | ✏️ |
| ✏️ DoR/DoD e priorização do backlog | ✏️ |
| ✏️ Outras atividades | ✏️ |

## Pendências que dependem dos papéis

| Pendência | Responsável |
|---|---|
| Aprovar os textos do DT-06 e registrar a ficha de avaliação como regra do RF06 (DT-07, #21) | PO (Gustavo) |
| Decidir DT-09 (#23, livros sem nota) e DT-12 (#26, resgate do próprio livro) | PO (Gustavo) |
| Executar o roteiro manual da N2 e registrar o resultado | QA (João) |
| Revisar e fazer merge do PR #20 (#15 a #19 já entraram) | Devs |
| PR `develop → main`, tag e release `v2.0.0` | DevOps (Victor) |
