package br.com.vivlio.avaliacao.dto;

import br.com.vivlio.avaliacao.entity.Eliminatorio;
import br.com.vivlio.avaliacao.entity.EstadoCapa;
import br.com.vivlio.avaliacao.entity.EstadoLombada;
import br.com.vivlio.avaliacao.entity.EstadoManchas;
import br.com.vivlio.avaliacao.entity.EstadoMarcacoes;
import br.com.vivlio.avaliacao.entity.EstadoOdor;
import br.com.vivlio.avaliacao.entity.EstadoPaginas;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AvaliacaoRequest(

        @NotNull(message = "Informe o estado da capa.")
        EstadoCapa capa,

        @NotNull(message = "Informe o estado da lombada.")
        EstadoLombada lombada,

        @NotNull(message = "Informe o estado das páginas.")
        EstadoPaginas paginas,

        @NotNull(message = "Informe as marcações do livro.")
        EstadoMarcacoes marcacoes,

        @NotNull(message = "Informe as manchas do livro.")
        EstadoManchas manchas,

        @NotNull(message = "Informe o cheiro do livro.")
        EstadoOdor odor,

        List<Eliminatorio> eliminatorios,

        boolean didaticoComExerciciosRespondidos,

        boolean sobrecapaAusente
) {
}
