package br.com.vivlio.avaliacao.controller;

import br.com.vivlio.avaliacao.dto.QuestionarioResponse;
import br.com.vivlio.avaliacao.service.AvaliacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/avaliacao")
@RequiredArgsConstructor
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    @GetMapping("/questionario")
    public QuestionarioResponse questionario() {
        return avaliacaoService.questionario();
    }
}
