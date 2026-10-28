import { beforeEach, describe, expect, it, vi } from 'vitest';
import api from './api';
import { irParaLogin } from './navegacao';

vi.mock('./navegacao', () => ({ irParaLogin: vi.fn() }));

function respostaDaApi(status) {
  api.defaults.adapter = async (config) => {
    const erro = new Error(`HTTP ${status}`);
    erro.config = config;
    erro.response = { status, data: {}, config };
    throw erro;
  };
}

describe('api: sessão expirada (TC19)', () => {
  beforeEach(() => {
    localStorage.setItem('vivlio_token', 'token-antigo');
  });

  it('envia o token salvo no cabeçalho Authorization', async () => {
    let cabecalho;
    api.defaults.adapter = async (config) => {
      cabecalho = config.headers.Authorization;
      return { status: 200, data: [], headers: {}, config };
    };

    await api.get('/api/livros');

    expect(cabecalho).toBe('Bearer token-antigo');
  });

  it('em 401 apaga o token e manda para o login', async () => {
    respostaDaApi(401);

    await expect(api.get('/api/usuarios/me')).rejects.toBeDefined();

    expect(localStorage.getItem('vivlio_token')).toBeNull();
    expect(irParaLogin).toHaveBeenCalledOnce();
  });

  it('401 no próprio login não redireciona (é só senha errada)', async () => {
    respostaDaApi(401);

    await expect(api.post('/api/auth/login', {})).rejects.toBeDefined();

    expect(localStorage.getItem('vivlio_token')).toBe('token-antigo');
    expect(irParaLogin).not.toHaveBeenCalled();
  });

  it('outros erros não derrubam a sessão', async () => {
    respostaDaApi(409);

    await expect(api.post('/api/resgates', {})).rejects.toBeDefined();

    expect(localStorage.getItem('vivlio_token')).toBe('token-antigo');
    expect(irParaLogin).not.toHaveBeenCalled();
  });
});
