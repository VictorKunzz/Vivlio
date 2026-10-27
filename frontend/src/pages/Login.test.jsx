import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { renderizar } from '../test/renderizar';
import Login from './Login';

describe('Login', () => {
  it('entra com e-mail e senha e vai para o acervo', async () => {
    const entrar = vi.fn().mockResolvedValue(undefined);
    renderizar(<Login />, { auth: { entrar } });

    await userEvent.type(screen.getByLabelText('E-mail'), 'ana@vivlio.com');
    await userEvent.type(screen.getByLabelText('Senha'), 'senha123');
    await userEvent.click(screen.getByRole('button', { name: 'Entrar' }));

    expect(entrar).toHaveBeenCalledWith('ana@vivlio.com', 'senha123');
    expect(await screen.findByText('Página do acervo')).toBeInTheDocument();
  });

  it('mostra a mensagem da API quando o login é recusado', async () => {
    const entrar = vi.fn().mockRejectedValue({
      response: {
        status: 401,
        data: { mensagem: 'E-mail ou senha inválidos.' },
      },
    });
    renderizar(<Login />, { auth: { entrar } });

    await userEvent.type(screen.getByLabelText('E-mail'), 'ana@vivlio.com');
    await userEvent.type(screen.getByLabelText('Senha'), 'errada');
    await userEvent.click(screen.getByRole('button', { name: 'Entrar' }));

    expect(
      await screen.findByText('E-mail ou senha inválidos.')
    ).toBeInTheDocument();
    expect(screen.queryByText('Página do acervo')).not.toBeInTheDocument();
  });
});
