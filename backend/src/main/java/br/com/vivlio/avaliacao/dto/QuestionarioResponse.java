package br.com.vivlio.avaliacao.dto;

import java.util.List;

public record QuestionarioResponse(List<CriterioResponse> criterios, List<OpcaoResponse> eliminatorios) {
}
