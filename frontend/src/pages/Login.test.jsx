import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { AuthContext } from '../context/AuthContext';
import Login from './Login';

function renderizar(entrar, entrada = '/login') {
  return render(
    <AuthContext.Provider value={{ entrar }}>
      <MemoryRouter initialEntries={[entrada]}>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/acervo" element={<p>Tela do acervo</p>} />
          <Route path="/doar" element={<p>Tela de doação</p>} />
        </Routes>
      </MemoryRouter>
    </AuthContext.Provider>
  );
}

async function preencherEEnviar(email, senha) {
  const usuario = userEvent.setup();
  await usuario.type(screen.getByLabelText('E-mail'), email);
  await usuario.type(screen.getByLabelText('Senha'), senha);
  await usuario.click(screen.getByRole('button', { name: 'Entrar' }));
}

describe('Login', () => {
  it('autentica e leva ao acervo', async () => {
    const entrar = vi.fn().mockResolvedValue();
    renderizar(entrar);

    await preencherEEnviar('ana@vivlio.com', 'segredo123');

    expect(entrar).toHaveBeenCalledWith('ana@vivlio.com', 'segredo123');
    expect(await screen.findByText('Tela do acervo')).toBeInTheDocument();
  });

  it('volta para a rota protegida que o usuário tentou abrir', async () => {
    const entrar = vi.fn().mockResolvedValue();
    renderizar(entrar, { pathname: '/login', state: { de: '/doar' } });

    await preencherEEnviar('ana@vivlio.com', 'segredo123');

    expect(await screen.findByText('Tela de doação')).toBeInTheDocument();
  });

  it('mostra a mensagem da API quando as credenciais são recusadas', async () => {
    const entrar = vi.fn().mockRejectedValue({
      response: {
        status: 401,
        data: { mensagem: 'E-mail ou senha inválidos.' },
      },
    });
    renderizar(entrar);

    await preencherEEnviar('ana@vivlio.com', 'errada');

    expect(
      await screen.findByText('E-mail ou senha inválidos.')
    ).toBeInTheDocument();
    expect(screen.queryByText('Tela do acervo')).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Entrar' })).toBeEnabled();
  });
});
