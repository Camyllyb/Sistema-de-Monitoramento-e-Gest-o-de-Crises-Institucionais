package br.edu.gestaocrises.auditoria;

import br.edu.gestaocrises.common.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ApiResponseDTO<List<AuditoriaLogResponseDTO>>> consultar(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) String entidade,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dataInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dataFim) {

        return ResponseEntity.ok(ApiResponseDTO.<List<AuditoriaLogResponseDTO>>builder()
                .status(200)
                .mensagem("Logs de auditoria listados com sucesso")
                .dados(auditoriaService.buscarLogs(usuarioId, acao, entidade, dataInicio, dataFim))
                .build());
    }
}
