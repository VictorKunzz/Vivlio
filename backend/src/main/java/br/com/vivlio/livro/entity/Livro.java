package br.com.vivlio.livro.entity;

import br.com.vivlio.avaliacao.entity.AvaliacaoLivro;
import br.com.vivlio.usuario.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "livros")
@Getter
@Setter
@NoArgsConstructor
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String autor;

    @Column(nullable = false)
    private String genero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusLivro status = StatusLivro.DISPONIVEL;

    @ManyToOne
    @JoinColumn(name = "id_usuario_doador", nullable = false)
    private Usuario doador;

    @ManyToOne
    @JoinColumn(name = "id_usuario_recebedor")
    private Usuario recebedor;

    @Embedded
    private AvaliacaoLivro avaliacao;

    public Livro(String titulo, String autor, String genero, Usuario doador) {
        this.titulo = titulo;
        this.autor = autor;
        this.genero = genero;
        this.doador = doador;
        this.status = StatusLivro.DISPONIVEL;
    }

    public Livro(String titulo, String autor, String genero, Usuario doador, AvaliacaoLivro avaliacao) {
        this(titulo, autor, genero, doador);
        this.avaliacao = avaliacao;
    }
}
