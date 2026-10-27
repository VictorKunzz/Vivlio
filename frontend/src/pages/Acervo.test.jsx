import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import api from '../services/api';
import { USUARIO, renderizar } from '../test/renderizar';
import Acervo from './Acervo';

vi.mock('../services/api', () => ({
  default: { get: vi.fn(), post: vi.fn() },
}));

const LIVRO = {
  id: 7,
  titulo: 'Dom Casmurro',
  autor: 'Machado de Assis',
  genero: 'Romance',
  status: 'DISPONIVEL',
  doador: 'Bruno',
  nota: 4,
  sobrecapaAusente: true,
};

const MENSAGEM_SEM_CREDITO =
  'Você precisa de pelo menos 1 crédito ativo para resgatar um livro. Doe um livro para ganhar créditos.';

describe('Acervo', () => {
  beforeEach(() => {
    api.get.mockResolvedValue({ data: [LIVRO] });
  });

  it('TC10: com saldo 0 o resgate é bloqueado no frontend com a mensagem oficial', async () => {
    renderizar(<Acervo />, {
      auth: { usuario: { ...USUARIO, saldoCreditos: 0 } },
    });

    const botao = await screen.findByRole('button', {
      name: 'Resgatar (1 Crédito)',
    });
    expect(botao).toBeDisabled();
    expect(
      screen.getByText(MENSAGEM_SEM_CREDITO, { exact: false })
    ).toBeInTheDocument();

    await userEvent.click(botao);
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(api.post).not.toHaveBeenCalled();
  });

  it('TC09: com saldo, confirma no modal de detalhes e mostra a mensagem oficial', async () => {
    api.post.mockResolvedValue({ data: { livro: LIVRO, saldoCreditos: 0 } });
    const auth = renderizar(<Acervo />, { auth: { usuario: USUARIO } });

    await userEvent.click(
      await screen.findByRole('button', { name: 'Resgatar (1 Crédito)' })
    );

    const modal = screen.getByRole('dialog');
    expect(within(modal).getByText('Dom Casmurro')).toBeInTheDocument();
    expect(
      within(modal).getByRole('img', { name: 'Estado: 4 de 5 estrelas' })
    ).toBeInTheDocument();
    expect(within(modal).getByText('Ausente')).toBeInTheDocument();
    expect(api.post).not.toHaveBeenCalled();

    await userEvent.click(
      within(modal).getByRole('button', { name: 'Confirmar resgate' })
    );

    expect(api.post).toHaveBeenCalledWith('/api/resgates', { livroId: 7 });
    expect(
      await screen.findByText(
        'Resgate confirmado! Combine a entrega na biblioteca.'
      )
    ).toBeInTheDocument();
    expect(auth.atualizarSaldo).toHaveBeenCalledWith(0);
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('cancelar o modal não resgata', async () => {
    renderizar(<Acervo />, { auth: { usuario: USUARIO } });

    await userEvent.click(
      await screen.findByRole('button', { name: 'Resgatar (1 Crédito)' })
    );
    await userEvent.click(screen.getByRole('button', { name: 'Cancelar' }));

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(api.post).not.toHaveBeenCalled();
  });

  it('visitante vê o convite para entrar em vez do botão', async () => {
    renderizar(<Acervo />);

    expect(
      await screen.findByRole('link', { name: 'Entre para resgatar' })
    ).toBeInTheDocument();
    expect(
      screen.queryByRole('button', { name: 'Resgatar (1 Crédito)' })
    ).not.toBeInTheDocument();
  });
});
