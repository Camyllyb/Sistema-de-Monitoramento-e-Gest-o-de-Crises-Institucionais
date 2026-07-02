package br.edu.gestaocrises.auditoria;

import br.edu.gestaocrises.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaLogRepository auditoriaLogRepository;

    @Transactional
    public void registrarLog(Usuario usuario,
                             String acao,
                             String entidade,
                             Long entidadeId,
                             String detalhes) {
        AuditoriaLog log = AuditoriaLog.builder()
                .usuario(usuario)
                .acao(acao)
                .entidade(entidade)
                .entidadeId(entidadeId)
                .detalhes(detalhes)
                .dataRegistro(OffsetDateTime.now())
                .build();
        auditoriaLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditoriaLogResponseDTO> buscarLogs(Long usuarioId,
                                                    String acao,
                                                    String entidade,
                                                    OffsetDateTime dataInicio,
                                                    OffsetDateTime dataFim) {
        return auditoriaLogRepository
                .findAll(AuditoriaLogSpecification.comFiltros(
                        usuarioId, acao, entidade, dataInicio, dataFim))
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ─────────────────────────────────────────────
    // Privados
    // ─────────────────────────────────────────────

    private AuditoriaLogResponseDTO toResponseDTO(AuditoriaLog log) {
        return AuditoriaLogResponseDTO.builder()
                .id(log.getId())
                .usuarioId(log.getUsuario() != null ? log.getUsuario().getId() : null)
                .usuarioNome(log.getUsuario() != null ? log.getUsuario().getNome() : null)
                .acao(log.getAcao())
                .entidade(log.getEntidade())
                .entidadeId(log.getEntidadeId())
                .detalhes(log.getDetalhes())
                .dataRegistro(log.getDataRegistro())
                .build();
    }
}
