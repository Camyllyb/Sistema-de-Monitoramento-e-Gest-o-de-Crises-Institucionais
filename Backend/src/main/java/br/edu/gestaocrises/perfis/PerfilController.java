package br.edu.gestaocrises.perfis;

import br.edu.gestaocrises.common.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/perfis")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<PerfilResponseDTO>>> listar() {
        List<PerfilResponseDTO> perfis = perfilService.listar();
        return ResponseEntity.ok(ApiResponseDTO.<List<PerfilResponseDTO>>builder()
                .status(200)
                .mensagem("Perfis listados com sucesso")
                .dados(perfis)
                .build());
    }
}
