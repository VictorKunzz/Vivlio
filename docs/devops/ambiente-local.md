# Ambiente local

## Caminho recomendado: Docker

```bash
cp .env.example .env
docker compose up
```

Sobe três serviços: PostgreSQL 16, backend na porta 8080 e frontend na 5173. Não é preciso ter
JDK, Maven ou Node instalados.

| Comando | Para que |
|---|---|
| `docker compose up` | Sobe tudo, com log no terminal |
| `docker compose up -d` | Sobe em background |
| `docker compose logs -f backend` | Acompanha o log de um serviço |
| `docker compose exec backend ./mvnw -B -ntp test` | Roda a suíte de testes do backend |
| `docker compose down` | Para tudo, mantendo o banco |
| `docker compose down -v` | Para tudo e **apaga o banco** |
| `docker compose build` | Reconstrói as imagens (necessário quando `pom.xml` ou `package.json` mudam) |

O `.env` é obrigatório: o compose se recusa a subir o backend sem `JWT_SECRET` definido, e o
`.env` é ignorado pelo git.

## Por que o ambiente é containerizado

Não é preferência. O `pom.xml` declara `<java.version>17</java.version>`, e o Spring Boot 3.3.5
resolve o Lombok 1.18.34, que não suporta JDK 23. Numa máquina com JDK 23 o processador de
anotações do Lombok falha **sem mensagem de erro própria**, e a compilação quebra com uma dúzia
de `cannot find symbol: method getId()` — parecendo código quebrado quando o problema é a
versão do JDK. Foi exatamente isso que aconteceu na primeira tentativa de build deste projeto
depois do handoff.

O Maven Wrapper (`mvnw`) já resolvia a versão do Maven. O container resolve a do Java e a do
Node. Com os dois, a versão de tudo que compila o projeto está declarada no repositório.

## Rodando sem Docker

Funciona, mas a responsabilidade pelas versões passa a ser sua:

- **JDK 17** (não 21, não 23). `java -version` precisa dizer 17.
- **Node 20.19+ ou 22.12+** (exigência do Vite 8).
- PostgreSQL 16 escutando em 5432, com banco, usuário e senha `vivlio`.

```bash
cd backend && ./mvnw spring-boot:run      # Windows: mvnw.cmd spring-boot:run
cd frontend && cp .env.example .env && npm install && npm run dev
```

Nunca `mvn` direto — o wrapper existe para que ninguém use a versão de Maven que instalou.

## Variáveis de ambiente

Todas têm padrão de desenvolvimento no `.env.example`, com uma exceção deliberada.

| Variável | Padrão | Observação |
|---|---|---|
| `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` | `vivlio` | Banco de desenvolvimento |
| `POSTGRES_PORT` | `5432` | Mude se já tiver um Postgres local ocupando a porta |
| `BACKEND_PORT` | `8080` | Porta publicada no host |
| `JPA_DDL_AUTO` | `update` | Ver a dívida DT-01 |
| `JPA_SHOW_SQL` | `false` | Log de SQL |
| `JWT_SECRET` | **nenhum** | Sem padrão de propósito: o compose falha se não for definido |
| `JWT_EXPIRACAO_MS` | `86400000` | 24 horas |
| `FRONTEND_PORT` | `5173` | Porta publicada no host |
| `VITE_API_URL` | `http://localhost:8080` | Quem chama a API é o navegador do host, por isso `localhost` e não `backend` |

Sobre o `JWT_SECRET`: a aplicação tem um valor padrão fixo versionado em
`application.properties` (dívida DT-03). O compose não replica esse padrão — ele exige o valor.
Isso não conserta a dívida, que precisa ser resolvida no código do backend, mas impede que o
caminho do Docker herde uma chave que está pública no repositório.

## Limitações conhecidas

**Recarregamento automático do frontend no Windows.** O código é montado por bind mount, e o
watcher do Vite às vezes não recebe evento de arquivo através de bind mount no Windows. Se
salvar e a tela não atualizar, recarregue o navegador. A correção definitiva é
`server.watch.usePolling` no `vite.config.js`, que é arquivo do dono do frontend.

**Backend não recarrega sozinho.** `spring-boot:run` não tem devtools configurado. Mudança em
Java exige `docker compose restart backend`.

**Primeiro build é lento.** Baixa as imagens base, o Maven 3.9.9 e todas as dependências.
Depois disso o cache do Maven fica num volume nomeado e os builds seguintes são rápidos.

## O que foi removido

`backend/docker-compose.yml`, que subia apenas o PostgreSQL e vivia dentro de uma das pastas do
monorepo. O compose agora é um só, na raiz, e orquestra os três serviços.
