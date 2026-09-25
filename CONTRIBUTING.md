# Guia de Contribuição — Vivlio

Regras de versionamento, commits, revisão e qualidade que todo integrante segue — e quem
receber o projeto depois também. Corresponde à seção 6 do Acordo de Manutenibilidade.

O que este documento promete, o pipeline verifica. Regra que não é verificada não é regra, é
recomendação.

## Estratégia de branches

Segue a especificação [Conventional Branch](https://conventionalbranch.org/).

`main` e `develop` são branches tronco e não levam prefixo:

- **main** — código estável, pronto para entrega. Protegida: sem push direto.
- **develop** — integração da entrega atual. Protegida: sem push direto.

As demais seguem `tipo/descricao-em-kebab-case`:

| Prefixo | Para que |
|---|---|
| `feature/` | Nova funcionalidade |
| `fix/` | Correção de bug |
| `hotfix/` | Correção urgente |
| `chore/` | Manutenção, configuração, documentação, dependência |
| `release/` | Preparação de entrega (`release/v1.2.0`) |

Regras da descrição: só `a-z`, `0-9` e hífen separando palavras. Sem maiúscula, sem espaço, sem
underscore, sem hífen duplicado, sem hífen no começo ou no fim. Ponto só em número de versão de
`release/`. Número de card pode entrar.

```
feature/resgate-de-livro          fix/saldo-negativo-no-resgate
chore/pipeline-ci                 feature/scrum-42-avaliacao-livro
```

Errado: `Feature/Resgate`, `feat/troca_credito`, `daniela`, `minhas-alterações`.

Fluxo: `feature/*` ou `fix/*` → Pull Request → `develop` → (após validação do QA) → Pull
Request → `main`. Toda branch nasce de `develop`, exceto `hotfix/`, que nasce de `main`.

## Padrão de commits

Segue a especificação [Conventional Commits 1.0.0](https://www.conventionalcommits.org/en/v1.0.0/).

```
tipo(escopo opcional)!: descrição

corpo opcional

rodapé opcional
```

| Tipo | Para que |
|---|---|
| `feat` | Nova funcionalidade |
| `fix` | Correção de bug |
| `docs` | Documentação |
| `refactor` | Muda o código sem mudar o comportamento externo (manutenção preventiva) |
| `test` | Testes |
| `ci` | Pipeline e automação |
| `build` | Build e dependências |
| `chore` | Manutenção que não cabe nas outras |
| `perf` | Desempenho |
| `style` | Formatação, sem efeito em código |
| `revert` | Reversão de commit |

Regras:

- Descrição em português, **começando em minúscula** e **sem ponto final**.
- Tipo e escopo em inglês e minúsculo.
- Cabeçalho de no máximo 100 caracteres.
- Um commit resolve uma coisa. Commit que mistura pipeline, correção e formatação não dá para
  reverter sozinho.
- Sem `wip`, `ajustes`, `teste`, `alterações`. Mensagem que não diz nada custa caro no dia em
  que algo quebrar e alguém precisar achar onde.

Escopos sugeridos, opcionais: `backend`, `frontend`, `ci`, `docs`, `auth`, `resgate`, `credito`,
`livro`, `deps`.

```
feat(resgate): valida saldo de credito antes de confirmar
fix(auth): corrige expiracao do token no refresh da pagina
ci: adiciona verificacao de lint e testes no pull request
docs: registra convencao de branches e commits
```

Mudança incompatível leva `!` antes dos dois-pontos e rodapé `BREAKING CHANGE:` explicando o
que quebra e o que fazer:

```
feat(auth)!: exige JWT_SECRET definido na inicializacao

BREAKING CHANGE: a aplicacao nao sobe mais com a chave padrao. Defina JWT_SECRET
no ambiente antes de iniciar.
```

## Pull Request

- Toda mudança entra por PR. Nem eu, nem ninguém, dá push direto em `main` ou `develop`.
- **1 aprovação** de outro desenvolvedor antes do merge, sem exceção para quem administra o
  repositório.
- **CODEOWNERS**: mudança em `backend/` precisa da revisão do dono do backend, em `frontend/` do
  dono do frontend. O GitHub pede automaticamente.
- Todos os checks do CI verdes. O check obrigatório é o `ci-ok`.
- Conversas de revisão resolvidas antes do merge.
- Aprovação é descartada quando chega commit novo — o que foi aprovado é o que vai entrar.

O título do PR segue o mesmo formato do commit, porque o merge por squash usa o título do PR
como mensagem do commit permanente.

A descrição responde três coisas: o que muda, por que muda (o débito técnico, o requisito ou o
card que originou) e como verificar. O template de PR já vem com esse formato.

PR grande não é revisado, é aprovado no impulso. Quando não der para quebrar, avise o revisor de
onde olhar.

### Estratégia de merge

- `feature/*` e `fix/*` → `develop`: **squash**. O histórico de `develop` fica uma linha por
  entrega.
- `develop` → `main`: **merge commit**. Preserva o que foi entregue junto.
- Rebase está desabilitado, para não haver uma terceira forma.
- A branch é apagada automaticamente depois do merge.

## Verificação automática

O workflow `.github/workflows/ci.yml` roda a cada Pull Request para `develop` e `main`:

| Job | O que verifica |
|---|---|
| `convencoes` | Nome da branch, mensagens de commit e título do PR contra as duas especificações |
| `backend` | `./mvnw -B -ntp verify` — compila e roda a suíte JUnit em Java 17 |
| `frontend` | `npm ci`, `npm run lint`, `prettier --check`, `npm run build` em Node 22 |
| `ci-ok` | Consolida os três. É o único check obrigatório na proteção de branch |

Rodar antes de abrir o PR evita ida e volta:

```bash
docker compose exec backend ./mvnw -B -ntp verify
cd frontend && npm run lint && npx prettier --check . && npm run build
```

As versões de Java e Node são fixadas no workflow e nos Dockerfiles, nunca herdadas da máquina.
O motivo está documentado em [`Docs/devops/ambiente-local.md`](Docs/devops/ambiente-local.md):
o Lombok que este projeto resolve não funciona em JDK 23, e falha sem dizer por quê.

Formatação: `npm run format` **escreve** nos arquivos e serve para uso local. O CI usa
`prettier --check`, que só acusa.

## Ambiente de desenvolvimento

`cp .env.example .env && docker compose up`. Detalhes, variáveis de ambiente e o caminho sem
Docker em [`Docs/devops/ambiente-local.md`](Docs/devops/ambiente-local.md).

Nunca commite `.env`. Nunca coloque credencial, chave ou token em código, em workflow ou em
documento. Se um segredo vazar num commit, trocar o segredo é obrigatório — o histórico já foi
clonado por todo mundo.

## Débito técnico

Atalho técnico assumido no caminho vira issue de débito técnico, no formato que o Engenheiro de
Requisitos definiu na seção 2 do Acordo. Marcador no código (`// DEBT:`) sempre com a issue
vinculada.

Os cinco débitos herdados da equipe anterior estão em `Docs/Vivlio - Debitos Tecnicos.docx`.

## Quem decide o que

| Área | Dono |
|---|---|
| Repositório, branches, commits, PR, CI/CD, ambientes, segredos | DevOps |
| Código e padrões do frontend | Dev Frontend |
| Código e padrões do backend e guia de estilo | Dev Backend |
| DoR e DoD | Product Owner |
| Estratégia de testes e registro de bug | QA |
| Requisitos e política de débito técnico | Engenheiro de Requisitos |

Dúvida numa área que não é sua: pergunte ao dono, não preencha a lacuna.
