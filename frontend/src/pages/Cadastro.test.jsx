import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { renderizar } from '../test/renderizar';
import Cadastro from './Cadastro';

function enviar() {
  return userEvent.click(screen.getByRole('button', { name: 'Criar conta' }));
}

describe('Cadastro', () => {
  it('TC03: envio vazio destaca nome, e-mail e senha e não chama a API', async () => {
    const auth = renderizar(<Cadastro />);

    await enviar();

    for (const campo of ['Nome', 'E-mail', 'Senha']) {
      expect(screen.getByLabelText(new RegExp(`^${campo}`))).toHaveAttribute(
        'aria-invalid',
        'true'
      );
    }
    expect(screen.getByText('O nome é obrigatório.')).toBeInTheDocument();
    expect(screen.getByText('O e-mail é obrigatório.')).toBeInTheDocument();
    expect(screen.getByText('A senha é obrigatória.')).toBeInTheDocument();
    expect(auth.registrar).not.toHaveBeenCalled();
  });

  it('valida o formato do e-mail e o tamanho da senha', async () => {
    renderizar(<Cadastro />);

    await userEvent.type(screen.getByLabelText(/^Nome/), 'Ana');
    await userEvent.type(screen.getByLabelText(/^E-mail/), 'ana-sem-arroba');
    await userEvent.type(screen.getByLabelText(/^Senha/), '123');
    await enviar();

    expect(screen.getByText('Informe um e-mail válido.')).toBeInTheDocument();
    expect(
      screen.getByText('A senha deve ter no mínimo 6 caracteres.')
    ).toBeInTheDocument();
    expect(screen.getByLabelText(/^Nome/)).toHaveAttribute(
      'aria-invalid',
      'false'
    );
  });

  it('cadastro válido registra e vai para o acervo', async () => {
    const registrar = vi.fn().mockResolvedValue(undefined);
    renderizar(<Cadastro />, { auth: { registrar } });

    await userEvent.type(screen.getByLabelText(/^Nome/), 'Ana Souza');
    await userEvent.type(screen.getByLabelText(/^E-mail/), 'ana@vivlio.com');
    await userEvent.type(screen.getByLabelText(/^Senha/), 'senha123');
    await enviar();

    expect(registrar).toHaveBeenCalledWith(
      'Ana Souza',
      'ana@vivlio.com',
      'senha123'
    );
    expect(await screen.findByText('Página do acervo')).toBeInTheDocument();
  });
});
