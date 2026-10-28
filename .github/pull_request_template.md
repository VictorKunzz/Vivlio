<!--
Titulo do PR no formato Conventional Commits: tipo(escopo): descricao
Ele vira a mensagem do commit permanente, porque o merge em develop e por squash.
-->

## O que muda

<!-- Em uma ou duas frases. O que alguem veria de diferente depois deste PR. -->

## Por que

<!-- O card, o requisito, o debito tecnico (DT-xx) ou o item do Acordo que originou.
     Se nao rastreia a nada, explique por que a mudanca e necessaria mesmo assim. -->

## Como verificar

<!-- Passos concretos para quem revisa reproduzir. Comandos, rota, dado de entrada.
     "Testei localmente" nao e um passo. -->

## Checklist

- [ ] Nome da branch no padrao Conventional Branch (`tipo/descricao-em-kebab-case`)
- [ ] Mensagens de commit no padrao Conventional Commits
- [ ] Verificado na minha maquina antes de abrir
- [ ] CI verde
- [ ] Documentacao atualizada, se a mudanca muda uma regra ou o jeito de rodar o projeto
- [ ] Nenhum segredo, credencial ou `.env` no diff
- [ ] Atalho tecnico assumido virou issue de debito tecnico, com `// DEBT:` vinculado
