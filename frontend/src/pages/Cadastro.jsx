import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import {
  MENSAGEM_CAMPOS_INVALIDOS,
  errosDosCampos,
  mensagemDeErro,
} from '../services/erros';

const EMAIL_VALIDO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function validar({ nome, email, senha }) {
  const erros = {};
  if (!nome.trim()) erros.nome = 'O nome é obrigatório.';
  if (!email.trim()) erros.email = 'O e-mail é obrigatório.';
  else if (!EMAIL_VALIDO.test(email.trim()))
    erros.email = 'Informe um e-mail válido.';
  if (!senha) erros.senha = 'A senha é obrigatória.';
  else if (senha.length < 6)
    erros.senha = 'A senha deve ter no mínimo 6 caracteres.';
  return erros;
}

function Cadastro() {
  const { registrar } = useAuth();
  const navegar = useNavigate();

  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState('');
  const [erros, setErros] = useState({});
  const [enviando, setEnviando] = useState(false);

  async function aoEnviar(evento) {
    evento.preventDefault();
    setErro('');

    const invalidos = validar({ nome, email, senha });
    setErros(invalidos);
    if (Object.keys(invalidos).length > 0) {
      setErro(MENSAGEM_CAMPOS_INVALIDOS);
      return;
    }

    setEnviando(true);
    try {
      await registrar(nome, email, senha);
      navegar('/acervo', { replace: true });
    } catch (falha) {
      const daApi = errosDosCampos(falha);
      setErros(daApi);
      setErro(
        Object.keys(daApi).length > 0
          ? MENSAGEM_CAMPOS_INVALIDOS
          : mensagemDeErro(falha)
      );
    } finally {
      setEnviando(false);
    }
  }

  return (
    <section className="tela-auth">
      <h1>Criar conta</h1>

      <form className="formulario" onSubmit={aoEnviar} noValidate>
        <label className="campo">
          Nome
          <input
            type="text"
            value={nome}
            onChange={(evento) => setNome(evento.target.value)}
            className={erros.nome ? 'invalido' : undefined}
            aria-invalid={Boolean(erros.nome)}
          />
          {erros.nome && <small className="mensagem-campo">{erros.nome}</small>}
        </label>

        <label className="campo">
          E-mail
          <input
            type="email"
            value={email}
            onChange={(evento) => setEmail(evento.target.value)}
            className={erros.email ? 'invalido' : undefined}
            aria-invalid={Boolean(erros.email)}
          />
          {erros.email && (
            <small className="mensagem-campo">{erros.email}</small>
          )}
        </label>

        <label className="campo">
          Senha
          <input
            type="password"
            value={senha}
            onChange={(evento) => setSenha(evento.target.value)}
            className={erros.senha ? 'invalido' : undefined}
            aria-invalid={Boolean(erros.senha)}
          />
          {erros.senha ? (
            <small className="mensagem-campo">{erros.senha}</small>
          ) : (
            <small>Mínimo de 6 caracteres.</small>
          )}
        </label>

        {erro && <p className="erro">{erro}</p>}

        <button type="submit" className="botao" disabled={enviando}>
          {enviando ? 'Criando...' : 'Criar conta'}
        </button>
      </form>

      <p className="rodape-form">
        Já tem conta? <Link to="/login">Entrar</Link>
      </p>
    </section>
  );
}

export default Cadastro;
