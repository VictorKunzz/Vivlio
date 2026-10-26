package br.com.vivlio.avaliacao.dto;

import java.util.List;

public record CriterioResponse(String campo, String pergunta, List<OpcaoResponse> opcoes) {
}
