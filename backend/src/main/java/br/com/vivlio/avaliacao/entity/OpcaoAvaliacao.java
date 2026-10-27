package br.com.vivlio.avaliacao.entity;

/**
 * Resposta de um critério do questionário. A nota fica só no servidor:
 * o usuário escolhe a descrição, nunca a estrela.
 */
public interface OpcaoAvaliacao {

    int getNota();

    String getDescricao();

    String name();
}
