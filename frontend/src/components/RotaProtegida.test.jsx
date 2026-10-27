import { screen } from '@testing-library/react';
import { Route } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { USUARIO, renderizar } from '../test/renderizar';
import RotaProtegida from './RotaProtegida';

const conteudo = <Route path="/doar" element={<p>Tela protegida</p>} />;

describe('RotaProtegida', () => {
  it('manda o visitante para o login', () => {
    renderizar(<RotaProtegida />, {
      rota: '/doar',
      caminho: '/',
      filhos: conteudo,
    });

    expect(screen.getByText('Página de login')).toBeInTheDocument();
    expect(screen.queryByText('Tela protegida')).not.toBeInTheDocument();
  });

  it('mostra a tela para quem está logado', () => {
    renderizar(<RotaProtegida />, {
      rota: '/doar',
      caminho: '/',
      filhos: conteudo,
      auth: { usuario: USUARIO },
    });

    expect(screen.getByText('Tela protegida')).toBeInTheDocument();
  });

  it('espera a sessão carregar antes de decidir', () => {
    renderizar(<RotaProtegida />, {
      rota: '/doar',
      caminho: '/',
      filhos: conteudo,
      auth: { carregando: true },
    });

    expect(screen.getByText('Carregando...')).toBeInTheDocument();
    expect(screen.queryByText('Página de login')).not.toBeInTheDocument();
  });
});
