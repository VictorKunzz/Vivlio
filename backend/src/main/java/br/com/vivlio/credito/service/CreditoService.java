package br.com.vivlio.credito.service;

import br.com.vivlio.credito.dto.ExtratoResponse;
import br.com.vivlio.credito.dto.TransacaoResponse;
import br.com.vivlio.credito.entity.TipoTransacao;
import br.com.vivlio.credito.entity.TransacaoCredito;
import br.com.vivlio.credito.repository.TransacaoCreditoRepository;
import br.com.vivlio.shared.exception.NegocioException;
import br.com.vivlio.usuario.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Único ponto que altera o saldo de créditos: toda mudança em saldo_creditos registra a
 * transação correspondente, para que saldo = entradas - saídas (DT-05).
 */
@Service
@RequiredArgsConstructor
public class CreditoService {

    private final TransacaoCreditoRepository transacaoCreditoRepository;

    @Transactional
    public void creditar(Usuario usuario, String descricao) {
        usuario.setSaldoCreditos(usuario.getSaldoCreditos() + 1);
        transacaoCreditoRepository.save(new TransacaoCredito(usuario, TipoTransacao.ENTRADA, descricao));
    }

    @Transactional
    public void debitar(Usuario usuario, String descricao) {
        if (usuario.getSaldoCreditos() < 1) {
            throw new NegocioException(HttpStatus.CONFLICT,
                    "Você precisa de pelo menos 1 crédito ativo para resgatar um livro. Doe um livro para ganhar créditos.");
        }

        usuario.setSaldoCreditos(usuario.getSaldoCreditos() - 1);
        transacaoCreditoRepository.save(new TransacaoCredito(usuario, TipoTransacao.SAIDA, descricao));
    }

    @Transactional(readOnly = true)
    public ExtratoResponse extrato(Usuario usuario) {
        return new ExtratoResponse(
                usuario.getSaldoCreditos(),
                transacaoCreditoRepository.countByUsuarioIdAndTipo(usuario.getId(), TipoTransacao.ENTRADA),
                transacaoCreditoRepository.countByUsuarioIdAndTipo(usuario.getId(), TipoTransacao.SAIDA),
                transacaoCreditoRepository.findByUsuarioIdOrderByIdDesc(usuario.getId()).stream()
                        .map(TransacaoResponse::de)
                        .toList()
        );
    }
}
