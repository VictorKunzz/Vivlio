package br.com.vivlio.avaliacao.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoPaginas implements OpcaoAvaliacao {

    SEM_DOBRAS(5, "Nenhuma dobra ou rasgo."),
    ATE_DUAS_ORELHAS(4, "Até 2 orelhas (canto dobrado)."),
    VARIAS_ORELHAS(3, "Várias orelhas ou rasgo pequeno só na margem."),
    RASGOS_NA_MARGEM(2, "Rasgos na margem e muitas dobras."),
    RASGOS_NO_TEXTO(1, "Rasgos que atingem o texto, mas ainda legível.");

    private final int nota;
    private final String descricao;
}
