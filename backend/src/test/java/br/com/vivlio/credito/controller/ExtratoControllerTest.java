package br.com.vivlio.credito.controller;

import br.com.vivlio.TesteIntegracao;
import br.com.vivlio.auth.service.JwtService;
import br.com.vivlio.avaliacao.dto.AvaliacaoRequest;
import br.com.vivlio.avaliacao.entity.EstadoCapa;
import br.com.vivlio.avaliacao.entity.EstadoLombada;
import br.com.vivlio.avaliacao.entity.EstadoManchas;
import br.com.vivlio.avaliacao.entity.EstadoMarcacoes;
import br.com.vivlio.avaliacao.entity.EstadoOdor;
import br.com.vivlio.avaliacao.entity.EstadoPaginas;
import br.com.vivlio.credito.entity.TipoTransacao;
import br.com.vivlio.livro.dto.DoacaoRequest;
import br.com.vivlio.livro.entity.Livro;
import br.com.vivlio.livro.service.LivroService;
import br.com.vivlio.resgate.service.ResgateService;
import br.com.vivlio.usuario.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExtratoControllerTest extends TesteIntegracao {

    private static final AvaliacaoRequest BOM_ESTADO = new AvaliacaoRequest(
            EstadoCapa.SEM_DESGASTE, EstadoLombada.RETA, EstadoPaginas.SEM_DOBRAS,
            EstadoMarcacoes.NENHUMA, EstadoManchas.NENHUMA, EstadoOdor.NENHUM,
            List.of(), false, false);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private LivroService livroService;

    @Autowired
    private ResgateService resgateService;

    private Usuario outroDoador;
    private Usuario leitor;

    @BeforeEach
    void prepararUsuarios() {
        outroDoador = usuarioRepository.save(new Usuario("Ana Souza", "ana@vivlio.com", "hash"));
        leitor = usuarioRepository.save(new Usuario("João Dev", "joao@vivlio.com", "hash"));
    }

    @Test
    @DisplayName("DT-05 - extrato sem token retorna 401")
    void deveExigirLogin() throws Exception {
        mockMvc.perform(get("/api/creditos/extrato")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DT-05 - usuario novo tem extrato vazio e saldo 0")
    void extratoDeUsuarioNovoEhVazio() throws Exception {
        mockMvc.perform(extrato(leitor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(0))
                .andExpect(jsonPath("$.totalEntradas").value(0))
                .andExpect(jsonPath("$.totalSaidas").value(0))
                .andExpect(jsonPath("$.transacoes.length()").value(0));
    }

    @Test
    @DisplayName("DT-05 - apos 3 doacoes e 2 resgates, saldo = entradas - saidas e o extrato lista as 5 transacoes")
    void saldoBateComTransacoesAposDoacoesEResgates() throws Exception {
        for (int i = 1; i <= 3; i++) {
            livroService.doar(leitor.getId(), doacao("Livro do leitor " + i));
        }
        Long primeiro = livroDoOutroDoador("Dom Casmurro");
        Long segundo = livroDoOutroDoador("O Cortiço");
        resgateService.resgatar(leitor.getId(), primeiro);
        resgateService.resgatar(leitor.getId(), segundo);

        mockMvc.perform(extrato(leitor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(1))
                .andExpect(jsonPath("$.totalEntradas").value(3))
                .andExpect(jsonPath("$.totalSaidas").value(2))
                .andExpect(jsonPath("$.transacoes.length()").value(5))
                // Mais recente primeiro
                .andExpect(jsonPath("$.transacoes[0].tipo").value("SAIDA"))
                .andExpect(jsonPath("$.transacoes[0].descricao").value("Resgate de livro"))
                .andExpect(jsonPath("$.transacoes[4].tipo").value("ENTRADA"))
                .andExpect(jsonPath("$.transacoes[4].descricao").value("Doação de livro"))
                .andExpect(jsonPath("$.transacoes[0].data").isNotEmpty());

        assertSaldoConsistente(leitor.getId());
    }

    @Test
    @DisplayName("DT-05 - resgates simultaneos mantem saldo = entradas - saidas")
    void saldoBateComTransacoesEmResgatesSimultaneos() throws Exception {
        livroService.doar(leitor.getId(), doacao("Livro do leitor"));
        Long primeiro = livroDoOutroDoador("Dom Casmurro");
        Long segundo = livroDoOutroDoador("O Cortiço");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Callable<Object>> tarefas = List.of(
                () -> resgateService.resgatar(leitor.getId(), primeiro),
                () -> resgateService.resgatar(leitor.getId(), segundo)
        );
        executor.invokeAll(tarefas);
        executor.shutdown();

        assertThat(usuarioRepository.findById(leitor.getId()).orElseThrow().getSaldoCreditos()).isZero();
        assertSaldoConsistente(leitor.getId());
    }

    private void assertSaldoConsistente(Long usuarioId) {
        int saldo = usuarioRepository.findById(usuarioId).orElseThrow().getSaldoCreditos();
        long entradas = transacaoCreditoRepository.countByUsuarioIdAndTipo(usuarioId, TipoTransacao.ENTRADA);
        long saidas = transacaoCreditoRepository.countByUsuarioIdAndTipo(usuarioId, TipoTransacao.SAIDA);
        assertThat(saldo).isEqualTo(entradas - saidas);
    }

    private Long livroDoOutroDoador(String titulo) {
        return livroRepository.save(new Livro(titulo, "Autor", "Romance", outroDoador)).getId();
    }

    private DoacaoRequest doacao(String titulo) {
        return new DoacaoRequest(titulo, "Autor", "Romance", BOM_ESTADO);
    }

    private MockHttpServletRequestBuilder extrato(Usuario usuario) {
        return get("/api/creditos/extrato").header("Authorization", "Bearer " + jwtService.gerarToken(usuario.getEmail()));
    }
}
