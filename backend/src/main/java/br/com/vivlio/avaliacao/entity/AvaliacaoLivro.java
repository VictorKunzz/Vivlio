package br.com.vivlio.avaliacao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Estado do livro informado no questionário de doação. As colunas aceitam nulo porque os
 * livros doados antes da avaliação existir não têm essas respostas.
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoLivro {

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_capa")
    private EstadoCapa capa;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_lombada")
    private EstadoLombada lombada;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_paginas")
    private EstadoPaginas paginas;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_marcacoes")
    private EstadoMarcacoes marcacoes;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_manchas")
    private EstadoManchas manchas;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_odor")
    private EstadoOdor odor;

    @Column(name = "didatico_com_exercicios_respondidos")
    private Boolean didaticoComExerciciosRespondidos;

    @Column(name = "sobrecapa_ausente")
    private Boolean sobrecapaAusente;

    @Column(name = "nota_estado")
    private Integer nota;
}
