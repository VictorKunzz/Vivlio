import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import api from '../services/api';
import { useAuth } from '../hooks/useAuth';
import { AuthProvider } from './AuthProvider';

vi.mock('../services/api', () => ({
  default: { get: vi.fn(), post: vi.fn() },
}));

const ana = { id: 1, nome: 'Ana', email: 'ana@vivlio.com', saldoCreditos: 2 };

function Painel() {
  const { usuario, carregando, entrar, sair, atualizarSaldo } = useAuth();

  if (carregando) {
    return <p>carregando</p>;
  }

  return (
    <div>
      <p>{usuario ? `${usuario.nome}: ${usuario.saldoCreditos}` : 'anônimo'}</p>
      <button onClick={() => entrar('ana@vivlio.com', 'segredo123')}>
        entrar
      </button>
      <button onClick={() => atualizarSaldo(5)}>saldo</button>
      <button onClick={sair}>sair</button>
    </div>
  );
}

function renderizar() {
  return render(
    <AuthProvider>
      <Painel />
    </AuthProvider>
  );
}

describe('AuthProvider', () => {
  beforeEach(() => {
    api.get.mockResolvedValue({ data: ana });
    api.post.mockResolvedValue({ data: { token: 'novo-token', usuario: ana } });
  });

  it('sem token salvo começa anônimo e não consulta a API', () => {
    renderizar();

    expect(screen.getByText('anônimo')).toBeInTheDocument();
    expect(api.get).not.toHaveBeenCalled();
  });

  it('com token salvo recupera o usuário em /api/usuarios/me', async () => {
    localStorage.setItem('vivlio_token', 'token-salvo');
    renderizar();

    expect(await screen.findByText('Ana: 2')).toBeInTheDocument();
    expect(api.get).toHaveBeenCalledWith('/api/usuarios/me');
  });

  it('descarta o token salvo quando a API não o aceita', async () => {
    localStorage.setItem('vivlio_token', 'token-invalido');
    api.get.mockRejectedValue({ response: { status: 401 } });
    renderizar();

    expect(await screen.findByText('anônimo')).toBeInTheDocument();
    expect(localStorage.getItem('vivlio_token')).toBeNull();
  });

  it('entrar guarda o token e o usuário; sair limpa os dois', async () => {
    renderizar();

    await userEvent.click(screen.getByRole('button', { name: 'entrar' }));

    expect(api.post).toHaveBeenCalledWith('/api/auth/login', {
      email: 'ana@vivlio.com',
      senha: 'segredo123',
    });
    expect(await screen.findByText('Ana: 2')).toBeInTheDocument();
    expect(localStorage.getItem('vivlio_token')).toBe('novo-token');

    await userEvent.click(screen.getByRole('button', { name: 'sair' }));

    expect(screen.getByText('anônimo')).toBeInTheDocument();
    expect(localStorage.getItem('vivlio_token')).toBeNull();
  });

  it('atualizarSaldo muda só o saldo do usuário logado', async () => {
    renderizar();
    await userEvent.click(screen.getByRole('button', { name: 'entrar' }));
    await screen.findByText('Ana: 2');

    await userEvent.click(screen.getByRole('button', { name: 'saldo' }));

    await waitFor(() => expect(screen.getByText('Ana: 5')).toBeInTheDocument());
  });
});
