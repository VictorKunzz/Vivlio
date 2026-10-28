import { screen, within } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import api from '../services/api';
import { USUARIO, renderizar } from '../test/renderizar';
import Perfil from './Perfil';

vi.mock('../services/api', () => ({
  default: { get: vi.fn() },
}));

const EXTRATO = {
  saldo: 1,
  totalEntradas: 1,
  totalSaidas: 0,
  transacoes: [
    {
      id: 1,
      tipo: 'ENTRADA',
      descricao: 'Doação de livro',
      data: '2026-10-27T17:05:12Z',
    },
  ],
};

function responder(doacoes) {
  api.get.mockImplementation((url) =>
    Promise.resolve({
      data: url === '/api/usuarios/me/doacoes' ? doacoes : EXTRATO,
    })
  );
}

describe('Perfil', () => {
  beforeEach(() => {
    responder([]);
  });

  it('lista os livros doados que ainda aguardam resgate', async () => {
    responder([
      { id: 7, titulo: 'Dom Casmurro', autor: 'Machado de Assis', nota: 3 },
      { id: 8, titulo: 'O Cortiço', autor: 'Aluísio Azevedo', nota: null },
    ]);
    renderizar(<Perfil />, { auth: { usuario: USUARIO } });

    const linha = (await screen.findByText('Dom Casmurro')).closest('tr');
    expect(within(linha).getByText('Machado de Assis')).toBeInTheDocument();
    expect(
      within(linha).getByRole('img', { name: 'Estado: 3 de 5 estrelas' })
    ).toBeInTheDocument();
    expect(screen.getByText('Sem avaliação')).toBeInTheDocument();
    expect(
      screen.getByText(/2 livros seus estão no acervo/)
    ).toBeInTheDocument();
    expect(api.get).toHaveBeenCalledWith('/api/usuarios/me/doacoes');
  });

  it('avisa quando não há livros aguardando resgate', async () => {
    renderizar(<Perfil />, { auth: { usuario: USUARIO } });

    expect(
      await screen.findByText(/Nenhum livro seu aguardando resgate/)
    ).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Doar um livro' })).toHaveAttribute(
      'href',
      '/doar'
    );
  });

  it('continua mostrando o extrato de créditos', async () => {
    renderizar(<Perfil />, { auth: { usuario: USUARIO } });

    expect(await screen.findByText('Doação de livro')).toBeInTheDocument();
    expect(
      screen.getByText(/1 entrada − 0 saídas = saldo de 1/)
    ).toBeInTheDocument();
  });
});
