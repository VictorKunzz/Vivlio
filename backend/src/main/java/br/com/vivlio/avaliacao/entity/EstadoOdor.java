package br.com.vivlio.avaliacao.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoOdor implements OpcaoAvaliacao {

    // A ficha descreve as notas 5 e 4 do odor do mesmo jeito ("nenhum"), por isso viram uma opção só.
    NENHUM(5, "Nenhum cheiro."),
    GUARDADO_LEVE(3, "Leve cheiro de guardado."),
    PERCEPTIVEL(2, "Cheiro perceptível de cigarro ou de guardado."),
    FORTE(1, "Cheiro forte, sem mofo.");

    private final int nota;
    private final String descricao;
}
