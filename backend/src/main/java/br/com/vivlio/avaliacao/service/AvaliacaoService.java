package br.com.vivlio.avaliacao.service;

import br.com.vivlio.avaliacao.dto.AvaliacaoRequest;
import br.com.vivlio.avaliacao.dto.CriterioResponse;
import br.com.vivlio.avaliacao.dto.OpcaoResponse;
import br.com.vivlio.avaliacao.dto.QuestionarioResponse;
import br.com.vivlio.avaliacao.entity.AvaliacaoLivro;
import br.com.vivlio.avaliacao.entity.Eliminatorio;
import br.com.vivlio.avaliacao.entity.EstadoCapa;
import br.com.vivlio.avaliacao.entity.EstadoLombada;
import br.com.vivlio.avaliacao.entity.EstadoManchas;
import br.com.vivlio.avaliacao.entity.EstadoMarcacoes;
import br.com.vivlio.avaliacao.entity.EstadoOdor;
import br.com.vivlio.avaliacao.entity.EstadoPaginas;
import br.com.vivlio.avaliacao.entity.OpcaoAvaliacao;
import br.com.vivlio.shared.exception.NegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AvaliacaoService {

    static final int NOTA_MAXIMA_DIDATICO_RESPONDIDO = 2;

    /**
     * A nota final é a menor entre os seis critérios: um defeito grave não é compensado
     * pelos outros. Qualquer eliminatório recusa o livro.
     */
    public AvaliacaoLivro avaliar(AvaliacaoRequest request) {
        List<Eliminatorio> eliminatorios = request.eliminatorios() == null ? List.of() : request.eliminatorios();
        if (!eliminatorios.isEmpty()) {
            String motivos = eliminatorios.stream()
                    .distinct()
                    .map(Eliminatorio::getDescricao)
                    .collect(Collectors.joining(" "));
            throw new NegocioException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Este livro não pode ser doado. " + motivos
                            + " Livros nessas condições não entram no acervo e não geram crédito.");
        }

        int nota = Stream.<OpcaoAvaliacao>of(request.capa(), request.lombada(), request.paginas(),
                        request.marcacoes(), request.manchas(), request.odor())
                .mapToInt(OpcaoAvaliacao::getNota)
                .min()
                .orElseThrow();

        if (request.didaticoComExerciciosRespondidos()) {
            nota = Math.min(nota, NOTA_MAXIMA_DIDATICO_RESPONDIDO);
        }

        return new AvaliacaoLivro(
                request.capa(),
                request.lombada(),
                request.paginas(),
                request.marcacoes(),
                request.manchas(),
                request.odor(),
                request.didaticoComExerciciosRespondidos(),
                request.sobrecapaAusente(),
                nota
        );
    }

    public QuestionarioResponse questionario() {
        return new QuestionarioResponse(
                List.of(
                        criterio("capa", "Como está a capa?", EstadoCapa.values()),
                        criterio("lombada", "Como está a lombada?", EstadoLombada.values()),
                        criterio("paginas", "Como estão as páginas?", EstadoPaginas.values()),
                        criterio("marcacoes", "O livro tem marcações?", EstadoMarcacoes.values()),
                        criterio("manchas", "O livro tem manchas?", EstadoManchas.values()),
                        criterio("odor", "O livro tem cheiro?", EstadoOdor.values())
                ),
                Arrays.stream(Eliminatorio.values())
                        .map(item -> new OpcaoResponse(item.name(), item.getDescricao()))
                        .toList()
        );
    }

    private CriterioResponse criterio(String campo, String pergunta, OpcaoAvaliacao[] opcoes) {
        return new CriterioResponse(campo, pergunta, Arrays.stream(opcoes)
                .map(opcao -> new OpcaoResponse(opcao.name(), opcao.getDescricao()))
                .toList());
    }
}
