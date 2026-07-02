package br.edu.gestaocrises.dashboard;

import br.edu.gestaocrises.acoes.AcaoCrise;
import br.edu.gestaocrises.acoes.AcaoCriseRepository;
import br.edu.gestaocrises.crises.Crise;
import br.edu.gestaocrises.crises.CriseRepository;
import br.edu.gestaocrises.crises.NivelCrise;
import br.edu.gestaocrises.crises.StatusCrise;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CriseRepository criseRepository;
    private final AcaoCriseRepository acaoCriseRepository;

    @Transactional(readOnly = true)
    public DashboardResumoDTO gerarResumo() {
        List<Crise> crises = criseRepository.findAll();
        List<AcaoCrise> acoes = acaoCriseRepository.findAll();

        long totalCrises = crises.size();

        Map<String, Long> crisesPorStatus = contarPorStatus(crises);
        Map<String, Long> crisesPorNivel = contarPorNivel(crises);

        long totalCrisesCriticas = crises.stream()
                .filter(c -> c.getNivel() == NivelCrise.CRITICO)
                .count();

        List<DashboardResumoDTO.UltimaAcaoDTO> ultimasAcoes = acoes.stream()
                .sorted(Comparator.comparing(AcaoCrise::getDataAcao).reversed())
                .limit(10)
                .map(this::toUltimaAcaoDTO)
                .collect(Collectors.toList());

        Double tempoMedioResolucaoHoras = calcularTempoMedioResolucao(crises);

        return DashboardResumoDTO.builder()
                .totalCrises(totalCrises)
                .crisesPorStatus(crisesPorStatus)
                .crisesPorNivel(crisesPorNivel)
                .totalCrisesCriticas(totalCrisesCriticas)
                .ultimasAcoes(ultimasAcoes)
                .tempoMedioResolucaoHoras(tempoMedioResolucaoHoras)
                .build();
    }

    // ─────────────────────────────────────────────
    // Privados
    // ─────────────────────────────────────────────

    private Map<String, Long> contarPorStatus(List<Crise> crises) {
        Map<String, Long> mapa = new LinkedHashMap<>();
        for (StatusCrise status : StatusCrise.values()) {
            mapa.put(status.name(), 0L);
        }
        crises.stream()
                .collect(Collectors.groupingBy(Crise::getStatus, Collectors.counting()))
                .forEach((status, count) -> mapa.put(status.name(), count));
        return mapa;
    }

    private Map<String, Long> contarPorNivel(List<Crise> crises) {
        Map<String, Long> mapa = new LinkedHashMap<>();
        for (NivelCrise nivel : NivelCrise.values()) {
            mapa.put(nivel.name(), 0L);
        }
        crises.stream()
                .collect(Collectors.groupingBy(Crise::getNivel, Collectors.counting()))
                .forEach((nivel, count) -> mapa.put(nivel.name(), count));
        return mapa;
    }

    private Double calcularTempoMedioResolucao(List<Crise> crises) {
        List<Crise> resolvidas = crises.stream()
                .filter(c -> c.getStatus() == StatusCrise.RESOLVIDA || c.getStatus() == StatusCrise.ENCERRADA)
                .filter(c -> c.getDataAtualizacao() != null)
                .collect(Collectors.toList());

        if (resolvidas.isEmpty()) {
            return null;
        }

        double media = resolvidas.stream()
                .mapToDouble(c -> {
                    OffsetDateTime inicio = c.getDataAbertura();
                    OffsetDateTime fim = c.getDataAtualizacao();
                    if (inicio != null && fim != null) {
                        return Duration.between(inicio, fim).toHours();
                    }
                    return 0;
                })
                .average()
                .orElse(0.0);

        return Math.round(media * 100.0) / 100.0;
    }

    private DashboardResumoDTO.UltimaAcaoDTO toUltimaAcaoDTO(AcaoCrise acao) {
        return DashboardResumoDTO.UltimaAcaoDTO.builder()
                .id(acao.getId())
                .tipo(acao.getTipo() != null ? acao.getTipo().name() : null)
                .descricao(acao.getDescricao())
                .criseId(acao.getCrise() != null ? acao.getCrise().getId() : null)
                .criseTitulo(acao.getCrise() != null ? acao.getCrise().getTitulo() : null)
                .executorNome(acao.getExecutor() != null ? acao.getExecutor().getNome() : null)
                .dataAcao(acao.getDataAcao() != null ? acao.getDataAcao().toString() : null)
                .build();
    }
}
