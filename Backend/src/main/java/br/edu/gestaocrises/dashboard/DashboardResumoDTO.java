package br.edu.gestaocrises.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResumoDTO {

    private long totalCrises;

    private Map<String, Long> crisesPorStatus;

    private Map<String, Long> crisesPorNivel;

    private long totalCrisesCriticas;

    private List<UltimaAcaoDTO> ultimasAcoes;

    private Double tempoMedioResolucaoHoras;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UltimaAcaoDTO {
        private Long id;
        private String tipo;
        private String descricao;
        private String criseTitulo;
        private Long criseId;
        private String executorNome;
        private String dataAcao;
    }
}
