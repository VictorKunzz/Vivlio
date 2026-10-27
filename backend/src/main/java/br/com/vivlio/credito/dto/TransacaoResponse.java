package br.com.vivlio.credito.dto;

import br.com.vivlio.credito.entity.TransacaoCredito;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public record TransacaoResponse(Long id, String tipo, String descricao, OffsetDateTime data) {
    public static TransacaoResponse de(TransacaoCredito transacao) {
        return new TransacaoResponse(
                transacao.getId(),
                transacao.getTipo().name(),
                transacao.getDescricao(),
                // criado_em é gravado no fuso do servidor; o offset deixa o cliente converter certo.
                transacao.getCriadoEm().atZone(ZoneId.systemDefault()).toOffsetDateTime()
        );
    }
}
