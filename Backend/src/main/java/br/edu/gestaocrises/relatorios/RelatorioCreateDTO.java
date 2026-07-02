package br.edu.gestaocrises.relatorios;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioCreateDTO {

    @NotNull(message = "O ID da crise é obrigatório")
    private Long criseId;

    @NotBlank(message = "O título do relatório é obrigatório")
    private String titulo;

    @NotBlank(message = "O conteúdo do relatório é obrigatório")
    private String conteudo;
}
