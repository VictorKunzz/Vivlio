package br.com.vivlio.avaliacao.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Condições que recusam o livro na hora: ele não entra no acervo e não gera crédito.
 */
@Getter
@RequiredArgsConstructor
public enum Eliminatorio {

    PAGINA_FALTANDO("Falta alguma página."),
    MOFO("Tem mofo, em qualquer quantidade."),
    DANO_POR_AGUA("Molhou e ficou com páginas coladas."),
    INFESTACAO("Tem traças, cupim ou outra infestação."),
    TEXTO_ILEGIVEL("Algum trecho do texto está ilegível."),
    CAPA_SEPARADA("Capa e miolo estão totalmente separados.");

    private final String descricao;
}
