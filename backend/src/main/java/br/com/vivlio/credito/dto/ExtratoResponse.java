package br.com.vivlio.credito.dto;

import java.util.List;

public record ExtratoResponse(Integer saldo, long totalEntradas, long totalSaidas, List<TransacaoResponse> transacoes) {
}
