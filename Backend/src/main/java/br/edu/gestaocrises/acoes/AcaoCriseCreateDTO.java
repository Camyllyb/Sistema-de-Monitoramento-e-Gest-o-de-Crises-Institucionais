package br.edu.gestaocrises.acoes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcaoCriseCreateDTO {

    @NotNull(message = "O tipo da ação é obrigatório")
    private TipoAcaoCrise tipo;

    @NotBlank(message = "A descrição da ação é obrigatória")
    private String descricao;

    private OffsetDateTime dataAcao;
}
