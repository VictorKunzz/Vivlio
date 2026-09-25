# Registro de configuração do repositório

Este documento existe porque configuração feita na interface do GitHub não vem no `git clone`.
O Vivlio já passou de uma equipe para outra uma vez, e vai passar de novo. Quem receber o
projeto precisa saber o que está ligado, por quê, e como reproduzir.

Toda mudança na configuração do repositório entra aqui. Responsável: DevOps.

## Repositório

| Item | Valor |
|---|---|
| Endereço | `github.com/VictorKunzz/Vivlio` |
| Origem | `github.com/Gusgb4/Vivlio`, herdado no handoff de 17/09/2026 |
| Visibilidade | Público |
| Branch padrão | `main` |

A equipe Horangotandgo não tinha permissão no repositório de origem — acesso de leitura, sem
push e sem admin. Sem repositório próprio o papel de DevOps não existe na prática: não dá para
proteger branch, ligar Actions, aprovar PR nem gerenciar colaborador.

A migração foi por **mirror**, não fork, e preservou os 28 commits da N1, as 8 branches e a tag
`1.0.0` (`main` em `233114b`, `develop` em `ae2ccad`, SHAs idênticos aos da origem).

Fork foi descartado por dois motivos concretos: em repositório que é fork o Pull Request aponta
por padrão para o upstream, e com cinco pessoas abrindo PR alguém acabaria mandando trabalho
para o repositório da equipe anterior; e o GitHub Actions vem desabilitado por padrão em fork,
que é justamente o que precisava ser montado.

Como o vínculo de fork não aparece na interface, a procedência está registrada no `README.md`.

**Público de propósito:** rulesets e proteção de branch são gratuitos em repositório público e
exigem plano pago em privado. O código já era público na origem, então não houve exposição nova.

## Visibilidade é escolha, não descuido

O repositório é público, então **qualquer pessoa lê tudo que entra**. Isso torna a regra de
segredos não negociável: nenhuma credencial, chave ou token em código, workflow ou documento.

## Estratégia de merge

| Configuração | Valor | Motivo |
|---|---|---|
| Squash merge | Habilitado | Entrada em `develop`: uma linha por entrega |
| Merge commit | Habilitado | Subida `develop` → `main`: preserva o que foi entregue junto |
| Rebase merge | **Desabilitado** | Uma terceira forma só cria divergência de histórico |
| Título do squash | `PR_TITLE` | O padrão era `COMMIT_OR_PR_TITLE`, que num PR de commit único usava a mensagem do commit em vez do título validado pelo commitlint |
| Corpo do squash | `PR_BODY` | O contexto do PR fica no histórico |
| Apagar branch no merge | Habilitado | Evita acumular o que foi limpo no handoff |

```bash
gh api -X PATCH repos/VictorKunzz/Vivlio \
  -F delete_branch_on_merge=true \
  -F allow_rebase_merge=false \
  -f squash_merge_commit_title=PR_TITLE \
  -f squash_merge_commit_message=PR_BODY
```

## Colaboradores

Permissão `push`, não `admin`: abrem branch e PR, aprovam revisão, e não alteram proteção,
segredo ou workflow. Admin fica só com o DevOps, que responde pela seção 6 do Acordo.

| GitHub | Integrante | Papel |
|---|---|---|
| `@VictorKunzz` | Viktor Kunz | DevOps (owner) |
| `@gustavotaques` | Gustavo Taques | Product Owner |
| `@VynicyusCandido` | Vynicyus Cândido | Engenheiro de Requisitos |
| `@MRanderle` | Alisson Anderle | QA |
| `@joaopedroangelico` | João Pedro Angélico | Dev Frontend |
| `@Luis-Fernando-Pereira` | Luis Fernando Pereira | Dev Backend |

## Proteção de branch

`main` e `develop` protegidas por ruleset, com o mesmo conjunto de regras:

| Regra | Efeito |
|---|---|
| Pull Request obrigatório | Sem push direto |
| 1 aprovação | De outro desenvolvedor |
| Revisão de code owner | O dono da área precisa aprovar (ver `.github/CODEOWNERS`) |
| Aprovação descartada em push novo | O que foi aprovado é o que entra |
| Conversas resolvidas | Comentário de revisão não fica pendurado |
| Check `ci-ok` obrigatório | Merge bloqueado com pipeline vermelho |
| Bloqueio de force push | Histórico não é reescrito |
| Bloqueio de deleção | `main` e `develop` não somem |

**Sem bypass, inclusive para o administrador.** Regra que o autor burla morre em duas semanas.
A consequência aceita conscientemente é que PR de infraestrutura espera aprovação de um colega.

**Sem exigência de histórico linear:** seria incompatível com o merge commit de `develop` → `main`.

`ci-ok` é o único check exigido de propósito. Ele consolida os outros três jobs, então
acrescentar ou renomear job no workflow não obriga a mexer na proteção de branch.

## Integração contínua

Workflow em `.github/workflows/ci.yml`, disparado em Pull Request e push para `develop` e `main`.

| Job | Verifica |
|---|---|
| `convencoes` | Nome da branch, mensagens de commit e título do PR |
| `backend` | `./mvnw -B -ntp verify` em Java 17 Temurin |
| `frontend` | `npm ci`, lint, `prettier --check`, build em Node 22 |
| `ci-ok` | Consolida os três |

Decisões que valem estar escritas:

- **`permissions: contents: read`** declarado no topo. O padrão do GitHub é mais amplo do que uma
  verificação precisa.
- **`pull_request`, nunca `pull_request_target`.** O segundo roda com o token do repositório base
  e acesso a segredo sobre código ainda não revisado.
- **Sem filtro de caminho.** Filtro deixa check obrigatório pendurado em "expected" e trava o PR.
  O projeto é pequeno o bastante para rodar tudo sempre.
- **Versões fixadas, nunca herdadas do runner.** Ver a seção seguinte.
- **Nenhum job usa segredo.** O backend testa com H2 e o frontend só faz lint e build.

## Por que as versões são fixadas

O `pom.xml` declara Java 17. O Spring Boot 3.3.5 resolve o Lombok 1.18.34, que não suporta
JDK 23: com um JDK mais novo o processador de anotações falha **sem mensagem própria**, e a
compilação quebra com uma dúzia de `cannot find symbol: method getId()` — parecendo código
quebrado quando o problema é o ambiente. Foi o que aconteceu na primeira tentativa de build
depois do handoff.

Node é fixado em 22 porque o Vite 8 exige 20.19+ ou 22.12+.

O Maven nunca é instalado: vem do wrapper versionado (`mvnw`, Maven 3.9.9). Nunca usar `mvn`.

## Normalização de fim de linha

`.gitattributes` na raiz. Não é detalhe de estilo:

- Sem ele, o `core.autocrlf` do Windows tratava **PDF e DOCX como texto**. O
  `Docs/Vivlio - Handoff.pdf` ficava com 16205 bytes no working tree contra 16037 no
  repositório — 168 bytes de CR injetados. O git escondia o problema porque o filtro desfazia a
  conversão ao comparar, então `git status` aparecia limpo com o arquivo corrompido em disco.
- `mvnw` com CRLF numa imagem Linux falha com `bad interpreter`.
- `prettier --check` acusa todo o frontend como fora de formato.

`mvnw.cmd` e `*.bat` continuam em CRLF, que é o que o interpretador de lote do Windows espera.

## Segredos

Nenhum segredo cadastrado no repositório. Nenhum job precisa de um.

O `JWT_SECRET` tem valor padrão fixo versionado em `application.properties` — dívida **DT-03**,
herdada. O `docker-compose.yml` não replica esse padrão: ele exige a variável e se recusa a
subir o backend sem ela. Isso não paga a dívida, que precisa ser resolvida no código do backend,
mas impede que o caminho do Docker herde uma chave que está pública.

Se um segredo vazar num commit, **trocar o segredo é obrigatório**. Reescrever o histórico é
opcional e vem depois: o histórico já foi clonado por todo mundo.

## Não adotado, e por quê

| Item | Motivo |
|---|---|
| Secret scanning com push protection | Gratuito em repo público e recomendado. Ficou fora desta rodada por decisão de escopo |
| Dependabot | Gera PR de atualização toda semana, que alguém precisa revisar. Adiado |
| Ambiente de deploy (CD) | Não existe ambiente publicado nem conversa sobre isso |
| Análise estática pesada (SonarQube) | Citado como exemplo no Acordo; custo de configurar e manter não se paga no tamanho deste projeto |
| Matriz de versões no CI | A equipe roda uma versão de Java e uma de Node. Matriz multiplicaria tempo de build sem pegar nada |
| Build de imagem no CI | Os Dockerfiles são de desenvolvimento. Sem ambiente de deploy, não há o que publicar |

## Histórico de mudanças

| Data | Mudança |
|---|---|
| 24/09/2026 | Repositório criado por mirror; 6 branches herdadas fora do padrão removidas, todas já mescladas |
| 25/09/2026 | Ambiente containerizado do monorepo e `.gitattributes` (PR #1) |
| 25/09/2026 | Pipeline de CI e commitlint (PR #2) |
| 25/09/2026 | Estratégia de merge ajustada: squash com `PR_TITLE`, rebase desabilitado, branch apagada no merge |
