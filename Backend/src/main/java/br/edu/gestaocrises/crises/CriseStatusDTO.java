package br.edu.gestaocrises.crises;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriseStatusDTO {

    @NotNull(message = "O status é obrigatório")
    private StatusCrise status;
}
