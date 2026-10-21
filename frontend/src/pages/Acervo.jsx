import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { mensagemDeErro } from '../services/erros';
import { useAuth } from '../hooks/useAuth';
import { GENEROS } from '../constants/generos';
import Estrelas from '../components/Estrelas';

function Acervo() {
  const { usuario, atualizarSaldo } = useAuth();

  const [livros, setLivros] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const [sucesso, setSucesso] = useState('');
  const [busca, setBusca] = useState('');
  const [genero, setGenero] = useState('');
  const [resgatando, setResgatando] = useState(null);

  useEffect(() => {
    let ativo = true;

    api
      .get('/api/livros')
      .then((resposta) => {
        if (ativo) setLivros(resposta.data);
      })
      .catch((falha) => {
        if (ativo) setErro(mensagemDeErro(falha));
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });

    return () => {
      ativo = false;
    };
  }, []);

  async function aoFiltrar(evento) {
    evento.preventDefault();
    setErro('');
    setSucesso('');
    setCarregando(true);

    try {
      const { data } = await api.get('/api/livros', {
        params: { busca, genero },
      });
      setLivros(data);
    } catch (falha) {
      setErro(mensagemDeErro(falha));
    } finally {
      setCarregando(false);
    }
  }

  async function aoResgatar(livro) {
    setErro('');
    setSucesso('');
    setResgatando(livro.id);

    try {
      const { data } = await api.post('/api/resgates', { livroId: livro.id });
      atualizarSaldo(data.saldoCreditos);
      setLivros((atuais) => atuais.filter((item) => item.id !== livro.id));
      setSucesso(
        `Resgate concluído. O livro "${livro.titulo}" é seu. Saldo atual: ${data.saldoCreditos}.`
      );
    } catch (falha) {
      setErro(mensagemDeErro(falha));
    } finally {
      setResgatando(null);
    }
  }

  return (
    <section>
      <h1>Acervo</h1>
      <p className="subtitulo">
        Livros disponíveis para resgate. Cada resgate consome 1 crédito.
      </p>

      <form className="filtros" onSubmit={aoFiltrar}>
        <label className="campo">
          Título ou autor
          <input
            type="text"
            value={busca}
            onChange={(evento) => setBusca(evento.target.value)}
            placeholder="Buscar no acervo"
          />
        </label>

        <label className="campo">
          Gênero
          <select
            value={genero}
            onChange={(evento) => setGenero(evento.target.value)}
          >
            <option value="">Todos</option>
            {GENEROS.map((item) => (
              <option key={item} value={item}>
                {item}
              </option>
            ))}
          </select>
        </label>

        <button type="submit" className="botao" disabled={carregando}>
          Filtrar
        </button>
      </form>

      {erro && <p className="erro">{erro}</p>}
      {sucesso && <p className="sucesso">{sucesso}</p>}

      {usuario && usuario.saldoCreditos < 1 && (
        <p className="dica">
          Você está sem créditos. <Link to="/doar">Doe um livro</Link> para
          ganhar 1 crédito e poder resgatar.
        </p>
      )}

      {carregando && <p className="vazio">Carregando livros...</p>}

      {!carregando && livros.length === 0 && (
        <p className="vazio">Nenhum livro disponível com esses filtros.</p>
      )}

      {!carregando && livros.length > 0 && (
        <ul className="grade-livros">
          {livros.map((livro) => (
            <li key={livro.id} className="card-livro">
              <h2>{livro.titulo}</h2>
              <p className="autor">{livro.autor}</p>
              <span className="chip">{livro.genero}</span>
              <Estrelas nota={livro.nota} />
              <p className="doador">Doado por {livro.doador}</p>

              {usuario ? (
                <button
                  type="button"
                  className="botao"
                  onClick={() => aoResgatar(livro)}
                  disabled={resgatando === livro.id}
                >
                  {resgatando === livro.id ? 'Resgatando...' : 'Resgatar'}
                </button>
              ) : (
                <Link to="/login" className="botao botao-secundario">
                  Entre para resgatar
                </Link>
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

export default Acervo;
