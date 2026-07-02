package br.edu.gestaocrises.crises;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriseUpdateDTO {

    @NotBlank(message = "O título é obrigatório")
    private String titulo;

    @NotBlank(message = "A descrição é obrigatória")
    private String descricao;

    @NotNull(message = "O tipo é obrigatório")
    private TipoCriseNome tipo;

    @NotNull(message = "O nível é obrigatório")
    private NivelCrise nivel;

    @NotNull(message = "O responsável é obrigatório")
    private Long responsavelId;
}
