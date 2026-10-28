package br.com.vivlio.usuario.controller;

import br.com.vivlio.TesteIntegracao;
import br.com.vivlio.auth.service.JwtService;
import br.com.vivlio.livro.entity.Livro;
import br.com.vivlio.livro.entity.StatusLivro;
import br.com.vivlio.usuario.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DoacoesDoUsuarioTest extends TesteIntegracao {

    @Autowired
    private JwtService jwtService;

    private Usuario ana;
    private Usuario bruno;

    @BeforeEach
    void prepararUsuarios() {
        ana = usuarioRepository.save(new Usuario("Ana Souza", "ana@vivlio.com", "hash"));
        bruno = usuarioRepository.save(new Usuario("Bruno Lima", "bruno@vivlio.com", "hash"));
    }

    @Test
    @DisplayName("Doacoes do usuario exigem login")
    void deveExigirLogin() throws Exception {
        mockMvc.perform(get("/api/usuarios/me/doacoes")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Lista so os livros do usuario logado que ainda estao disponiveis, do mais recente ao mais antigo")
    void listaSoAsDoacoesDisponiveisDoUsuario() throws Exception {
        livroRepository.save(new Livro("Dom Casmurro", "Machado de Assis", "Romance", ana));
        livroRepository.save(new Livro("O Cortiço", "Aluísio Azevedo", "Naturalismo", ana));

        Livro resgatado = new Livro("Iracema", "José de Alencar", "Romance", ana);
        resgatado.setStatus(StatusLivro.RESGATADO);
        resgatado.setRecebedor(bruno);
        livroRepository.save(resgatado);

        livroRepository.save(new Livro("Capitães da Areia", "Jorge Amado", "Romance", bruno));

        mockMvc.perform(get("/api/usuarios/me/doacoes").header("Authorization", "Bearer " + token(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].titulo").value("O Cortiço"))
                .andExpect(jsonPath("$[1].titulo").value("Dom Casmurro"))
                .andExpect(jsonPath("$[0].status").value("DISPONIVEL"));
    }

    @Test
    @DisplayName("Usuario sem doacoes em aberto recebe lista vazia")
    void semDoacoesDevolveListaVazia() throws Exception {
        livroRepository.save(new Livro("Capitães da Areia", "Jorge Amado", "Romance", bruno));

        mockMvc.perform(get("/api/usuarios/me/doacoes").header("Authorization", "Bearer " + token(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private String token(Usuario usuario) {
        return jwtService.gerarToken(usuario.getEmail());
    }
}
