package br.edu.gestaocrises.crises;

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
public class CriseResponseDTO {
    private Long id;
    private String titulo;
    private String descricao;
    private String tipo;
    private String nivel;
    private String status;
    private Long responsavelId;
    private String responsavelNome;
    private Long criadoPorId;
    private String criadoPorNome;
    private OffsetDateTime dataCriacao;
    private OffsetDateTime dataAtualizacao;
}
