package br.edu.gestaocrises.crises;

import br.edu.gestaocrises.usuarios.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "crise")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Crise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O título da crise é obrigatório")
    @Size(max = 200, message = "O título da crise deve ter no máximo 200 caracteres")
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotBlank(message = "A descrição da crise é obrigatória")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoCriseNome tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NivelCrise nivel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCrise status;

    @NotNull(message = "A data de abertura é obrigatória")
    @Column(nullable = false)
    private OffsetDateTime dataAbertura;

    @Column(name = "data_atualizacao")
    private OffsetDateTime dataAtualizacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criador_id", nullable = false)
    private Usuario criador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;
}
