import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthContext } from '../context/AuthContext';
import api from '../services/api';
import Acervo from './Acervo';

vi.mock('../services/api', () => ({
  default: { get: vi.fn(), post: vi.fn() },
}));

const domCasmurro = {
  id: 1,
  titulo: 'Dom Casmurro',
  autor: 'Machado de Assis',
  genero: 'Romance',
  status: 'DISPONIVEL',
  doador: 'Ana Souza',
};

function renderizar(usuario, atualizarSaldo = vi.fn()) {
  render(
    <AuthContext.Provider value={{ usuario, atualizarSaldo }}>
      <MemoryRouter>
        <Acervo />
      </MemoryRouter>
    </AuthContext.Provider>
  );
  return { atualizarSaldo };
}

async function cardDoLivro(titulo) {
  const cabecalho = await screen.findByRole('heading', { name: titulo });
  return within(cabecalho.closest('li'));
}

describe('Acervo', () => {
  beforeEach(() => {
    api.get.mockResolvedValue({ data: [domCasmurro] });
  });

  it('TC15 - lista os livros devolvidos pela API', async () => {
    renderizar(null);

    const card = await cardDoLivro('Dom Casmurro');
    expect(card.getByText('Machado de Assis')).toBeInTheDocument();
    expect(card.getByText('Doado por Ana Souza')).toBeInTheDocument();
    expect(api.get).toHaveBeenCalledWith('/api/livros');
  });

  it('visitante vê o convite para entrar em vez do botão de resgate', async () => {
    renderizar(null);

    const card = await cardDoLivro('Dom Casmurro');
    expect(
      card.getByRole('link', { name: 'Entre para resgatar' })
    ).toBeInTheDocument();
    expect(
      card.queryByRole('button', { name: 'Resgatar' })
    ).not.toBeInTheDocument();
  });

  it('TC09 - resgate com saldo debita o crédito e tira o livro da lista', async () => {
    api.post.mockResolvedValue({
      data: {
        livro: { ...domCasmurro, status: 'RESGATADO' },
        saldoCreditos: 0,
      },
    });
    const { atualizarSaldo } = renderizar({ nome: 'Bruno', saldoCreditos: 1 });

    const card = await cardDoLivro('Dom Casmurro');
    await userEvent.click(card.getByRole('button', { name: 'Resgatar' }));

    expect(api.post).toHaveBeenCalledWith('/api/resgates', { livroId: 1 });
    expect(atualizarSaldo).toHaveBeenCalledWith(0);
    expect(await screen.findByText(/Resgate concluído/)).toBeInTheDocument();
    expect(
      screen.queryByRole('heading', { name: 'Dom Casmurro' })
    ).not.toBeInTheDocument();
  });

  it('com saldo 0 avisa que é preciso doar e exibe a recusa da API', async () => {
    const mensagem =
      'Você precisa de pelo menos 1 crédito ativo para resgatar um livro. Doe um livro para ganhar créditos.';
    api.post.mockRejectedValue({
      response: { status: 409, data: { mensagem } },
    });
    const { atualizarSaldo } = renderizar({ nome: 'Bruno', saldoCreditos: 0 });

    expect(
      await screen.findByText(/Você está sem créditos/)
    ).toBeInTheDocument();

    const card = await cardDoLivro('Dom Casmurro');
    await userEvent.click(card.getByRole('button', { name: 'Resgatar' }));

    expect(await screen.findByText(mensagem)).toBeInTheDocument();
    expect(atualizarSaldo).not.toHaveBeenCalled();
    expect(
      screen.getByRole('heading', { name: 'Dom Casmurro' })
    ).toBeInTheDocument();
  });

  it.todo(
    'TC10 - com saldo 0 o resgate é bloqueado no frontend, sem chamar a API (aguarda decisão do DT-06, #11)'
  );
});
