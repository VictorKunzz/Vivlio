package br.com.vivlio.livro.repository;

import br.com.vivlio.livro.entity.Livro;
import br.com.vivlio.livro.entity.StatusLivro;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    @Query("""
            select l from Livro l
            where l.status = :status
              and (lower(l.titulo) like lower(concat('%', :busca, '%'))
                   or lower(l.autor) like lower(concat('%', :busca, '%')))
              and lower(l.genero) like lower(concat('%', :genero, '%'))
            order by l.id desc
            """)
    List<Livro> buscar(@Param("status") StatusLivro status,
                       @Param("busca") String busca,
                       @Param("genero") String genero);

    List<Livro> findByDoadorIdAndStatusOrderByIdDesc(Long doadorId, StatusLivro status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Livro l where l.id = :id")
    Optional<Livro> buscarParaResgate(@Param("id") Long id);
}
