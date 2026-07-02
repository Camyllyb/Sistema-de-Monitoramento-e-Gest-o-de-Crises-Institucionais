package br.edu.gestaocrises.acoes;

import br.edu.gestaocrises.auditoria.AuditoriaService;
import br.edu.gestaocrises.common.RecursoNaoEncontradoException;
import br.edu.gestaocrises.common.RegraNegocioException;
import br.edu.gestaocrises.crises.Crise;
import br.edu.gestaocrises.crises.CriseRepository;
import br.edu.gestaocrises.crises.StatusCrise;
import br.edu.gestaocrises.usuarios.Usuario;
import br.edu.gestaocrises.usuarios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AcaoCriseService {

    private final AcaoCriseRepository acaoCriseRepository;
    private final CriseRepository criseRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<AcaoCriseResponseDTO> listarPorCrise(Long criseId) {
        buscarCrise(criseId);
        return acaoCriseRepository.findByCriseIdOrderByDataAcaoDesc(criseId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public AcaoCriseResponseDTO registrarAcao(Long criseId, AcaoCriseCreateDTO dto) {
        Crise crise = buscarCrise(criseId);
        validarStatusParaAcao(crise);
        Usuario executor = obterUsuarioAutenticado();

        OffsetDateTime dataAcao = dto.getDataAcao() != null ? dto.getDataAcao() : OffsetDateTime.now();

        AcaoCrise acao = AcaoCrise.builder()
                .crise(crise)
                .executor(executor)
                .tipo(dto.getTipo())
                .descricao(dto.getDescricao())
                .dataAcao(dataAcao)
                .build();

        AcaoCrise acaoSalva = acaoCriseRepository.save(acao);
        auditoriaService.registrarLog(executor, "REGISTRO_ACAO_CRISE", "ACAO_CRISE", acaoSalva.getId(),
                "Ação " + dto.getTipo().name() + " registrada na crise ID " + criseId);
        return toResponseDTO(acaoSalva);
    }

    // ─────────────────────────────────────────────
    // Privados
    // ─────────────────────────────────────────────

    private Crise buscarCrise(Long criseId) {
        return criseRepository.findById(criseId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Crise não encontrada com ID: " + criseId));
    }

    private void validarStatusParaAcao(Crise crise) {
        StatusCrise status = crise.getStatus();
        if (status != StatusCrise.ABERTA && status != StatusCrise.EM_ANDAMENTO) {
            throw new RegraNegocioException(
                    "Não é possível registrar ação em crise com status " + status.name()
                    + ". Apenas crises ABERTA ou EM_ANDAMENTO aceitam novas ações.");
        }
    }

    private Usuario obterUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário autenticado não encontrado: " + email));
    }

    private AcaoCriseResponseDTO toResponseDTO(AcaoCrise acao) {
        return AcaoCriseResponseDTO.builder()
                .id(acao.getId())
                .criseId(acao.getCrise().getId())
                .tipo(acao.getTipo().name())
                .descricao(acao.getDescricao())
                .dataAcao(acao.getDataAcao())
                .executorId(acao.getExecutor().getId())
                .executorNome(acao.getExecutor().getNome())
                .build();
    }
}
