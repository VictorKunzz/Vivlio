package br.com.vivlio.resgate.service;

import br.com.vivlio.credito.service.CreditoService;
import br.com.vivlio.livro.dto.LivroResponse;
import br.com.vivlio.livro.entity.Livro;
import br.com.vivlio.livro.entity.StatusLivro;
import br.com.vivlio.livro.repository.LivroRepository;
import br.com.vivlio.resgate.dto.ResgateResponse;
import br.com.vivlio.shared.exception.NegocioException;
import br.com.vivlio.usuario.entity.Usuario;
import br.com.vivlio.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResgateService {

    // DT-12: doar e resgatar o mesmo livro é neutro em créditos, mas tira o livro do acervo
    // sem troca real e polui o histórico.
    static final String MENSAGEM_PROPRIO_LIVRO = "Você não pode resgatar um livro que você mesmo doou.";

    private static final String DESCRICAO_RESGATE = "Resgate de livro";

    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final CreditoService creditoService;

    @Transactional
    public ResgateResponse resgatar(Long usuarioId, Long livroId) {
        Usuario usuario = usuarioRepository.buscarParaAtualizarSaldo(usuarioId)
                .orElseThrow(() -> new NegocioException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado."));

        Livro livro = livroRepository.buscarParaResgate(livroId)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Livro não encontrado."));

        if (livro.getStatus() != StatusLivro.DISPONIVEL) {
            throw new NegocioException(HttpStatus.CONFLICT, "Este livro não está mais disponível para resgate.");
        }

        if (livro.getDoador().getId().equals(usuario.getId())) {
            throw new NegocioException(HttpStatus.CONFLICT, MENSAGEM_PROPRIO_LIVRO);
        }

        creditoService.debitar(usuario, DESCRICAO_RESGATE);

        livro.setStatus(StatusLivro.RESGATADO);
        livro.setRecebedor(usuario);

        return new ResgateResponse(LivroResponse.de(livro), usuario.getSaldoCreditos());
    }
}
