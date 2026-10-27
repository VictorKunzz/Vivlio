package br.com.vivlio.avaliacao.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoMarcacoes implements OpcaoAvaliacao {

    NENHUMA(5, "Nenhuma marcação."),
    NOME_DO_DONO(4, "Só nome do dono, carimbo ou dedicatória."),
    GRIFOS_ATE_10_POR_CENTO(3, "Grifos em até 10% das páginas, a lápis ou marca-texto."),
    ANOTACOES_ATE_30_POR_CENTO(2, "Grifos ou anotações em até 30% das páginas, inclusive a caneta."),
    MAIS_DE_30_POR_CENTO(1, "Mais de 30% das páginas marcadas.");

    private final int nota;
    private final String descricao;
}
