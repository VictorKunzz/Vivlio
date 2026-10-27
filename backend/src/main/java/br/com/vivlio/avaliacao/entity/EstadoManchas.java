package br.com.vivlio.avaliacao.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoManchas implements OpcaoAvaliacao {

    NENHUMA(5, "Nenhuma mancha. Papel uniforme."),
    AMARELAMENTO_LEVE(4, "Amarelamento leve e uniforme."),
    PEQUENAS_FORA_DO_TEXTO(3, "Manchas pequenas, fora da área do texto."),
    ATINGEM_O_TEXTO(2, "Manchas que atingem o texto, sem atrapalhar a leitura."),
    EXTENSAS(1, "Manchas extensas, mas o texto continua legível.");

    private final int nota;
    private final String descricao;
}
