import { render } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { vi } from 'vitest';
import { AuthContext } from '../context/AuthContext';

// Renderiza a tela com um AuthContext controlado pelo teste e rotas de destino
// fáceis de reconhecer (/login e /acervo), para conferir redirecionamentos.
export function renderizar(
  elemento,
  { rota = '/', caminho = '*', auth = {}, filhos = null } = {}
) {
  const valorAuth = {
    usuario: null,
    carregando: false,
    entrar: vi.fn(),
    registrar: vi.fn(),
    sair: vi.fn(),
    atualizarSaldo: vi.fn(),
    ...auth,
  };

  render(
    <AuthContext.Provider value={valorAuth}>
      <MemoryRouter initialEntries={[rota]}>
        <Routes>
          <Route path="/login" element={<p>Página de login</p>} />
          <Route path="/acervo" element={<p>Página do acervo</p>} />
          <Route path={caminho} element={elemento}>
            {filhos}
          </Route>
        </Routes>
      </MemoryRouter>
    </AuthContext.Provider>
  );

  return valorAuth;
}

export const USUARIO = {
  id: 1,
  nome: 'Ana Souza',
  email: 'ana@vivlio.com',
  saldoCreditos: 1,
};
