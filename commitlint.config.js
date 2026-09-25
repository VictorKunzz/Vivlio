// Convencao de commit do Vivlio — Conventional Commits 1.0.0.
// https://www.conventionalcommits.org/en/v1.0.0/
//
// A lista de tipos abaixo repete a do config-conventional de proposito: ela e a
// regra escrita da secao 6.2 do Acordo de Manutenibilidade, e quem abrir este
// arquivo precisa ver quais tipos valem sem ir procurar dentro da dependencia.
//
// Formato:  tipo(escopo opcional)!: descricao
// Exemplo:  feat(resgate): valida saldo de credito antes de confirmar
// Quebra:   feat(auth)!: exige JWT_SECRET na inicializacao
//           + rodape  BREAKING CHANGE: <explicacao>

module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'type-enum': [
      2,
      'always',
      [
        'feat', // nova funcionalidade
        'fix', // correcao de bug
        'docs', // documentacao
        'refactor', // muda o codigo sem mudar o comportamento externo
        'test', // testes
        'ci', // pipeline e automacao
        'build', // build e dependencias
        'chore', // manutencao que nao cabe nas outras
        'perf', // desempenho
        'style', // formatacao, sem efeito em codigo
        'revert', // reversao de commit
      ],
    ],
    // Descricao em portugues, comecando em minuscula e sem ponto final.
    'subject-case': [2, 'never', ['sentence-case', 'start-case', 'pascal-case', 'upper-case']],
    'subject-full-stop': [2, 'never', '.'],
    'subject-empty': [2, 'never'],
    'type-case': [2, 'always', 'lower-case'],
    'header-max-length': [2, 'always', 100],
  },
};
