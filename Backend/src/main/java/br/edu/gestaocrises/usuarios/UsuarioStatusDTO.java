package br.edu.gestaocrises.usuarios;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioStatusDTO {

    @NotNull(message = "O campo ativo é obrigatório")
    private Boolean ativo;
}
