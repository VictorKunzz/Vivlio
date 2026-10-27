import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import {
  MENSAGEM_CAMPOS_INVALIDOS,
  errosDosCampos,
  mensagemDeErro,
} from '../services/erros';
import { useAuth } from '../hooks/useAuth';
import { GENEROS } from '../constants/generos';

const AVALIACAO_VAZIA = {
  respostas: {},
  eliminatorios: [],
  didaticoComExerciciosRespondidos: false,
  sobrecapaAusente: false,
};

function validar({ titulo, autor, genero }, questionario, respostas) {
  const erros = {};
  if (!titulo.trim()) erros.titulo = 'O título é obrigatório.';
  if (!autor.trim()) erros.autor = 'O autor é obrigatório.';
  if (!genero) erros.genero = 'O gênero é obrigatório.';
  questionario.criterios.forEach((criterio) => {
    if (!respostas[criterio.campo]) {
      erros[`avaliacao.${criterio.campo}`] = 'Escolha uma opção.';
    }
  });
  return erros;
}

function Doacao() {
  const { atualizarSaldo } = useAuth();

  const [titulo, setTitulo] = useState('');
  const [autor, setAutor] = useState('');
  const [genero, setGenero] = useState('');
  const [questionario, setQuestionario] = useState(null);
  const [avaliacao, setAvaliacao] = useState(AVALIACAO_VAZIA);
  const [erro, setErro] = useState('');
  const [erros, setErros] = useState({});
  const [sucesso, setSucesso] = useState('');
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    let ativo = true;

    api
      .get('/api/avaliacao/questionario')
      .then((resposta) => {
        if (ativo) setQuestionario(resposta.data);
      })
      .catch((falha) => {
        if (ativo) setErro(mensagemDeErro(falha));
      });

    return () => {
      ativo = false;
    };
  }, []);

  const recusado = avaliacao.eliminatorios.length > 0;

  function responder(campo, codigo) {
    setAvaliacao((atual) => ({
      ...atual,
      respostas: { ...atual.respostas, [campo]: codigo },
    }));
  }

  function alternarEliminatorio(codigo) {
    setAvaliacao((atual) => ({
      ...atual,
      eliminatorios: atual.eliminatorios.includes(codigo)
        ? atual.eliminatorios.filter((item) => item !== codigo)
        : [...atual.eliminatorios, codigo],
    }));
  }

  function marcar(campo, valor) {
    setAvaliacao((atual) => ({ ...atual, [campo]: valor }));
  }

  async function aoEnviar(evento) {
    evento.preventDefault();
    setErro('');
    setSucesso('');

    const invalidos = validar(
      { titulo, autor, genero },
      questionario,
      avaliacao.respostas
    );
    setErros(invalidos);
    if (Object.keys(invalidos).length > 0) {
      setErro(MENSAGEM_CAMPOS_INVALIDOS);
      return;
    }

    setEnviando(true);
    try {
      const { data } = await api.post('/api/livros', {
        titulo,
        autor,
        genero,
        avaliacao: {
          ...avaliacao.respostas,
          eliminatorios: avaliacao.eliminatorios,
          didaticoComExerciciosRespondidos:
            avaliacao.didaticoComExerciciosRespondidos,
          sobrecapaAusente: avaliacao.sobrecapaAusente,
        },
      });
      atualizarSaldo(data.saldoCreditos);
      setSucesso(
        `Livro cadastrado! Você ganhou 1 crédito. Nota do estado: ${data.livro.nota} de 5.`
      );
      setTitulo('');
      setAutor('');
      setGenero('');
      setAvaliacao(AVALIACAO_VAZIA);
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
    <section className="tela-form">
      <h1>Doar livro</h1>
      <p className="subtitulo">
        Cada livro doado entra no acervo da comunidade e te dá 1 crédito.
        Descreva o estado do livro com sinceridade: o sebo confere na entrega.
      </p>

      <form className="formulario" onSubmit={aoEnviar} noValidate>
        <label className="campo">
          Título
          <input
            type="text"
            value={titulo}
            onChange={(evento) => setTitulo(evento.target.value)}
            className={erros.titulo ? 'invalido' : undefined}
            aria-invalid={Boolean(erros.titulo)}
          />
          {erros.titulo && (
            <small className="mensagem-campo">{erros.titulo}</small>
          )}
        </label>

        <label className="campo">
          Autor
          <input
            type="text"
            value={autor}
            onChange={(evento) => setAutor(evento.target.value)}
            className={erros.autor ? 'invalido' : undefined}
            aria-invalid={Boolean(erros.autor)}
          />
          {erros.autor && (
            <small className="mensagem-campo">{erros.autor}</small>
          )}
        </label>

        <label className="campo">
          Gênero
          <select
            value={genero}
            onChange={(evento) => setGenero(evento.target.value)}
            className={erros.genero ? 'invalido' : undefined}
            aria-invalid={Boolean(erros.genero)}
          >
            <option value="">Selecione</option>
            {GENEROS.map((item) => (
              <option key={item} value={item}>
                {item}
              </option>
            ))}
          </select>
          {erros.genero && (
            <small className="mensagem-campo">{erros.genero}</small>
          )}
        </label>

        {!questionario && !erro && (
          <p className="vazio">Carregando questionário...</p>
        )}

        {questionario && (
          <>
            <h2 className="titulo-secao">Estado do livro</h2>

            <fieldset className="pergunta">
              <legend>O livro tem algum destes problemas?</legend>
              {questionario.eliminatorios.map((item) => (
                <label key={item.codigo} className="opcao">
                  <input
                    type="checkbox"
                    checked={avaliacao.eliminatorios.includes(item.codigo)}
                    onChange={() => alternarEliminatorio(item.codigo)}
                  />
                  {item.descricao}
                </label>
              ))}
            </fieldset>

            {recusado && (
              <p className="erro">
                Livros com esses problemas não podem ser doados: eles não entram
                no acervo e não geram crédito.
              </p>
            )}

            {!recusado &&
              questionario.criterios.map((criterio) => (
                <fieldset
                  key={criterio.campo}
                  className={
                    erros[`avaliacao.${criterio.campo}`]
                      ? 'pergunta invalida'
                      : 'pergunta'
                  }
                >
                  <legend>{criterio.pergunta}</legend>
                  {criterio.opcoes.map((opcao) => (
                    <label key={opcao.codigo} className="opcao">
                      <input
                        type="radio"
                        name={criterio.campo}
                        value={opcao.codigo}
                        checked={
                          avaliacao.respostas[criterio.campo] === opcao.codigo
                        }
                        onChange={() => responder(criterio.campo, opcao.codigo)}
                      />
                      {opcao.descricao}
                    </label>
                  ))}
                  {erros[`avaliacao.${criterio.campo}`] && (
                    <small className="mensagem-campo">
                      {erros[`avaliacao.${criterio.campo}`]}
                    </small>
                  )}
                </fieldset>
              ))}

            {!recusado && (
              <fieldset className="pergunta">
                <legend>Outras informações</legend>
                <label className="opcao">
                  <input
                    type="checkbox"
                    checked={avaliacao.didaticoComExerciciosRespondidos}
                    onChange={(evento) =>
                      marcar(
                        'didaticoComExerciciosRespondidos',
                        evento.target.checked
                      )
                    }
                  />
                  É livro didático com exercícios respondidos.
                </label>
                <label className="opcao">
                  <input
                    type="checkbox"
                    checked={avaliacao.sobrecapaAusente}
                    onChange={(evento) =>
                      marcar('sobrecapaAusente', evento.target.checked)
                    }
                  />
                  Veio com sobrecapa, mas ela se perdeu.
                </label>
              </fieldset>
            )}
          </>
        )}

        {erro && <p className="erro">{erro}</p>}
        {sucesso && (
          <p className="sucesso">
            {sucesso} <Link to="/acervo">Ver no acervo</Link>
          </p>
        )}

        <button
          type="submit"
          className="botao"
          disabled={enviando || !questionario || recusado}
        >
          {enviando ? 'Cadastrando...' : 'Cadastrar doação'}
        </button>
      </form>
    </section>
  );
}

export default Doacao;
