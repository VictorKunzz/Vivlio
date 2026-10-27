# Vivlio — Frontend

Interface web do Vivlio, plataforma comunitária de troca de livros usados por créditos. Consome a API
do `backend/` deste repositório.

**Stack:** React 19, Vite 8, React Router 7 e Axios. Lint com ESLint e formatação com Prettier.

## Como rodar

**Com Docker (recomendado).** Na raiz do repositório, `cp .env.example .env && docker compose up`
sobe banco, API e frontend, sem precisar de Node instalado. O código de `src/`, `public/` e
`index.html` é montado no container; quando o `package.json` mudar, rode `docker compose build`.
Detalhes e limitações conhecidas em `Docs/devops/ambiente-local.md`.

**Sem Docker.** Precisa de Node.js 20.19+ ou 22.12+ (exigência do Vite 8) e da API rodando. Nesta
pasta:

```bash
cp .env.example .env
npm install
npm run dev
```

Nos dois casos a aplicação sobe em `http://localhost:5173`.

| Variável | Padrão | Descrição |
| :--- | :--- | :--- |
| `VITE_API_URL` | `http://localhost:8080` | Endereço da API, chamado pelo navegador. Se não for definida, o cliente usa o mesmo padrão. No Docker vem do `.env` da raiz. |

A API só aceita chamadas de origens `http://localhost:*` (CORS em `SecurityConfig`). Para publicar em
outro domínio, a origem precisa ser liberada no backend.

## Scripts

| Comando | O que faz |
| :--- | :--- |
| `npm run dev` | Servidor de desenvolvimento com recarga automática |
| `npm run build` | Build de produção em `dist/` |
| `npm run preview` | Serve o build de produção localmente |
| `npm run lint` | ESLint (precisa passar sem erros críticos para o PR ser aprovado) |
| `npm run format` | Prettier em todo o projeto |

O CI roda `npm run lint`, `npx prettier --check .` e `npm run build` a cada Pull Request; rode os
três antes de abrir o PR. Não há testes automatizados no frontend (DT-04 no Relatório de Débitos
Técnicos).

## Telas e rotas

| Rota | Tela | Acesso |
| :--- | :--- | :--- |
| `/login` | Login | Público |
| `/cadastro` | Cadastro de usuário | Público |
| `/` e `/acervo` | Acervo com busca, filtro por gênero e botão de resgate | Público (resgate exige login) |
| `/doar` | Cadastro de doação | Logado |
| `/perfil` | Dados do usuário e saldo de créditos | Logado |
| qualquer outra | Página não encontrada | Público |

## Estrutura

```
src/
├── App.jsx                  rotas
├── main.jsx                 ponto de entrada (Router + AuthProvider)
├── index.css                estilos globais e variáveis de cor
├── components/
│   ├── Layout.jsx           cabeçalho, menu, saldo e rodapé
│   └── RotaProtegida.jsx    redireciona para /login quem não está logado
├── context/
│   ├── AuthContext.js
│   └── AuthProvider.jsx     sessão: login, cadastro, logout e saldo em memória
├── hooks/
│   └── useAuth.js           acesso ao contexto de autenticação
├── services/
│   ├── api.js               cliente Axios: envia o token e trata 401
│   └── erros.js             extrai a mensagem de erro da resposta da API
├── constants/
│   └── generos.js           lista de gêneros do formulário e do filtro
└── pages/                   Acervo, Cadastro, Doacao, Login, NaoEncontrada, Perfil
```

## Sessão e autenticação

- O token JWT devolvido por `/api/auth/login` ou `/api/auth/registrar` é salvo em `localStorage`
  (chave `vivlio_token`) e enviado em toda requisição como `Authorization: Bearer <token>`.
- Ao abrir a aplicação com um token salvo, o `AuthProvider` carrega o usuário em `/api/usuarios/me`.
- Qualquer resposta 401 fora de `/api/auth` apaga o token e redireciona para `/login`.
- Depois de doar ou resgatar, o saldo exibido é atualizado com o `saldoCreditos` da resposta, sem
  recarregar o perfil.

## Erros da API

As telas mostram a mensagem devolvida pela API. Em erros de validação a API envia `campos`, e
`services/erros.js` exibe só a primeira mensagem (os campos não são destacados; ver DT-06). Sem
resposta do servidor, a tela informa que o backend pode estar fora do ar.

O contrato completo da API está em `Docs/equipe2/Vivlio - Handoff.md`, seção 6.2.
