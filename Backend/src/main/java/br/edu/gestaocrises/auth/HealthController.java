package br.edu.gestaocrises.auth;

import br.edu.gestaocrises.common.ApiResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Endpoint de verificação da API")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Retorna o estado da API")
    public ApiResponseDTO<Map<String, String>> health() {
        return ApiResponseDTO.<Map<String, String>>builder()
                .status(HttpStatus.OK.value())
                .mensagem("API de Gestão de Crises Institucionais ativa")
                .dados(Map.of("servico", "gestaocrises-api"))
                .build();
    }
}
