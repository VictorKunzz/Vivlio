import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { AuthContext } from '../context/AuthContext';
import RotaProtegida from './RotaProtegida';

function TelaDeLogin() {
  const local = useLocation();
  return <p>Tela de login, voltar para {local.state?.de}</p>;
}

function renderizar(auth) {
  return render(
    <AuthContext.Provider value={auth}>
      <MemoryRouter initialEntries={['/perfil']}>
        <Routes>
          <Route path="/login" element={<TelaDeLogin />} />
          <Route element={<RotaProtegida />}>
            <Route path="/perfil" element={<p>Conteúdo protegido</p>} />
          </Route>
        </Routes>
      </MemoryRouter>
    </AuthContext.Provider>
  );
}

describe('RotaProtegida', () => {
  it('RNF02 - visitante é levado ao login guardando a rota de origem', () => {
    renderizar({ usuario: null, carregando: false });

    expect(
      screen.getByText('Tela de login, voltar para /perfil')
    ).toBeInTheDocument();
    expect(screen.queryByText('Conteúdo protegido')).not.toBeInTheDocument();
  });

  it('usuário logado acessa a rota', () => {
    renderizar({ usuario: { nome: 'Ana' }, carregando: false });

    expect(screen.getByText('Conteúdo protegido')).toBeInTheDocument();
  });

  it('aguarda a sessão carregar antes de decidir', () => {
    renderizar({ usuario: null, carregando: true });

    expect(screen.getByText('Carregando...')).toBeInTheDocument();
    expect(screen.queryByText(/Tela de login/)).not.toBeInTheDocument();
  });
});
