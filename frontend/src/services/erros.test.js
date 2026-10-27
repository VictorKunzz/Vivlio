import { describe, expect, it } from 'vitest';
import { mensagemDeErro } from './erros';

describe('mensagemDeErro', () => {
  it('avisa que o servidor está fora do ar quando não há resposta', () => {
    expect(mensagemDeErro(new Error('Network Error'))).toBe(
      'Não foi possível falar com o servidor. Verifique se o backend está no ar.'
    );
  });

  it('usa a primeira mensagem de campo nos erros de validação', () => {
    const erro = {
      response: {
        data: {
          mensagem: 'Existem campos obrigatórios ou inválidos no formulário.',
          campos: {
            titulo: 'O título é obrigatório.',
            autor: 'O autor é obrigatório.',
          },
        },
      },
    };

    expect(mensagemDeErro(erro)).toBe('O título é obrigatório.');
  });

  it('usa a mensagem de negócio devolvida pela API', () => {
    const erro = {
      response: { data: { mensagem: 'E-mail ou senha inválidos.' } },
    };

    expect(mensagemDeErro(erro)).toBe('E-mail ou senha inválidos.');
  });

  it('usa a mensagem padrão quando a API não devolve corpo', () => {
    expect(mensagemDeErro({ response: { status: 500 } })).toBe(
      'Não foi possível concluir a operação. Tente novamente.'
    );
    expect(mensagemDeErro({ response: {} }, 'Falhou.')).toBe('Falhou.');
  });
});
