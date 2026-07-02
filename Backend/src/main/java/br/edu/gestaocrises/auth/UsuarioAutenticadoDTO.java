package br.edu.gestaocrises.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAutenticadoDTO {

    private Long id;
    private String nome;
    private String email;
    private String perfil;
}
