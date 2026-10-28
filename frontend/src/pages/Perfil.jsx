import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { mensagemDeErro } from '../services/erros';
import { useAuth } from '../hooks/useAuth';

const FORMATO_DATA = new Intl.DateTimeFormat('pt-BR', {
  dateStyle: 'short',
  timeStyle: 'short',
});

function Perfil() {
  const { usuario } = useAuth();
  const [extrato, setExtrato] = useState(null);
  const [erro, setErro] = useState('');

  useEffect(() => {
    let ativo = true;

    api
      .get('/api/creditos/extrato')
      .then((resposta) => {
        if (ativo) setExtrato(resposta.data);
      })
      .catch((falha) => {
        if (ativo) setErro(mensagemDeErro(falha));
      });

    return () => {
      ativo = false;
    };
  }, []);

  return (
    <section>
      <h1>Meu perfil</h1>

      <div className="cartao">
        <p className="linha-perfil">
          <span>Nome</span>
          {usuario.nome}
        </p>
        <p className="linha-perfil">
          <span>E-mail</span>
          {usuario.email}
        </p>
        <p className="linha-perfil">
          <span>Saldo de créditos</span>
          {usuario.saldoCreditos}
        </p>
      </div>

      {usuario.saldoCreditos < 1 ? (
        <p className="dica">
          Sem créditos no momento. <Link to="/doar">Doe um livro</Link> para
          ganhar 1 crédito.
        </p>
      ) : (
        <p className="dica">
          Você pode resgatar {usuario.saldoCreditos}{' '}
          {usuario.saldoCreditos === 1 ? 'livro' : 'livros'} no{' '}
          <Link to="/acervo">acervo</Link>.
        </p>
      )}

      <h2 className="titulo-secao">Extrato de créditos</h2>

      {erro && <p className="erro">{erro}</p>}
      {!extrato && !erro && <p className="vazio">Carregando extrato...</p>}

      {extrato && (
        <>
          <p className="dica">
            {extrato.totalEntradas}{' '}
            {extrato.totalEntradas === 1 ? 'entrada' : 'entradas'} −{' '}
            {extrato.totalSaidas}{' '}
            {extrato.totalSaidas === 1 ? 'saída' : 'saídas'} = saldo de{' '}
            {extrato.saldo}.
          </p>

          {extrato.transacoes.length === 0 ? (
            <p className="vazio">Nenhuma movimentação ainda.</p>
          ) : (
            <table className="extrato">
              <thead>
                <tr>
                  <th scope="col">Data</th>
                  <th scope="col">Descrição</th>
                  <th scope="col">Crédito</th>
                </tr>
              </thead>
              <tbody>
                {extrato.transacoes.map((transacao) => (
                  <tr key={transacao.id}>
                    <td>{FORMATO_DATA.format(new Date(transacao.data))}</td>
                    <td>{transacao.descricao}</td>
                    <td
                      className={
                        transacao.tipo === 'ENTRADA'
                          ? 'valor-entrada'
                          : 'valor-saida'
                      }
                    >
                      {transacao.tipo === 'ENTRADA' ? '+1' : '−1'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </section>
  );
}

export default Perfil;
