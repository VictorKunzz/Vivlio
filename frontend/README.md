# Vivlio — Frontend

Interface web do Vivlio, plataforma comunitária de troca de livros usados por créditos. Consome a API
do `backend/` deste repositório.

**Stack:** React 19, Vite 8, React Router 7 e Axios. Lint com ESLint, formatação com Prettier e testes com Vitest + Testing Library.

## Como rodar

**Com Docker (recomendado).** Na raiz do repositório, `cp .env.example .env && docker compose up`
sobe banco, API e frontend, sem precisar de Node instalado. O código de `src/`, `public/` e
`index.html` é montado no container; quando o `package.json` mudar, rode `docker compose build`.
Detalhes e limitações conhecidas em [`docs/devops/ambiente-local.md`](../docs/devops/ambiente-local.md).

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
| `npm test` | Roda os testes uma vez (Vitest) |
| `npm run test:watch` | Testes em modo observação |

O CI roda `npm run lint`, `npm test`, `npx prettier --check .` e `npm run build` a cada Pull Request.
Rode os quatro antes de abrir o PR.

## Telas e rotas

| Rota | Tela | Acesso |
| :--- | :--- | :--- |
| `/login` | Login | Público |
| `/cadastro` | Cadastro de usuário | Público |
| `/` e `/acervo` | Acervo com busca, filtro por gênero, estrelas do estado e resgate com modal de confirmação | Público (resgate exige login) |
| `/doar` | Cadastro de doação com o questionário de avaliação | Logado |
| `/perfil` | Dados do usuário, saldo e extrato de créditos | Logado |
| qualquer outra | Página não encontrada | Público |

## Estrutura

```
src/
├── App.jsx                  rotas
├── main.jsx                 ponto de entrada (Router + AuthProvider)
├── index.css                estilos globais e variáveis de cor
├── components/
│   ├── Estrelas.jsx         nota do estado do livro (1 a 5)
│   ├── Layout.jsx           cabeçalho, menu, saldo e rodapé
│   ├── ModalResgate.jsx     detalhes do livro e confirmação do resgate
│   └── RotaProtegida.jsx    redireciona para /login quem não está logado
├── context/
│   ├── AuthContext.js
│   └── AuthProvider.jsx     sessão: login, cadastro, logout e saldo em memória
├── hooks/
│   └── useAuth.js           acesso ao contexto de autenticação
├── services/
│   ├── api.js               cliente Axios: envia o token e trata 401
│   ├── erros.js             extrai a mensagem e os campos de erro da resposta da API
│   └── navegacao.js         redirecionamento para /login fora do React Router
├── constants/
│   └── generos.js           lista de gêneros do formulário e do filtro
├── pages/                   Acervo, Cadastro, Doacao, Login, NaoEncontrada, Perfil
└── test/                    setup do Vitest e helper renderizar()

Os testes ficam ao lado do arquivo testado (`*.test.jsx`).
```

## Sessão e autenticação

- O token JWT (válido por 2 horas) devolvido por `/api/auth/login` ou `/api/auth/registrar` é salvo em `localStorage`
  (chave `vivlio_token`) e enviado em toda requisição como `Authorization: Bearer <token>`.
- Ao abrir a aplicação com um token salvo, o `AuthProvider` carrega o usuário em `/api/usuarios/me`.
- Qualquer resposta 401 fora de `/api/auth` apaga o token e redireciona para `/login`.
- Depois de doar ou resgatar, o saldo exibido é atualizado com o `saldoCreditos` da resposta, sem
  recarregar o perfil.

## Erros da API

As telas mostram a mensagem devolvida pela API. Em erros de validação a API envia `campos`, e os
formulários de cadastro e doação destacam **todos** os campos recusados, com a mensagem de cada um
(`errosDosCampos` em `services/erros.js`). Os mesmos formulários validam antes de enviar. Sem
resposta do servidor, a tela informa que o backend pode estar fora do ar.

## Regras de tela importantes

- **Resgate sem crédito (TC10):** com saldo 0 o botão "Resgatar (1 Crédito)" fica desabilitado e a
  mensagem oficial aparece; nenhuma chamada é feita à API. O backend também bloqueia (TC11).
- **Avaliação do livro:** as perguntas e opções vêm de `GET /api/avaliacao/questionario`. A tela
  nunca mostra nem envia a nota; ela é calculada no servidor. Marcar um eliminatório esconde as
  perguntas e trava o envio.

Os endpoints e o contrato dos endpoints novos estão no [handoff](../docs/handoff/README.md#61-endpoints-da-api), seções 6.1 e 6.2.
