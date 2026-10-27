import { useEffect } from 'react';
import Estrelas from './Estrelas';

function ModalResgate({ livro, resgatando, aoConfirmar, aoFechar }) {
  useEffect(() => {
    function aoTeclar(evento) {
      if (evento.key === 'Escape' && !resgatando) aoFechar();
    }
    document.addEventListener('keydown', aoTeclar);
    return () => document.removeEventListener('keydown', aoTeclar);
  }, [resgatando, aoFechar]);

  return (
    <div className="modal-fundo" onClick={resgatando ? undefined : aoFechar}>
      <div
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-resgate-titulo"
        onClick={(evento) => evento.stopPropagation()}
      >
        <h2 id="modal-resgate-titulo">{livro.titulo}</h2>
        <p className="autor">{livro.autor}</p>
        <span className="chip">{livro.genero}</span>

        <dl className="detalhes-livro">
          <dt>Estado</dt>
          <dd>
            {livro.nota == null ? (
              'Sem avaliação'
            ) : (
              <Estrelas nota={livro.nota} />
            )}
          </dd>
          {livro.sobrecapaAusente && (
            <>
              <dt>Sobrecapa</dt>
              <dd>Ausente</dd>
            </>
          )}
          <dt>Doado por</dt>
          <dd>{livro.doador}</dd>
        </dl>

        <p className="dica">
          Confirmar o resgate consome 1 crédito do seu saldo.
        </p>

        <div className="modal-acoes">
          <button
            type="button"
            className="botao botao-secundario"
            onClick={aoFechar}
            disabled={resgatando}
          >
            Cancelar
          </button>
          <button
            type="button"
            className="botao"
            onClick={aoConfirmar}
            disabled={resgatando}
            autoFocus
          >
            {resgatando ? 'Resgatando...' : 'Confirmar resgate'}
          </button>
        </div>
      </div>
    </div>
  );
}

export default ModalResgate;
