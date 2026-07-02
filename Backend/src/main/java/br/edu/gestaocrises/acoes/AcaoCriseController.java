package br.edu.gestaocrises.acoes;

import br.edu.gestaocrises.common.ApiResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crises/{criseId}/acoes")
@RequiredArgsConstructor
public class AcaoCriseController {

    private final AcaoCriseService acaoCriseService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ANALISTA')")
    public ResponseEntity<ApiResponseDTO<List<AcaoCriseResponseDTO>>> listar(
            @PathVariable Long criseId) {
        return ResponseEntity.ok(ApiResponseDTO.<List<AcaoCriseResponseDTO>>builder()
                .status(200)
                .mensagem("Ações listadas com sucesso")
                .dados(acaoCriseService.listarPorCrise(criseId))
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ANALISTA')")
    public ResponseEntity<ApiResponseDTO<AcaoCriseResponseDTO>> registrar(
            @PathVariable Long criseId,
            @Valid @RequestBody AcaoCriseCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.<AcaoCriseResponseDTO>builder()
                        .status(201)
                        .mensagem("Ação registrada com sucesso")
                        .dados(acaoCriseService.registrarAcao(criseId, dto))
                        .build());
    }
}
