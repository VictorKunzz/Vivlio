package br.com.vivlio.avaliacao.service;

import br.com.vivlio.avaliacao.dto.AvaliacaoRequest;
import br.com.vivlio.avaliacao.dto.QuestionarioResponse;
import br.com.vivlio.avaliacao.entity.AvaliacaoLivro;
import br.com.vivlio.avaliacao.entity.Eliminatorio;
import br.com.vivlio.avaliacao.entity.EstadoCapa;
import br.com.vivlio.avaliacao.entity.EstadoLombada;
import br.com.vivlio.avaliacao.entity.EstadoManchas;
import br.com.vivlio.avaliacao.entity.EstadoMarcacoes;
import br.com.vivlio.avaliacao.entity.EstadoOdor;
import br.com.vivlio.avaliacao.entity.EstadoPaginas;
import br.com.vivlio.shared.exception.NegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvaliacaoServiceTest {

    private final AvaliacaoService service = new AvaliacaoService();

    @Test
    @DisplayName("Livro sem nenhum defeito recebe 5 estrelas")
    void livroComoNovoRecebeCinco() {
        assertThat(service.avaliar(perfeito(List.of(), false, false)).getNota()).isEqualTo(5);
    }

    @Test
    @DisplayName("Exemplo da ficha: notas 4, 5, 3, 4, 5, 5 resultam em 3 estrelas")
    void notaFinalEhAMenorEntreOsCriterios() {
        AvaliacaoRequest request = new AvaliacaoRequest(
                EstadoCapa.CANTOS_LEVEMENTE_GASTOS,
                EstadoLombada.RETA,
                EstadoPaginas.VARIAS_ORELHAS,
                EstadoMarcacoes.NOME_DO_DONO,
                EstadoManchas.NENHUMA,
                EstadoOdor.NENHUM,
                List.of(), false, false);

        assertThat(service.avaliar(request).getNota()).isEqualTo(3);
    }

    @Test
    @DisplayName("Um unico criterio ruim derruba a nota, mesmo com os outros perfeitos")
    void defeitoGraveNaoEhCompensado() {
        AvaliacaoRequest request = new AvaliacaoRequest(
                EstadoCapa.SEM_DESGASTE,
                EstadoLombada.RETA,
                EstadoPaginas.RASGOS_NO_TEXTO,
                EstadoMarcacoes.NENHUMA,
                EstadoManchas.NENHUMA,
                EstadoOdor.NENHUM,
                List.of(), false, false);

        assertThat(service.avaliar(request).getNota()).isEqualTo(1);
    }

    @Test
    @DisplayName("Didatico com exercicios respondidos fica com no maximo 2 estrelas")
    void didaticoRespondidoLimitaANota() {
        assertThat(service.avaliar(perfeito(List.of(), true, false)).getNota()).isEqualTo(2);
    }

    @Test
    @DisplayName("Didatico com exercicios respondidos nao sobe uma nota que ja e menor que 2")
    void didaticoRespondidoNaoAumentaANota() {
        AvaliacaoRequest request = new AvaliacaoRequest(
                EstadoCapa.MUITO_DANIFICADA, EstadoLombada.RETA, EstadoPaginas.SEM_DOBRAS,
                EstadoMarcacoes.NENHUMA, EstadoManchas.NENHUMA, EstadoOdor.NENHUM,
                List.of(), true, false);

        assertThat(service.avaliar(request).getNota()).isEqualTo(1);
    }

    @Test
    @DisplayName("Sobrecapa ausente e registrada, mas nao reduz a nota")
    void sobrecapaAusenteNaoReduzANota() {
        AvaliacaoLivro avaliacao = service.avaliar(perfeito(List.of(), false, true));

        assertThat(avaliacao.getNota()).isEqualTo(5);
        assertThat(avaliacao.getSobrecapaAusente()).isTrue();
    }

    @Test
    @DisplayName("Qualquer eliminatorio recusa o livro, mesmo com todos os criterios em 5")
    void eliminatorioRecusaOLivro() {
        AvaliacaoRequest request = perfeito(List.of(Eliminatorio.MOFO), false, false);

        assertThatThrownBy(() -> service.avaliar(request))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("mofo")
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus())
                        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
    }

    @Test
    @DisplayName("Lista de eliminatorios ausente e tratada como vazia")
    void eliminatoriosNulosSaoIgnorados() {
        assertThat(service.avaliar(perfeito(null, false, false)).getNota()).isEqualTo(5);
    }

    @Test
    @DisplayName("Questionario lista os 6 criterios e todos os eliminatorios")
    void questionarioTemTodosOsCriterios() {
        QuestionarioResponse questionario = service.questionario();

        assertThat(questionario.criterios())
                .extracting("campo")
                .containsExactly("capa", "lombada", "paginas", "marcacoes", "manchas", "odor");
        assertThat(questionario.eliminatorios()).hasSize(Eliminatorio.values().length);
    }

    private AvaliacaoRequest perfeito(List<Eliminatorio> eliminatorios, boolean didatico, boolean semSobrecapa) {
        return new AvaliacaoRequest(
                EstadoCapa.SEM_DESGASTE, EstadoLombada.RETA, EstadoPaginas.SEM_DOBRAS,
                EstadoMarcacoes.NENHUMA, EstadoManchas.NENHUMA, EstadoOdor.NENHUM,
                eliminatorios, didatico, semSobrecapa);
    }
}
