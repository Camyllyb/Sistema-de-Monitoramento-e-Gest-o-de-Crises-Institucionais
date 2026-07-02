package br.edu.gestaocrises.acoes;

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
@Table(name = "acao_crise")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcaoCrise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A descrição da ação é obrigatória")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoAcaoCrise tipo;

    @NotNull(message = "A data da ação é obrigatória")
    @Column(nullable = false)
    private OffsetDateTime dataAcao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crise_id", nullable = false)
    private Crise crise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executor_id", nullable = false)
    private Usuario executor;
}
