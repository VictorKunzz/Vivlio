package br.com.vivlio.livro.service;

import br.com.vivlio.avaliacao.entity.AvaliacaoLivro;
import br.com.vivlio.avaliacao.service.AvaliacaoService;
import br.com.vivlio.credito.service.CreditoService;
import br.com.vivlio.livro.dto.DoacaoRequest;
import br.com.vivlio.livro.dto.DoacaoResponse;
import br.com.vivlio.livro.dto.LivroResponse;
import br.com.vivlio.livro.entity.Livro;
import br.com.vivlio.livro.entity.StatusLivro;
import br.com.vivlio.livro.repository.LivroRepository;
import br.com.vivlio.shared.exception.NegocioException;
import br.com.vivlio.usuario.entity.Usuario;
import br.com.vivlio.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LivroService {

    private static final String DESCRICAO_DOACAO = "Doação de livro";

    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final CreditoService creditoService;
    private final AvaliacaoService avaliacaoService;

    @Transactional(readOnly = true)
    public List<LivroResponse> listarDisponiveis(String busca, String genero) {
        return livroRepository.buscar(StatusLivro.DISPONIVEL, texto(busca), texto(genero))
                .stream()
                .map(LivroResponse::de)
                .toList();
    }

    /** Livros que o usuário doou e que ninguém resgatou ainda. */
    @Transactional(readOnly = true)
    public List<LivroResponse> listarDoacoesDisponiveis(Long usuarioId) {
        return livroRepository.findByDoadorIdAndStatusOrderByIdDesc(usuarioId, StatusLivro.DISPONIVEL)
                .stream()
                .map(LivroResponse::de)
                .toList();
    }

    @Transactional
    public DoacaoResponse doar(Long usuarioId, DoacaoRequest request) {
        Usuario doador = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NegocioException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado."));

        AvaliacaoLivro avaliacao = avaliacaoService.avaliar(request.avaliacao());

        Livro livro = livroRepository.save(new Livro(
                request.titulo().trim(),
                request.autor().trim(),
                request.genero().trim(),
                doador,
                avaliacao
        ));

        creditoService.creditar(doador, DESCRICAO_DOACAO);

        return new DoacaoResponse(LivroResponse.de(livro), doador.getSaldoCreditos());
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
