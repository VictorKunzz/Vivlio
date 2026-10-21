import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import api from './api';

const adapterOriginal = api.defaults.adapter;

function responderCom(status) {
  api.defaults.adapter = vi.fn((config) => {
    if (status < 400) {
      return Promise.resolve({ data: {}, status, headers: {}, config });
    }
    const erro = new Error(`Request failed with status code ${status}`);
    erro.config = config;
    erro.response = { status, data: {} };
    return Promise.reject(erro);
  });
}

describe('cliente da API', () => {
  beforeEach(() => {
    vi.stubGlobal('location', { pathname: '/acervo', href: '/acervo' });
  });

  afterEach(() => {
    api.defaults.adapter = adapterOriginal;
  });

  it('envia o token salvo no cabeçalho Authorization', async () => {
    localStorage.setItem('vivlio_token', 'abc.def.ghi');
    responderCom(200);

    await api.get('/api/usuarios/me');

    const config = api.defaults.adapter.mock.calls[0][0];
    expect(config.headers.Authorization).toBe('Bearer abc.def.ghi');
  });

  it('não envia Authorization quando não há token', async () => {
    responderCom(200);

    await api.get('/api/livros');

    const config = api.defaults.adapter.mock.calls[0][0];
    expect(config.headers.Authorization).toBeUndefined();
  });

  it('TC19 - em 401 descarta o token e redireciona para o login', async () => {
    localStorage.setItem('vivlio_token', 'token-expirado');
    responderCom(401);

    await expect(api.post('/api/livros', {})).rejects.toThrow();

    expect(localStorage.getItem('vivlio_token')).toBeNull();
    expect(window.location.href).toBe('/login');
  });

  it('em 401 nas rotas de autenticação não redireciona', async () => {
    localStorage.setItem('vivlio_token', 'token');
    responderCom(401);

    await expect(api.post('/api/auth/login', {})).rejects.toThrow();

    expect(localStorage.getItem('vivlio_token')).toBe('token');
    expect(window.location.href).toBe('/acervo');
  });

  it('em 401 já na tela de login não redireciona de novo', async () => {
    vi.stubGlobal('location', { pathname: '/login', href: '/login?x=1' });
    responderCom(401);

    await expect(api.get('/api/usuarios/me')).rejects.toThrow();

    expect(window.location.href).toBe('/login?x=1');
  });

  it('outros erros não mexem na sessão', async () => {
    localStorage.setItem('vivlio_token', 'token');
    responderCom(409);

    await expect(api.post('/api/resgates', {})).rejects.toThrow();

    expect(localStorage.getItem('vivlio_token')).toBe('token');
    expect(window.location.href).toBe('/acervo');
  });
});
