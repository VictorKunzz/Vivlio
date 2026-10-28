import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import api from '../services/api';
import { USUARIO, renderizar } from '../test/renderizar';
import Doacao from './Doacao';

vi.mock('../services/api', () => ({
  default: { get: vi.fn(), post: vi.fn() },
}));

function criterio(campo, pergunta, codigo, descricao) {
  return { campo, pergunta, opcoes: [{ codigo, descricao }] };
}

const QUESTIONARIO = {
  criterios: [
    criterio('capa', 'Como está a capa?', 'SEM_DESGASTE', 'Capa perfeita.'),
    criterio('lombada', 'Como está a lombada?', 'RETA', 'Lombada reta.'),
    criterio('paginas', 'Como estão as páginas?', 'SEM_DOBRAS', 'Páginas ok.'),
    criterio('marcacoes', 'Tem marcações?', 'NENHUMA', 'Sem marcações.'),
    criterio('manchas', 'Tem manchas?', 'NENHUMA', 'Sem manchas.'),
    criterio('odor', 'Tem cheiro?', 'NENHUM', 'Sem cheiro.'),
  ],
  eliminatorios: [{ codigo: 'MOFO', descricao: 'Tem mofo.' }],
};

const RESPOSTAS = [
  'Capa perfeita.',
  'Lombada reta.',
  'Páginas ok.',
  'Sem marcações.',
  'Sem manchas.',
  'Sem cheiro.',
];

async function abrirFormulario() {
  const auth = renderizar(<Doacao />, { auth: { usuario: USUARIO } });
  await screen.findByText('Como está a capa?');
  return auth;
}

async function preencherTudo() {
  await userEvent.type(screen.getByLabelText(/^Título/), 'Dom Casmurro');
  await userEvent.type(screen.getByLabelText(/^Autor/), 'Machado de Assis');
  await userEvent.selectOptions(screen.getByLabelText(/^Gênero/), 'Romance');
  await userEvent.click(
    screen.getByLabelText('Não tem nenhum destes problemas.')
  );
  for (const descricao of RESPOSTAS) {
    await userEvent.click(screen.getByLabelText(descricao));
  }
}

function enviar() {
  return userEvent.click(
    screen.getByRole('button', { name: 'Cadastrar doação' })
  );
}

describe('Doação', () => {
  beforeEach(() => {
    api.get.mockResolvedValue({ data: QUESTIONARIO });
  });

  it('TC06: envio vazio destaca os campos e as perguntas e não chama a API', async () => {
    await abrirFormulario();

    await enviar();

    for (const campo of ['Título', 'Autor', 'Gênero']) {
      expect(screen.getByLabelText(new RegExp(`^${campo}`))).toHaveAttribute(
        'aria-invalid',
        'true'
      );
    }
    expect(screen.getAllByText('Escolha uma opção.')).toHaveLength(6);
    expect(
      screen.getByText(
        'Marque os problemas do livro ou confirme que ele não tem nenhum.'
      )
    ).toBeInTheDocument();
    expect(
      screen.getByText('Corrija os campos destacados em vermelho.')
    ).toBeInTheDocument();
    expect(api.post).not.toHaveBeenCalled();
  });

  it('exige confirmar que não há problemas, mesmo com o resto preenchido', async () => {
    await abrirFormulario();

    await preencherTudo();
    await userEvent.click(
      screen.getByLabelText('Não tem nenhum destes problemas.')
    );
    await enviar();

    expect(
      screen.getByText(
        'Marque os problemas do livro ou confirme que ele não tem nenhum.'
      )
    ).toBeInTheDocument();
    expect(api.post).not.toHaveBeenCalled();
  });

  it('"nenhum problema" e os eliminatórios se excluem', async () => {
    await abrirFormulario();
    const nenhum = screen.getByLabelText('Não tem nenhum destes problemas.');
    const mofo = screen.getByLabelText('Tem mofo.');

    await userEvent.click(nenhum);
    expect(nenhum).toBeChecked();

    await userEvent.click(mofo);
    expect(mofo).toBeChecked();
    expect(nenhum).not.toBeChecked();

    await userEvent.click(nenhum);
    expect(nenhum).toBeChecked();
    expect(mofo).not.toBeChecked();
    expect(screen.getByText('Como está a capa?')).toBeInTheDocument();
  });

  it('TC05: doação completa mostra a mensagem oficial e envia o questionário', async () => {
    api.post.mockResolvedValue({
      data: { livro: { titulo: 'Dom Casmurro', nota: 5 }, saldoCreditos: 2 },
    });
    const auth = await abrirFormulario();

    await preencherTudo();
    await enviar();

    expect(
      await screen.findByText(/Livro cadastrado! Você ganhou 1 crédito\./)
    ).toBeInTheDocument();
    expect(api.post).toHaveBeenCalledWith(
      '/api/livros',
      expect.objectContaining({
        titulo: 'Dom Casmurro',
        avaliacao: expect.objectContaining({
          capa: 'SEM_DESGASTE',
          odor: 'NENHUM',
          eliminatorios: [],
        }),
      })
    );
    expect(auth.atualizarSaldo).toHaveBeenCalledWith(2);
  });

  it('eliminatório marcado esconde as perguntas e trava o envio', async () => {
    await abrirFormulario();

    await userEvent.click(screen.getByLabelText('Tem mofo.'));

    expect(screen.queryByText('Como está a capa?')).not.toBeInTheDocument();
    expect(
      screen.getByRole('button', { name: 'Cadastrar doação' })
    ).toBeDisabled();
  });

  it('destaca todos os campos que a API recusar, não só o primeiro', async () => {
    api.post.mockRejectedValue({
      response: {
        status: 400,
        data: {
          mensagem: 'Existem campos inválidos.',
          campos: { titulo: 'Título inválido.', autor: 'Autor inválido.' },
        },
      },
    });
    await abrirFormulario();

    await preencherTudo();
    await enviar();

    expect(await screen.findByText('Título inválido.')).toBeInTheDocument();
    expect(screen.getByText('Autor inválido.')).toBeInTheDocument();
    expect(screen.getByLabelText(/^Título/)).toHaveAttribute(
      'aria-invalid',
      'true'
    );
    expect(screen.getByLabelText(/^Autor/)).toHaveAttribute(
      'aria-invalid',
      'true'
    );
  });
});
