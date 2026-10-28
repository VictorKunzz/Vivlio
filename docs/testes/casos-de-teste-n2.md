# Casos de teste da N2

Complemento ao [Plano de Testes](Plano_de_Testes_Sebo_Comunitario.md) (TC01 a TC19). Aqui estão os casos das funcionalidades da N2 e os casos da N1 cujo resultado esperado mudou.

**Onde cada caso está automatizado:**
- **B**: backend, JUnit (`cd backend && ./mvnw verify`)
- **F**: frontend, Vitest (`cd frontend && npm test`)
- **P**: coleção do Postman (`backend/postman/Vivlio.postman_collection.json`)

## Avaliação do estado do livro (RF06)

Regra: o doador responde 6 critérios (capa, lombada, páginas, marcações, manchas, odor). A nota final é a **menor** entre eles. Qualquer eliminatório recusa o livro.

| ID | Cenário | Passos | Resultado esperado | Prior. | Auto. |
|---|---|---|---|---|---|
| AV01 | Exemplo da ficha | Doar com capa 4, lombada 5, páginas 3, marcações 4, manchas 5, odor 5 | Livro salvo com **nota 3**; +1 crédito | Alta | B, P |
| AV02 | Defeito grave não é compensado | Todos os critérios perfeitos, páginas "rasgos que atingem o texto" | Nota **1** | Alta | B |
| AV03 | Eliminatório recusa o livro | Marcar "Tem mofo" | Tela: perguntas somem, aviso aparece e o botão trava. API: **422**, livro não salvo, nenhum crédito | Crítica | B, F, P |
| AV04 | Didático com exercícios respondidos | Todos perfeitos + "didático com exercícios respondidos" | Nota **2** (máximo) | Média | B, P |
| AV05 | Sobrecapa ausente | Todos perfeitos + "sobrecapa se perdeu" | Nota **5**; "Sobrecapa: Ausente" no modal de resgate | Baixa | B, F |
| AV06 | Questionário incompleto | Enviar sem responder um critério | Tela: pergunta destacada em vermelho. API: **400** com `campos["avaliacao.odor"]` | Média | B, F |
| AV07 | Doação sem questionário (API) | `POST /api/livros` sem `avaliacao` | **400** com `campos.avaliacao` | Média | B, P |
| AV08 | Opção inexistente (API) | `"capa": "CINCO_ESTRELAS"` | **400** com mensagem amigável, nada salvo | Baixa | B |
| AV09 | Notas não são expostas | `GET /api/avaliacao/questionario` | 6 critérios e 6 eliminatórios, nenhuma opção com campo `nota` | Média | B, P |
| AV10 | Estrelas no acervo | Abrir o Acervo depois de AV01 | Card com 3 de 5 estrelas | Média | — |

## Extrato de créditos (DT-05)

| ID | Cenário | Passos | Resultado esperado | Prior. | Auto. |
|---|---|---|---|---|---|
| EX01 | Extrato exige login | `GET /api/creditos/extrato` sem token | **401** | Alta | B, P |
| EX02 | Saldo bate com o histórico | 3 doações e 2 resgates | saldo 1, 3 entradas, 2 saídas, 5 transações (mais recente primeiro) | Crítica | B, P |
| EX03 | Consistência sob concorrência | 2 resgates simultâneos com saldo 1 | 1 resgate conclui; saldo 0 = 1 entrada − 1 saída | Crítica | B |
| EX04 | Extrato na tela | Abrir **Meu perfil** | Tabela com data no horário local, descrição e +1/−1; resumo "entradas − saídas = saldo" | Média | — |

## Segurança (DT-03)

| ID | Cenário | Passos | Resultado esperado | Prior. | Auto. |
|---|---|---|---|---|---|
| SEC01 | Backend sem segredo | Iniciar sem `JWT_SECRET` | Não inicializa; log: `JWT_SECRET não definido...` | Alta | B |
| SEC02 | Segredo fraco | `JWT_SECRET` com menos de 32 caracteres | Não inicializa; log explica o mínimo | Alta | B |
| SEC03 | Token expirado | Usar token com mais de 2 horas | **401**; o frontend apaga o token e volta ao login (TC19) | Média | B, F |

## Livros em doação no perfil

| ID | Cenário | Passos | Resultado esperado | Prior. | Auto. |
|---|---|---|---|---|---|
| PF01 | Exige login | `GET /api/usuarios/me/doacoes` sem token | **401** | Alta | B |
| PF02 | Só os livros do usuário ainda disponíveis | Doar 2 livros, ter 1 doado já resgatado e outro usuário com 1 livro | A lista traz só os 2 disponíveis do usuário, do mais recente ao mais antigo | Alta | B |
| PF03 | Seção no perfil | Abrir **Meu perfil** | Tabela "Livros em doação" com título, autor e estrelas; sem livros: aviso com link para doar | Média | F |

## Casos da N1 com resultado esperado atualizado (DT-06)

| ID | O que mudou | Auto. |
|---|---|---|
| TC03 | Todos os campos vazios ficam em vermelho, com uma mensagem por campo | F |
| TC05 | Mensagem: "Livro cadastrado! Você ganhou 1 crédito." + nota do estado. A doação exige o questionário | B, F, P |
| TC06 | Título, autor, gênero e as perguntas sem resposta ficam em vermelho. A API devolve todos os campos inválidos | B, F, P |
| TC09 | "Resgatar (1 Crédito)" abre um modal com os detalhes do livro. Depois de confirmar: "Resgate confirmado! Combine a entrega na biblioteca." | F, P |
| TC10 | Com saldo 0, o botão fica desabilitado e a mensagem oficial aparece. Nenhuma chamada à API | F |

## Como rodar a coleção do Postman

A coleção supõe um **banco vazio**: ela cadastra `teste@vivlio.com` e `segundo@vivlio.com` e resgata o livro de id 1.

```bash
docker compose down -v && docker compose up -d
npx newman run backend/postman/Vivlio.postman_collection.json
```
