package br.com.vivlio.livro.dto;

import br.com.vivlio.avaliacao.entity.AvaliacaoLivro;
import br.com.vivlio.livro.entity.Livro;

public record LivroResponse(Long id, String titulo, String autor, String genero, String status, String doador,
                            Integer nota, Boolean sobrecapaAusente) {
    public static LivroResponse de(Livro livro) {
        AvaliacaoLivro avaliacao = livro.getAvaliacao();
        return new LivroResponse(
                livro.getId(),
                livro.getTitulo(),
                livro.getAutor(),
                livro.getGenero(),
                livro.getStatus().name(),
                livro.getDoador().getNome(),
                avaliacao == null ? null : avaliacao.getNota(),
                avaliacao == null ? null : avaliacao.getSobrecapaAusente()
        );
    }
}
