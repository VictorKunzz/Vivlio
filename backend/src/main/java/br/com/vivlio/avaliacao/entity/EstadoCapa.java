package br.com.vivlio.avaliacao.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoCapa implements OpcaoAvaliacao {

    SEM_DESGASTE(5, "Sem dobras, riscos ou cantos gastos."),
    CANTOS_LEVEMENTE_GASTOS(4, "Leve desgaste nos cantos."),
    RISCOS_E_DOBRAS(3, "Riscos e dobras visíveis, sem rasgo."),
    RASGO_PEQUENO(2, "Rasgo pequeno, desbotada ou com fita adesiva."),
    MUITO_DANIFICADA(1, "Muito danificada, solta ou ausente.");

    private final int nota;
    private final String descricao;
}
