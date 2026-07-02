package br.edu.gestaocrises.auditoria;

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
@Table(name = "auditoria_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A ação de auditoria é obrigatória")
    @Column(nullable = false, length = 200)
    private String acao;

    @NotBlank(message = "A entidade de auditoria é obrigatória")
    @Column(nullable = false, length = 100)
    private String entidade;

    @Column(name = "entidade_id")
    private Long entidadeId;

    @Column(columnDefinition = "TEXT")
    private String detalhes;

    @NotNull(message = "A data do log é obrigatória")
    @Column(nullable = false)
    private OffsetDateTime dataRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
