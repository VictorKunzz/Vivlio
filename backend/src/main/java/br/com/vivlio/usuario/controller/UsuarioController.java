package br.com.vivlio.usuario.controller;

import br.com.vivlio.livro.dto.LivroResponse;
import br.com.vivlio.livro.service.LivroService;
import br.com.vivlio.usuario.dto.UsuarioResponse;
import br.com.vivlio.usuario.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final LivroService livroService;

    @GetMapping("/me")
    public UsuarioResponse perfil(@AuthenticationPrincipal Usuario usuarioLogado) {
        return UsuarioResponse.de(usuarioLogado);
    }

    @GetMapping("/me/doacoes")
    public List<LivroResponse> doacoesDisponiveis(@AuthenticationPrincipal Usuario usuarioLogado) {
        return livroService.listarDoacoesDisponiveis(usuarioLogado.getId());
    }
}
