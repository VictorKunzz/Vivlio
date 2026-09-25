# Vivlio — Plataforma de Troca de Livros Usados

> Sistema desenvolvido para promover a troca gratuita e sustentável de livros entre moradores de comunidades locais através de um sistema simplificado de créditos.

---

## Procedência

Este repositório continua o Vivlio entregue na N1 pelo Grupo Adriam em [Gusgb4/Vivlio](https://github.com/Gusgb4/Vivlio), recebido pela equipe Horangotandgo no handoff de 17/09/2026. O histórico completo foi migrado (mesmos commits, mesmas tags), e a partir daqui o desenvolvimento acontece neste repositório. O original permanece como referência somente-leitura.

Documentos do handoff em [`Docs/`](Docs/): guia de instalação e pontos de atenção, relatório de débitos técnicos, plano de testes e especificação de requisitos.

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

Detalhes, variáveis de ambiente, como rodar os testes e o caminho sem Docker em [`Docs/devops/ambiente-local.md`](Docs/devops/ambiente-local.md). Regras de branch, commit e Pull Request em [`CONTRIBUTING.md`](CONTRIBUTING.md).

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

| Nome | Papel / Função |
| :--- | :--- |
| **Ana** | Product Owner (PO) |
| **Adrian** | Engenheiro de Requisitos |
| **Daniela** | Quality Assurance (QA) |
| **Marcelo** | Desenvolvedor Frontend |
| **João** | Desenvolvedor Frontend |
| **Leonardo** | Desenvolvedor Backend |
| **Gustavo** | DevOps |

---

## 🚀 Funcionalidades (Escopo do Sistema)

### 🟢 Nível 1 — MVP (Fase Atual)
- [x] **Cadastro e Autenticação:** Registro e login de usuários.
- [x] **Acervo de Livros:** Listagem de livros disponíveis para troca.
- [x] **Cadastro de Doação:** Cadastro de novos livros no acervo (adiciona +1 crédito ao usuário).
- [x] **Solicitação de Troca/Resgate:** Resgate de livros utilizando o saldo de créditos (validação da RN Crítica).

### 🟡 Níveis 2 e 3 — Evoluções Futuras
- [ ] **Avaliação do Estado do Livro:** Classificação de conservação (1 a 5 estrelas).
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
│   ├── src/main/resources/   # application.properties
│   ├── src/test/java/        # Suíte JUnit
│   └── Dockerfile            # Imagem de desenvolvimento (JDK 17)
├── frontend/                 # Aplicação React + Vite
│   ├── src/components/       # Componentes reutilizáveis
│   ├── src/pages/            # Telas da aplicação (Login, Acervo, Doação, Perfil)
│   ├── src/services/         # Configuração de chamadas de API (Axios)
│   └── Dockerfile            # Imagem de desenvolvimento (Node 22)
├── Docs/                     # Requisitos, plano de testes, handoff, débitos técnicos
│   └── devops/               # Ambiente local e registro de configuração
├── docker-compose.yml        # Banco + backend + frontend
├── .env.example              # Variáveis de ambiente
├── commitlint.config.js      # Convenção de commit
├── CONTRIBUTING.md           # Branches, commits, Pull Request, CI
└── README.md
```
