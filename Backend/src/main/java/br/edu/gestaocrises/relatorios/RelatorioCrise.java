package br.edu.gestaocrises.relatorios;

import br.edu.gestaocrises.crises.Crise;
import br.edu.gestaocrises.usuarios.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "relatorio_crise")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioCrise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O título do relatório é obrigatório")
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotBlank(message = "O conteúdo do relatório é obrigatório")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @NotNull(message = "A data de geração é obrigatória")
    @Column(nullable = false)
    private OffsetDateTime dataGeracao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crise_id", nullable = false)
    private Crise crise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gerador_id", nullable = false)
    private Usuario gerador;
}
