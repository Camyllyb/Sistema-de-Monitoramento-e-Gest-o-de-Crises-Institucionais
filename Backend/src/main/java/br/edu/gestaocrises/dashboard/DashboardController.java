package br.edu.gestaocrises.dashboard;

import br.edu.gestaocrises.common.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resumo")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ANALISTA')")
    public ResponseEntity<ApiResponseDTO<DashboardResumoDTO>> resumo() {
        return ResponseEntity.ok(ApiResponseDTO.<DashboardResumoDTO>builder()
                .status(200)
                .mensagem("Dashboard carregado com sucesso")
                .dados(dashboardService.gerarResumo())
                .build());
    }
}
