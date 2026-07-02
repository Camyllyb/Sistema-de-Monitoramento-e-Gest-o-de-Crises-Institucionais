package br.edu.gestaocrises.relatorios;

import br.edu.gestaocrises.common.ApiResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ApiResponseDTO<RelatorioResponseDTO>> gerar(
            @Valid @RequestBody RelatorioCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.<RelatorioResponseDTO>builder()
                        .status(201)
                        .mensagem("Relatório gerado com sucesso")
                        .dados(relatorioService.gerarRelatorio(dto))
                        .build());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ApiResponseDTO<List<RelatorioResponseDTO>>> listar() {
        return ResponseEntity.ok(ApiResponseDTO.<List<RelatorioResponseDTO>>builder()
                .status(200)
                .mensagem("Relatórios listados com sucesso")
                .dados(relatorioService.listarRelatorios())
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ApiResponseDTO<RelatorioResponseDTO>> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.<RelatorioResponseDTO>builder()
                .status(200)
                .mensagem("Relatório encontrado com sucesso")
                .dados(relatorioService.buscarPorId(id))
                .build());
    }
}
