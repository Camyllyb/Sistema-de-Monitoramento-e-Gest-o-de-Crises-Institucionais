package br.edu.gestaocrises.perfis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfilResponseDTO {
    private Long id;
    private String nome;
}
