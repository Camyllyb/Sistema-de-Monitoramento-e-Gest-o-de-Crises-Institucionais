package br.edu.gestaocrises.acoes;

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
public class AcaoCriseResponseDTO {
    private Long id;
    private Long criseId;
    private String tipo;
    private String descricao;
    private OffsetDateTime dataAcao;
    private Long executorId;
    private String executorNome;
}
