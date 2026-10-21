package br.com.vivlio.livro.dto;

import br.com.vivlio.avaliacao.dto.AvaliacaoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DoacaoRequest(

        @NotBlank(message = "O título é obrigatório.")
        String titulo,

        @NotBlank(message = "O autor é obrigatório.")
        String autor,

        @NotBlank(message = "O gênero é obrigatório.")
        String genero,

        @Valid
        @NotNull(message = "Responda o questionário de estado do livro.")
        AvaliacaoRequest avaliacao
) {
}
