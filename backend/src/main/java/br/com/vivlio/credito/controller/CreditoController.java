package br.com.vivlio.credito.controller;

import br.com.vivlio.credito.dto.ExtratoResponse;
import br.com.vivlio.credito.service.CreditoService;
import br.com.vivlio.usuario.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/creditos")
@RequiredArgsConstructor
public class CreditoController {

    private final CreditoService creditoService;

    @GetMapping("/extrato")
    public ExtratoResponse extrato(@AuthenticationPrincipal Usuario usuarioLogado) {
        return creditoService.extrato(usuarioLogado);
    }
}
