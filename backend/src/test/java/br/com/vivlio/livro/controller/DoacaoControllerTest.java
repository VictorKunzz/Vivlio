package br.com.vivlio.livro.controller;

import br.com.vivlio.TesteIntegracao;
import br.com.vivlio.avaliacao.dto.AvaliacaoRequest;
import br.com.vivlio.avaliacao.entity.Eliminatorio;
import br.com.vivlio.avaliacao.entity.EstadoCapa;
import br.com.vivlio.avaliacao.entity.EstadoLombada;
import br.com.vivlio.avaliacao.entity.EstadoManchas;
import br.com.vivlio.avaliacao.entity.EstadoMarcacoes;
import br.com.vivlio.avaliacao.entity.EstadoOdor;
import br.com.vivlio.avaliacao.entity.EstadoPaginas;
import br.com.vivlio.auth.service.JwtService;
import br.com.vivlio.credito.entity.TipoTransacao;
import br.com.vivlio.credito.entity.TransacaoCredito;
import br.com.vivlio.livro.dto.DoacaoRequest;
import br.com.vivlio.livro.entity.Livro;
import br.com.vivlio.usuario.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DoacaoControllerTest extends TesteIntegracao {

    @Autowired
    private JwtService jwtService;

    // Notas 4, 5, 3, 4, 5, 5: o exemplo da ficha de avaliação, que resulta em 3 estrelas.
    private static final AvaliacaoRequest BOM_ESTADO = new AvaliacaoRequest(
            EstadoCapa.CANTOS_LEVEMENTE_GASTOS, EstadoLombada.RETA, EstadoPaginas.VARIAS_ORELHAS,
            EstadoMarcacoes.NOME_DO_DONO, EstadoManchas.NENHUMA, EstadoOdor.NENHUM,
            List.of(), false, false);

    private Usuario doador;
    private String token;

    @BeforeEach
    void prepararUsuario() {
        doador = usuarioRepository.save(new Usuario("Ana Souza", "ana@vivlio.com", "hash"));
        token = jwtService.gerarToken(doador.getEmail());
    }

    @Test
    @DisplayName("TC05 - doacao com dados completos salva o livro como DISPONIVEL e credita +1")
    void deveCadastrarDoacaoECreditarUmCredito() throws Exception {
        mockMvc.perform(doacao(new DoacaoRequest("Dom Casmurro", "Machado de Assis", "Romance", BOM_ESTADO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.livro.titulo").value("Dom Casmurro"))
                .andExpect(jsonPath("$.livro.status").value("DISPONIVEL"))
                .andExpect(jsonPath("$.livro.doador").value("Ana Souza"))
                .andExpect(jsonPath("$.saldoCreditos").value(1));

        assertThat(usuarioRepository.findById(doador.getId()).orElseThrow().getSaldoCreditos()).isEqualTo(1);
        assertThat(livroRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("TC06 - doacao sem campo obrigatorio retorna 400 e nao gera credito")
    void naoDeveCadastrarDoacaoIncompleta() throws Exception {
        mockMvc.perform(post("/api/livros")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\",\"autor\":\"\",\"genero\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.titulo").isNotEmpty())
                .andExpect(jsonPath("$.campos.autor").isNotEmpty())
                .andExpect(jsonPath("$.campos.genero").isNotEmpty());

        assertThat(livroRepository.count()).isZero();
        assertThat(usuarioRepository.findById(doador.getId()).orElseThrow().getSaldoCreditos()).isZero();
        assertThat(transacaoCreditoRepository.count()).isZero();
    }

    @Test
    @DisplayName("TC07 - doacao registra transacao de ENTRADA com a descricao oficial")
    void deveRegistrarTransacaoDeEntrada() throws Exception {
        mockMvc.perform(doacao(new DoacaoRequest("O Cortiço", "Aluísio Azevedo", "Naturalismo", BOM_ESTADO)))
                .andExpect(status().isCreated());

        List<TransacaoCredito> transacoes = transacaoCreditoRepository.findByUsuarioIdOrderByIdDesc(doador.getId());
        assertThat(transacoes).hasSize(1);
        assertThat(transacoes.get(0).getTipo()).isEqualTo(TipoTransacao.ENTRADA);
        assertThat(transacoes.get(0).getDescricao()).isEqualTo("Doação de livro");
    }

    @Test
    @DisplayName("TC08 - proporcao estrita 1:1 - tres doacoes geram exatamente 3 creditos")
    void deveManterProporcaoUmParaUm() throws Exception {
        mockMvc.perform(doacao(new DoacaoRequest("Livro 1", "Autor 1", "Romance", BOM_ESTADO))).andExpect(status().isCreated());
        mockMvc.perform(doacao(new DoacaoRequest("Livro 2", "Autor 2", "Romance", BOM_ESTADO))).andExpect(status().isCreated());
        mockMvc.perform(doacao(new DoacaoRequest("Livro 3", "Autor 3", "Romance", BOM_ESTADO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldoCreditos").value(3));

        assertThat(usuarioRepository.findById(doador.getId()).orElseThrow().getSaldoCreditos()).isEqualTo(3);
        assertThat(transacaoCreditoRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("RNF02 - doacao sem token retorna 401 e nao salva nada")
    void deveBloquearDoacaoSemToken() throws Exception {
        mockMvc.perform(post("/api/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DoacaoRequest("Dom Casmurro", "Machado de Assis", "Romance", BOM_ESTADO))))
                .andExpect(status().isUnauthorized());

        assertThat(livroRepository.count()).isZero();
    }

    @Test
    @DisplayName("Livro doado passa a aparecer no acervo publico")
    void livroDoadoApareceNoAcervo() throws Exception {
        mockMvc.perform(doacao(new DoacaoRequest("Dom Casmurro", "Machado de Assis", "Romance", BOM_ESTADO)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/livros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Dom Casmurro"));
    }

    @Test
    @DisplayName("Doacao calcula a nota pelo questionario e a expoe no acervo")
    void deveCalcularANotaDoLivro() throws Exception {
        mockMvc.perform(doacao(new DoacaoRequest("Dom Casmurro", "Machado de Assis", "Romance", BOM_ESTADO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.livro.nota").value(3));

        mockMvc.perform(get("/api/livros"))
                .andExpect(jsonPath("$[0].nota").value(3))
                .andExpect(jsonPath("$[0].sobrecapaAusente").value(false));

        Livro salvo = livroRepository.findAll().get(0);
        assertThat(salvo.getAvaliacao().getPaginas()).isEqualTo(EstadoPaginas.VARIAS_ORELHAS);
    }

    @Test
    @DisplayName("Livro com eliminatorio e recusado com 422, nao entra no acervo e nao gera credito")
    void deveRecusarLivroComEliminatorio() throws Exception {
        AvaliacaoRequest comMofo = new AvaliacaoRequest(
                EstadoCapa.SEM_DESGASTE, EstadoLombada.RETA, EstadoPaginas.SEM_DOBRAS,
                EstadoMarcacoes.NENHUMA, EstadoManchas.NENHUMA, EstadoOdor.NENHUM,
                List.of(Eliminatorio.MOFO), false, false);

        mockMvc.perform(doacao(new DoacaoRequest("Dom Casmurro", "Machado de Assis", "Romance", comMofo)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());

        assertThat(livroRepository.count()).isZero();
        assertThat(transacaoCreditoRepository.count()).isZero();
        assertThat(usuarioRepository.findById(doador.getId()).orElseThrow().getSaldoCreditos()).isZero();
    }

    @Test
    @DisplayName("Doacao sem questionario retorna 400 e nao gera credito")
    void naoDeveCadastrarDoacaoSemQuestionario() throws Exception {
        mockMvc.perform(doacao(new DoacaoRequest("Dom Casmurro", "Machado de Assis", "Romance", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.avaliacao").isNotEmpty());

        assertThat(livroRepository.count()).isZero();
        assertThat(transacaoCreditoRepository.count()).isZero();
    }

    @Test
    @DisplayName("Questionario incompleto retorna 400 apontando o criterio faltante")
    void naoDeveCadastrarDoacaoComQuestionarioIncompleto() throws Exception {
        AvaliacaoRequest semOdor = new AvaliacaoRequest(
                EstadoCapa.SEM_DESGASTE, EstadoLombada.RETA, EstadoPaginas.SEM_DOBRAS,
                EstadoMarcacoes.NENHUMA, EstadoManchas.NENHUMA, null,
                List.of(), false, false);

        mockMvc.perform(doacao(new DoacaoRequest("Dom Casmurro", "Machado de Assis", "Romance", semOdor)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos['avaliacao.odor']").isNotEmpty());

        assertThat(livroRepository.count()).isZero();
    }

    @Test
    @DisplayName("Opcao inexistente no questionario retorna 400")
    void naoDeveAceitarOpcaoInexistente() throws Exception {
        mockMvc.perform(post("/api/livros")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"A\",\"autor\":\"B\",\"genero\":\"C\","
                                + "\"avaliacao\":{\"capa\":\"CINCO_ESTRELAS\"}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());

        assertThat(livroRepository.count()).isZero();
    }

    @Test
    @DisplayName("Questionario de avaliacao exige login e nao expoe as notas")
    void deveEntregarQuestionario() throws Exception {
        mockMvc.perform(get("/api/avaliacao/questionario").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criterios.length()").value(6))
                .andExpect(jsonPath("$.criterios[0].opcoes[0].codigo").value("SEM_DESGASTE"))
                .andExpect(jsonPath("$.criterios[0].opcoes[0].nota").doesNotExist())
                .andExpect(jsonPath("$.eliminatorios.length()").value(6));

        mockMvc.perform(get("/api/avaliacao/questionario")).andExpect(status().isUnauthorized());
    }

    private MockHttpServletRequestBuilder doacao(DoacaoRequest request) throws Exception {
        return post("/api/livros")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request));
    }
}
