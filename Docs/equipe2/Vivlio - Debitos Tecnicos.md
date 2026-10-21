# Vivlio — Relatório de Débitos Técnicos

> Revisão de outubro/2026 (equipe 2). A versão original da equipe anterior está preservada em
> `Docs/documentação_defasada/`. Alterações: linha "Situação" em cada débito, conferida com o código da
> develop em outubro/2026 (DT-02 resolvido pelo pipeline de CI, DT-03 mitigado no caminho Docker, demais
> em aberto) e inclusão do DT-06.

## 1. Contexto

O MVP (Nível 1) do Vivlio está concluído de ponta a ponta: backend Spring Boot com cadastro e autenticação, acervo, doação com crédito ao usuário e resgate validando a regra de negócio crítica (mínimo de 1 crédito ativo); e frontend React com as telas de Login, Cadastro, Acervo, Doação e Perfil, rota protegida e cliente Axios integrado à API.

As próximas entregas previstas são a Avaliação do Estado do Livro (classificação de 1 a 5 estrelas) e os Clubes de Leitura Locais. Este documento lista os seis débitos técnicos que mais impactam essas entregas e a ação recomendada para cada um.

## 2. Débitos Técnicos Priorizados

### DT-01 — Schema do banco sem versionamento (ddl-auto=update)

| | |
| :--- | :--- |
| **Impacto** | Alto |
| **Onde** | `backend/src/main/resources/application.properties` |
| **Problema** | O schema é gerado pelo Hibernate com `spring.jpa.hibernate.ddl-auto=update`. Não existe Flyway, Liquibase nem scripts SQL versionados no repositório. |
| **Consequência** | As duas próximas entregas exigem mudanças estruturais: coluna de avaliação em livros e novas tabelas de clubes e participantes. Sem migração versionada, cada ambiente evolui de forma diferente, alterações destrutivas não são aplicadas e não há caminho de rollback. |
| **Ação recomendada** | Adotar Flyway, gerar o script de baseline a partir do schema atual e usar `ddl-auto=validate` nos ambientes compartilhados. |
| **Situação** | Aberto. |

### DT-02 — Ausência de pipeline de integração contínua

| | |
| :--- | :--- |
| **Impacto** | Alto |
| **Onde** | Repositório (não existe `.github/workflows`) |
| **Problema** | O CONTRIBUTING.md exige aprovação em Pull Request e verificação de ESLint/Prettier sem erros críticos, mas nada disso roda automaticamente. A suíte de testes do backend (autenticação, livros, doação, resgate e concorrência) só é executada na máquina de quem lembra de rodá-la. |
| **Consequência** | Com backend e frontend já integrados, uma regressão na regra crítica de créditos pode chegar à develop sem ser percebida. A verificação de qualidade depende da disciplina manual de cada revisor. |
| **Ação recomendada** | Criar workflow no GitHub Actions rodando `mvn test` no backend e `npm run lint` e `npm run build` no frontend a cada Pull Request, marcando o job como obrigatório para o merge. |
| **Situação** | **Resolvido (out/2026).** `.github/workflows/ci.yml` roda a cada Pull Request para `develop` e `main`: convenções de branch e commit, `./mvnw -B -ntp verify` no backend e `npm run lint`, `prettier --check` e `npm run build` no frontend. O `ci-ok` é o check obrigatório para o merge. Os testes de frontend ainda não existem (DT-04) e entram no pipeline quando forem criados. |

### DT-03 — Segredo JWT versionado e sessão sem revogação

| | |
| :--- | :--- |
| **Impacto** | Alto |
| **Onde** | `backend/.../application.properties`, `auth/service/JwtService.java`, `frontend/src/services/api.js` |
| **Problema** | A chave de assinatura do JWT tem valor padrão fixo dentro do repositório, usado sempre que a variável `JWT_SECRET` não é definida. O token vale 24 horas, é guardado em localStorage pelo frontend e não há refresh token nem lista de revogação — o logout apenas remove o token do navegador. |
| **Consequência** | Quem tiver acesso ao repositório consegue assinar tokens válidos em qualquer ambiente que use o padrão, e um token vazado continua utilizável até expirar. O armazenamento em localStorage amplia o impacto de uma eventual falha de XSS. |
| **Ação recomendada** | Remover o valor padrão e tornar `JWT_SECRET` obrigatório na inicialização, reduzir o tempo de expiração e planejar refresh token com revogação antes da exposição pública da plataforma. |
| **Situação** | **Mitigado em parte (out/2026).** No caminho Docker o `docker-compose.yml` exige `JWT_SECRET` no `.env` e não usa o padrão. O valor padrão continua em `application.properties` e vale para quem roda sem Docker; expiração, armazenamento e revogação do token não mudaram. |

### DT-04 — Frontend sem nenhum teste automatizado

| | |
| :--- | :--- |
| **Impacto** | Médio-Alto |
| **Onde** | `frontend/package.json` (sem Vitest, Testing Library ou E2E) |
| **Problema** | O frontend não tem framework de teste instalado nem script "test". Regras visíveis ao usuário — bloqueio de resgate sem crédito, atualização do saldo após o resgate, redirecionamento em caso de 401 e proteção de rotas — são validadas apenas manualmente. |
| **Consequência** | O Plano de Testes do projeto fica coberto só do lado do backend. As telas de avaliação do livro e de clubes de leitura vão nascer sem rede de proteção, e cada regressão de interface só aparece em teste manual. |
| **Ação recomendada** | Adicionar Vitest com Testing Library, cobrir primeiro os fluxos de login, resgate sem crédito e rota protegida, e incluir o script "test" no pipeline do DT-02. |
| **Situação** | Aberto. |

### DT-05 — Saldo de créditos duplicado, sem reconciliação

| | |
| :--- | :--- |
| **Impacto** | Médio |
| **Onde** | `usuario/entity/Usuario.java`, `credito/service/CreditoService.java` |
| **Problema** | O saldo existe em dois lugares: o campo `saldo_creditos` na tabela de usuários e o histórico em `transacoes_credito`. O CreditoService atualiza os dois, mas nada garante que sigam coerentes, e não há endpoint de extrato nem rotina de conferência. |
| **Consequência** | Uma falha parcial ou uma futura operação que altere o saldo sem registrar a transação gera divergência silenciosa justamente na regra de negócio crítica do sistema, sem trilha para auditar. O usuário vê o saldo no Perfil, mas não consegue conferir de onde ele veio. |
| **Ação recomendada** | Expor um endpoint de extrato de créditos e exibi-lo no Perfil, cobrir a consistência entre saldo e transações com teste automatizado e centralizar toda alteração de saldo exclusivamente no CreditoService. |
| **Situação** | Aberto. |

### DT-06 — Telas divergem dos critérios de aceitação

| | |
| :--- | :--- |
| **Impacto** | Médio |
| **Onde** | `frontend/src/pages/Doacao.jsx`, `frontend/src/pages/Acervo.jsx`, `frontend/src/services/erros.js` |
| **Problema** | Parte das mensagens e comportamentos da interface não segue o Documento de Especificação nem o Plano de Testes. **Doação:** a tela exibe *"'&lt;título&gt;' entrou no acervo. Seu saldo agora é de N."* em vez de *"Livro cadastrado! Você ganhou 1 crédito."* (TC05). **Resgate:** a tela exibe *"Resgate concluído. O livro '&lt;título&gt;' é seu. Saldo atual: N."* em vez de *"Resgate confirmado! Combine a entrega na biblioteca."* (TC09); o botão "Resgatar" fica habilitado com saldo 0 e o bloqueio só acontece quando a API responde 409 (o TC10 exige bloqueio no frontend); não existe página de detalhes do livro nem modal de confirmação (US_02). **Formulários:** campos vazios são barrados pelo bloqueio nativo do navegador (atributo `required`), sem destaque em vermelho, e quando a API recusa só a primeira mensagem de erro é exibida (TC03/TC06). |
| **Consequência** | Os casos TC03, TC05, TC06, TC09 e TC10 falham na execução manual do roteiro mesmo com o backend correto. O TC10 é de prioridade Crítica, então o critério de saída da N1 não é atendido enquanto isso não for resolvido. Sem uma decisão registrada, a equipe pode ajustar o documento para refletir a tela (ou o contrário) sem aval do PO. |
| **Ação recomendada** | Levar ao PO a decisão sobre quais textos valem. Em seguida, ajustar as telas: desabilitar "Resgatar" com saldo 0 exibindo a mensagem oficial, padronizar as mensagens de sucesso e destacar todos os campos inválidos devolvidos pela API em `campos`. Cobrir esses fluxos com os testes do DT-04. |
| **Situação** | Aberto. |

## 3. Ordem Sugerida de Tratamento

1. **DT-01 (migrações)** — pré-requisito estrutural para avaliação de livros e clubes de leitura.
2. **DT-02 (CI)** — protege a regra crítica de créditos contra regressões daí em diante. *Concluído.*
3. **DT-03 (JWT)** — obrigatório antes de qualquer exposição pública da plataforma.
4. **DT-04 (testes de frontend)** — deve acompanhar o início das telas de Nível 2.
5. **DT-05 (créditos)** — melhoria de auditoria, pode entrar junto com a entrega de Nível 2.
6. **DT-06 (aderência aos critérios)** — tratar junto com o DT-04, para que os testes de frontend já nasçam validando as mensagens oficiais.
