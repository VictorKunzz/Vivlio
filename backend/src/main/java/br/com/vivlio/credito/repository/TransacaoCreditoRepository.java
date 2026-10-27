package br.com.vivlio.credito.repository;

import br.com.vivlio.credito.entity.TipoTransacao;
import br.com.vivlio.credito.entity.TransacaoCredito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransacaoCreditoRepository extends JpaRepository<TransacaoCredito, Long> {

    List<TransacaoCredito> findByUsuarioIdOrderByIdDesc(Long usuarioId);

    long countByUsuarioIdAndTipo(Long usuarioId, TipoTransacao tipo);
}
