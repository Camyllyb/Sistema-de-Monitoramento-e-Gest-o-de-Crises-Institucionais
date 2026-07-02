package br.edu.gestaocrises.crises;

import br.edu.gestaocrises.common.ApiResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crises")
@RequiredArgsConstructor
public class CriseController {

    private final CriseService criseService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ANALISTA')")
    public ResponseEntity<ApiResponseDTO<List<CriseResponseDTO>>> listar(
            @RequestParam(required = false) StatusCrise status,
            @RequestParam(required = false) NivelCrise nivel,
            @RequestParam(required = false) Long responsavelId,
            @RequestParam(required = false) Long criadoPorId) {

        return ResponseEntity.ok(ApiResponseDTO.<List<CriseResponseDTO>>builder()
                .status(200)
                .mensagem("Crises listadas com sucesso")
                .dados(criseService.listar(status, nivel, responsavelId, criadoPorId))
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ANALISTA')")
    public ResponseEntity<ApiResponseDTO<CriseResponseDTO>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.<CriseResponseDTO>builder()
                .status(200)
                .mensagem("Crise encontrada com sucesso")
                .dados(criseService.buscarPorId(id))
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ApiResponseDTO<CriseResponseDTO>> criar(
            @Valid @RequestBody CriseCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.<CriseResponseDTO>builder()
                        .status(201)
                        .mensagem("Crise criada com sucesso")
                        .dados(criseService.criar(dto))
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ApiResponseDTO<CriseResponseDTO>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CriseUpdateDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.<CriseResponseDTO>builder()
                .status(200)
                .mensagem("Crise atualizada com sucesso")
                .dados(criseService.atualizar(id, dto))
                .build());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ApiResponseDTO<CriseResponseDTO>> alterarStatus(
            @PathVariable Long id,
            @Valid @RequestBody CriseStatusDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.<CriseResponseDTO>builder()
                .status(200)
                .mensagem("Status da crise atualizado com sucesso")
                .dados(criseService.alterarStatus(id, dto))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> excluir(@PathVariable Long id) {
        criseService.excluir(id);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .status(200)
                .mensagem("Crise excluída com sucesso")
                .build());
    }
}
