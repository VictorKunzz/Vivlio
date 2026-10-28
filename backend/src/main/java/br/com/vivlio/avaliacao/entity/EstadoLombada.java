package br.com.vivlio.avaliacao.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoLombada implements OpcaoAvaliacao {

    RETA(5, "Reta, sem vincos de leitura."),
    VINCOS_LEVES(4, "Vincos leves de leitura."),
    VINCOS_MARCADOS(3, "Vincos marcados, mas continua firme."),
    GASTA_E_FROUXA(2, "Gasta e um pouco frouxa, sem nenhuma página solta."),
    QUEBRADA(1, "Quebrada, com páginas soltas, mas todas presentes.");

    private final int nota;
    private final String descricao;
}
