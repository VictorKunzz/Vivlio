package br.com.vivlio.resgate.controller;

import br.com.vivlio.TesteIntegracao;
import br.com.vivlio.auth.service.JwtService;
import br.com.vivlio.credito.entity.TipoTransacao;
import br.com.vivlio.credito.entity.TransacaoCredito;
import br.com.vivlio.livro.entity.Livro;
import br.com.vivlio.livro.entity.StatusLivro;
import br.com.vivlio.resgate.dto.ResgateRequest;
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

class ResgateControllerTest extends TesteIntegracao {

    private static final String MENSAGEM_RN_CRITICA =
            "Você precisa de pelo menos 1 crédito ativo para resgatar um livro. Doe um livro para ganhar créditos.";

    @Autowired
    private JwtService jwtService;

    private Usuario leitor;
    private Usuario doador;
    private Livro livro;
    private String token;

    @BeforeEach
    void prepararCenario() {
        doador = usuarioRepository.save(new Usuario("Ana Souza", "ana@vivlio.com", "hash"));
        leitor = usuarioRepository.save(new Usuario("João Dev", "joao@vivlio.com", "hash"));
        livro = livroRepository.save(new Livro("Dom Casmurro", "Machado de Assis", "Romance", doador));
        token = jwtService.gerarToken(leitor.getEmail());
    }

    private void darCreditos(int quantidade) {
        leitor.setSaldoCreditos(quantidade);
        leitor = usuarioRepository.save(leitor);
    }

    @Test
    @DisplayName("TC09 - resgate com saldo suficiente debita 1 credito e vincula o livro ao usuario")
    void deveResgatarComSaldoSuficiente() throws Exception {
        darCreditos(1);

        mockMvc.perform(resgate(livro.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.livro.status").value("RESGATADO"))
                .andExpect(jsonPath("$.saldoCreditos").value(0));

        Livro resgatado = livroRepository.findById(livro.getId()).orElseThrow();
        assertThat(resgatado.getStatus()).isEqualTo(StatusLivro.RESGATADO);
        assertThat(resgatado.getRecebedor().getId()).isEqualTo(leitor.getId());
        assertThat(usuarioRepository.findById(leitor.getId()).orElseThrow().getSaldoCreditos()).isZero();
    }

    @Test
    @DisplayName("TC11 - RN CRITICA: resgate com saldo 0 e bloqueado no backend e nada muda no banco")
    void naoDeveResgatarSemCredito() throws Exception {
        mockMvc.perform(resgate(livro.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_RN_CRITICA));

        Livro naoResgatado = livroRepository.findById(livro.getId()).orElseThrow();
        assertThat(naoResgatado.getStatus()).isEqualTo(StatusLivro.DISPONIVEL);
        assertThat(naoResgatado.getRecebedor()).isNull();
        assertThat(usuarioRepository.findById(leitor.getId()).orElseThrow().getSaldoCreditos()).isZero();
        assertThat(transacaoCreditoRepository.count()).isZero();
    }

    @Test
    @DisplayName("TC12 - resgate registra transacao de SAIDA com a descricao oficial")
    void deveRegistrarTransacaoDeSaida() throws Exception {
        darCreditos(1);

        mockMvc.perform(resgate(livro.getId())).andExpect(status().isOk());

        List<TransacaoCredito> transacoes = transacaoCreditoRepository.findByUsuarioIdOrderByIdDesc(leitor.getId());
        assertThat(transacoes).hasSize(1);
        assertThat(transacoes.get(0).getTipo()).isEqualTo(TipoTransacao.SAIDA);
        assertThat(transacoes.get(0).getDescricao()).isEqualTo("Resgate de livro");
    }

    @Test
    @DisplayName("TC13 - livro ja resgatado nao pode ser resgatado de novo e some do acervo")
    void naoDeveResgatarLivroJaResgatado() throws Exception {
        darCreditos(2);
        mockMvc.perform(resgate(livro.getId())).andExpect(status().isOk());

        mockMvc.perform(resgate(livro.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Este livro não está mais disponível para resgate."));

        mockMvc.perform(get("/api/livros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        assertThat(usuarioRepository.findById(leitor.getId()).orElseThrow().getSaldoCreditos()).isEqualTo(1);
    }

    @Test
    @DisplayName("DT-12 - doador nao pode resgatar o proprio livro: 409 e nada muda no banco")
    void naoDeveResgatarOProprioLivro() throws Exception {
        doador.setSaldoCreditos(1);
        doador = usuarioRepository.save(doador);

        mockMvc.perform(post("/api/resgates")
                        .header("Authorization", "Bearer " + jwtService.gerarToken(doador.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResgateRequest(livro.getId()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Você não pode resgatar um livro que você mesmo doou."));

        Livro naoResgatado = livroRepository.findById(livro.getId()).orElseThrow();
        assertThat(naoResgatado.getStatus()).isEqualTo(StatusLivro.DISPONIVEL);
        assertThat(naoResgatado.getRecebedor()).isNull();
        assertThat(usuarioRepository.findById(doador.getId()).orElseThrow().getSaldoCreditos()).isEqualTo(1);
        assertThat(transacaoCreditoRepository.count()).isZero();
    }

    @Test
    @DisplayName("DT-12 - o acervo informa quem doou cada livro (doadorId)")
    void acervoInformaODoador() throws Exception {
        mockMvc.perform(get("/api/livros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].doadorId").value(doador.getId()));
    }

    @Test
    @DisplayName("Resgate de livro inexistente retorna 404")
    void naoDeveResgatarLivroInexistente() throws Exception {
        darCreditos(1);

        mockMvc.perform(resgate(99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Livro não encontrado."));
    }

    @Test
    @DisplayName("RNF02 - resgate sem token retorna 401 e o livro continua disponivel")
    void deveBloquearResgateSemToken() throws Exception {
        mockMvc.perform(post("/api/resgates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResgateRequest(livro.getId()))))
                .andExpect(status().isUnauthorized());

        assertThat(livroRepository.findById(livro.getId()).orElseThrow().getStatus())
                .isEqualTo(StatusLivro.DISPONIVEL);
    }

    @Test
    @DisplayName("Resgate sem informar o livro retorna 400")
    void deveExigirLivroId() throws Exception {
        mockMvc.perform(post("/api/resgates")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.livroId").isNotEmpty());
    }

    private MockHttpServletRequestBuilder resgate(Long livroId) throws Exception {
        return post("/api/resgates")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ResgateRequest(livroId)));
    }
}
