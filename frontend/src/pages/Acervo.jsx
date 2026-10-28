import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { mensagemDeErro } from '../services/erros';
import { useAuth } from '../hooks/useAuth';
import { GENEROS } from '../constants/generos';
import Estrelas from '../components/Estrelas';
import ModalResgate from '../components/ModalResgate';

const MENSAGEM_SEM_CREDITO =
  'Você precisa de pelo menos 1 crédito ativo para resgatar um livro. Doe um livro para ganhar créditos.';

function Acervo() {
  const { usuario, atualizarSaldo } = useAuth();

  const [livros, setLivros] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const [sucesso, setSucesso] = useState('');
  const [busca, setBusca] = useState('');
  const [genero, setGenero] = useState('');
  const [selecionado, setSelecionado] = useState(null);
  const [resgatando, setResgatando] = useState(false);

  const semCredito = Boolean(usuario) && usuario.saldoCreditos < 1;
  const ehMeu = (livro) => Boolean(usuario) && livro.doadorId === usuario.id;

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

  function abrirResgate(livro) {
    setErro('');
    setSucesso('');
    if (semCredito) {
      // RN crítica: sem crédito o resgate é barrado aqui, antes de chegar à API.
      setErro(MENSAGEM_SEM_CREDITO);
      return;
    }
    setSelecionado(livro);
  }

  const fecharResgate = useCallback(() => setSelecionado(null), []);

  async function aoResgatar(livro) {
    setResgatando(true);

    try {
      const { data } = await api.post('/api/resgates', { livroId: livro.id });
      atualizarSaldo(data.saldoCreditos);
      setLivros((atuais) => atuais.filter((item) => item.id !== livro.id));
      setSucesso('Resgate confirmado! Combine a entrega na biblioteca.');
    } catch (falha) {
      setErro(mensagemDeErro(falha));
    } finally {
      setResgatando(false);
      setSelecionado(null);
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

      {semCredito && (
        <p className="dica">
          {MENSAGEM_SEM_CREDITO} <Link to="/doar">Ir para a doação</Link>
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

              {usuario && ehMeu(livro) && (
                <p className="chip chip-neutro">Você doou este livro</p>
              )}
              {usuario && !ehMeu(livro) && (
                <button
                  type="button"
                  className="botao"
                  onClick={() => abrirResgate(livro)}
                  disabled={semCredito}
                  title={semCredito ? MENSAGEM_SEM_CREDITO : undefined}
                >
                  Resgatar (1 Crédito)
                </button>
              )}
              {!usuario && (
                <Link to="/login" className="botao botao-secundario">
                  Entre para resgatar
                </Link>
              )}
            </li>
          ))}
        </ul>
      )}

      {selecionado && (
        <ModalResgate
          livro={selecionado}
          resgatando={resgatando}
          aoConfirmar={() => aoResgatar(selecionado)}
          aoFechar={fecharResgate}
        />
      )}
    </section>
  );
}

export default Acervo;
