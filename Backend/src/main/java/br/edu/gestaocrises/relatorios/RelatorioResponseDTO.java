package br.edu.gestaocrises.relatorios;

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
public class RelatorioResponseDTO {
    private Long id;
    private Long criseId;
    private String titulo;
    private String conteudo;
    private OffsetDateTime dataGeracao;
    private Long geradorId;
    private String geradorNome;
}
