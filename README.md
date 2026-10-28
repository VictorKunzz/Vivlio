# Vivlio — Plataforma de Troca de Livros Usados

> Sistema desenvolvido para promover a troca gratuita e sustentável de livros entre moradores de comunidades locais através de um sistema simplificado de créditos.

---

## Procedência

Este repositório continua o Vivlio entregue na N1 pelo Grupo Adriam em [Gusgb4/Vivlio](https://github.com/Gusgb4/Vivlio), recebido pela equipe Horangotandgo no handoff de 17/09/2026. O histórico completo foi migrado (mesmos commits, mesmas tags), e a partir daqui o desenvolvimento acontece neste repositório. O original permanece como referência somente-leitura.

Documentação em [`docs/`](docs/README.md): [handoff](docs/handoff/README.md), [DER](docs/DER/README.md), [débitos técnicos](docs/debitos-tecnicos/README.md), [casos de teste](docs/testes/casos-de-teste-n2.md) e especificação de requisitos.

**Versão atual: `v2.0` (N2).**

---

## Sobre o Projeto

Bibliotecas e feiras comunitárias frequentemente enfrentam dificuldades para precificar livros usados e controlar o fluxo de trocas. O **Sebo Comunitário** resolve esse problema implementando a equivalência por **créditos de doação**:
* **Doou 1 livro** ➔ Ganha **1 crédito**.
* **1 crédito** ➔ Dá direito a **resgatar 1 livro** do acervo.

---

## Regra de Negócio Crítica (RN Crítica)

> **Regra:** Um usuário só pode solicitar o resgate de um livro se possuir **pelo menos 1 crédito ativo** em sua conta.

---

## Como rodar

```bash
cp .env.example .env
docker compose up
```

Frontend em `http://localhost:5173`, API em `http://localhost:8080`. Não é preciso ter JDK, Maven ou Node instalados — as versões vêm fixadas nas imagens.

Testes:

```bash
cd backend  && ./mvnw verify   # JUnit + migrações Flyway no H2
cd frontend && npm test        # Vitest + Testing Library
```

Detalhes, variáveis de ambiente e o caminho sem Docker em [`docs/devops/ambiente-local.md`](docs/devops/ambiente-local.md). Regras de branch, commit e Pull Request em [`CONTRIBUTING.md`](CONTRIBUTING.md).

---

## Tecnologias Utilizadas

### Frontend
* **React** (v18+)
* **Vite** (Build tool e servidor de desenvolvimento)
* **React Router DOM** (Navegação SPA)
* **Axios / Fetch API** (Consumo da API REST)

### Backend
* **Java 17+ / Spring Boot**
* **Spring Data JPA** (Persistência e ORM)
* **Spring Security / JWT** (Autenticação e Autorização)

### Banco de Dados & Infraestrutura
* **PostgreSQL** (Banco de dados relacional)
* **Docker / Docker Compose** (Ambiente de desenvolvimento do BD)

---

## 👥 Equipe e Responsabilidades

Equipe Horangotango (N2):

| Nome | Papel / Função |
| :--- | :--- |
| **Gustavo Taques** | Product Owner (PO) |
| **Vynicyus Cândido** | Engenheiro de Requisitos |
| **João Pedro Angélico** | Quality Assurance (QA) e Desenvolvedor Frontend |
| **Luis Fernando Pereira** | Desenvolvedor Backend |
| **Victor Kunz** | DevOps |

Contribuições de cada integrante na N2: [Matriz de Papéis](docs/equipe/matriz-de-papeis.md).

<details>
<summary>Equipe da N1 (Grupo Adriam, autora do MVP)</summary>

| Nome | Papel / Função |
| :--- | :--- |
| **Ana** | Product Owner (PO) |
| **Adrian** | Engenheiro de Requisitos |
| **Daniela** | Quality Assurance (QA) |
| **Marcelo** | Desenvolvedor Frontend |
| **João** | Desenvolvedor Frontend |
| **Leonardo** | Desenvolvedor Backend |
| **Gustavo** | DevOps |

</details>

---

## 🚀 Funcionalidades (Escopo do Sistema)

### 🟢 Nível 1 — MVP (Fase Atual)
- [x] **Cadastro e Autenticação:** Registro e login de usuários.
- [x] **Acervo de Livros:** Listagem de livros disponíveis para troca.
- [x] **Cadastro de Doação:** Cadastro de novos livros no acervo (adiciona +1 crédito ao usuário).
- [x] **Solicitação de Troca/Resgate:** Resgate de livros utilizando o saldo de créditos (validação da RN Crítica).

### 🟢 Nível 2 — Entregue na N2 (`v2.0`)
- [x] **Avaliação do Estado do Livro:** questionário de 6 critérios (capa, lombada, páginas, marcações, manchas, odor). A nota de 1 a 5 é a **menor** entre eles, calculada no servidor. Livros com mofo, página faltando e outros eliminatórios são recusados e não geram crédito. As estrelas aparecem no acervo.
- [x] **Extrato de Créditos:** histórico de entradas e saídas no perfil, com saldo = entradas − saídas.
- [x] **Correções:** resgate bloqueado na tela com saldo 0, modal de confirmação, mensagens oficiais e destaque de campos (DT-06); `JWT_SECRET` obrigatório (DT-03); migrações Flyway (DT-01); testes de frontend (DT-04). Situação completa no [relatório de débitos](docs/debitos-tecnicos/README.md).

### 🟡 Nível 3 — Evoluções Futuras
- [ ] **Conferência do sebo na entrega:** confirmação do estado antes de liberar o crédito (DT-08).
- [ ] **Clubes de Leitura Locais:** Criação e gestão de grupos comunitários de discussão e leitura.

---

## 📁 Estrutura do Repositório

```text
Vivlio/
├── .github/
│   ├── workflows/ci.yml      # Pipeline de verificação
│   ├── CODEOWNERS            # Quem revisa cada área
│   └── pull_request_template.md
├── backend/                  # Aplicação Java Spring Boot
│   ├── src/main/java/        # Controllers, Services, Repositories, Entities
│   ├── src/main/resources/   # application.properties e db/migration (Flyway)
│   ├── src/test/java/        # Suíte JUnit
│   ├── postman/              # Coleção de requisições da API
│   └── Dockerfile            # Imagem de desenvolvimento (JDK 17)
├── frontend/                 # Aplicação React + Vite
│   ├── src/components/       # Componentes reutilizáveis
│   ├── src/pages/            # Telas da aplicação (Login, Acervo, Doação, Perfil)
│   ├── src/services/         # Configuração de chamadas de API (Axios)
│   ├── src/**/*.test.jsx     # Testes Vitest
│   └── Dockerfile            # Imagem de desenvolvimento (Node 22)
├── docs/
│   ├── DER/                  # Modelo de dados e migrações
│   ├── debitos-tecnicos/     # Situação de cada débito técnico
│   ├── handoff/              # Guia para a equipe que recebe o projeto
│   ├── requisitos/           # Especificação de requisitos e DoR
│   ├── testes/               # Plano de testes e casos da N2
│   └── devops/               # Ambiente local e registro de configuração
├── docker-compose.yml        # Banco + backend + frontend
├── .env.example              # Variáveis de ambiente
├── commitlint.config.js      # Convenção de commit
├── CONTRIBUTING.md           # Branches, commits, Pull Request, CI
└── README.md
```
