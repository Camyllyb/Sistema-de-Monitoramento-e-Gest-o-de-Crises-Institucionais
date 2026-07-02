package br.edu.gestaocrises.auth;

import br.edu.gestaocrises.common.ApiResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints de autenticação e autorização JWT")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Realiza login e retorna JWT")
    public ApiResponseDTO<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ApiResponseDTO.<LoginResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .mensagem("Login realizado com sucesso")
                .dados(response)
                .build();
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retorna dados do usuário autenticado", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponseDTO<UsuarioAutenticadoDTO> me() {
        UsuarioAutenticadoDTO usuario = authService.me();
        return ApiResponseDTO.<UsuarioAutenticadoDTO>builder()
                .status(HttpStatus.OK.value())
                .mensagem("Usuário autenticado")
                .dados(usuario)
                .build();
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Encerra a sessão do usuário autenticado", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponseDTO<Map<String, String>> logout() {
        return ApiResponseDTO.<Map<String, String>>builder()
                .status(HttpStatus.OK.value())
                .mensagem("Logout realizado com sucesso")
                .dados(Map.of("info", "Token invalidado no lado do cliente"))
                .build();
    }

    @GetMapping("/admin-check")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verifica acesso administrativo", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponseDTO<Map<String, String>> adminCheck() {
        return ApiResponseDTO.<Map<String, String>>builder()
                .status(HttpStatus.OK.value())
                .mensagem("Acesso administrativo autorizado")
                .dados(Map.of("perfil", "ADMIN"))
                .build();
    }
}
