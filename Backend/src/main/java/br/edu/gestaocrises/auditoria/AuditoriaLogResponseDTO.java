package br.edu.gestaocrises.auditoria;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditoriaLogResponseDTO {
    private Long id;
    private Long usuarioId;
    private String usuarioNome;
    private String acao;
    private String entidade;
    private Long entidadeId;
    private String detalhes;
    private OffsetDateTime dataRegistro;
}
