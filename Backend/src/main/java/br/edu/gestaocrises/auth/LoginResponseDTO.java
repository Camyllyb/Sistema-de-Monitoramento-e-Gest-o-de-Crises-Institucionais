package br.edu.gestaocrises.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    private String token;
    private String tipo;
    private long expiraEm;
    private UsuarioAutenticadoDTO usuario;
}
