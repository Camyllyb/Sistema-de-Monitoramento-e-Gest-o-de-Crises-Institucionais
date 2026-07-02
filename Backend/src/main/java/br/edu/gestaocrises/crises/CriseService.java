package br.edu.gestaocrises.crises;

import br.edu.gestaocrises.auditoria.AuditoriaService;
import br.edu.gestaocrises.common.RecursoNaoEncontradoException;
import br.edu.gestaocrises.common.RegraNegocioException;
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
public class CriseService {

    private final CriseRepository criseRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<CriseResponseDTO> listar(
            StatusCrise status, NivelCrise nivel, Long responsavelId, Long criadoPorId) {
        return criseRepository.findAll(
                CriseSpecification.comFiltros(status, nivel, responsavelId, criadoPorId))
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public CriseResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarCrise(id));
    }

    @Transactional
    public CriseResponseDTO criar(CriseCreateDTO dto) {
        Usuario responsavel = buscarResponsavelAtivo(dto.getResponsavelId());
        Usuario criador = obterUsuarioAutenticado();

        Crise crise = Crise.builder()
                .titulo(dto.getTitulo())
                .descricao(dto.getDescricao())
                .tipo(dto.getTipo())
                .nivel(dto.getNivel())
                .status(StatusCrise.ABERTA)
                .dataAbertura(OffsetDateTime.now())
                .criador(criador)
                .responsavel(responsavel)
                .build();

        CriseResponseDTO resultado = toResponseDTO(criseRepository.save(crise));
        auditoriaService.registrarLog(criador, "CRIACAO_CRISE", "CRISE", resultado.getId(),
                "Crise criada: " + dto.getTitulo());
        return resultado;
    }

    @Transactional
    public CriseResponseDTO atualizar(Long id, CriseUpdateDTO dto) {
        Crise crise = buscarCrise(id);
        if (crise.getStatus() == StatusCrise.ENCERRADA) {
            throw new RegraNegocioException("Não é possível editar uma crise encerrada");
        }
        Usuario responsavel = buscarResponsavelAtivo(dto.getResponsavelId());

        crise.setTitulo(dto.getTitulo());
        crise.setDescricao(dto.getDescricao());
        crise.setTipo(dto.getTipo());
        crise.setNivel(dto.getNivel());
        crise.setResponsavel(responsavel);
        crise.setDataAtualizacao(OffsetDateTime.now());

        CriseResponseDTO resultado = toResponseDTO(criseRepository.save(crise));
        auditoriaService.registrarLog(obterUsuarioAutenticado(), "EDICAO_CRISE", "CRISE", id,
                "Crise editada: " + dto.getTitulo());
        return resultado;
    }

    @Transactional
    public CriseResponseDTO alterarStatus(Long id, CriseStatusDTO dto) {
        Crise crise = buscarCrise(id);
        StatusCrise statusAnterior = crise.getStatus();
        validarTransicao(statusAnterior, dto.getStatus());
        crise.setStatus(dto.getStatus());
        crise.setDataAtualizacao(OffsetDateTime.now());
        CriseResponseDTO resultado = toResponseDTO(criseRepository.save(crise));
        auditoriaService.registrarLog(obterUsuarioAutenticado(), "ALTERACAO_STATUS_CRISE", "CRISE", id,
                "Status: " + statusAnterior.name() + " → " + dto.getStatus().name());
        return resultado;
    }

    @Transactional
    public void excluir(Long id) {
        Crise crise = buscarCrise(id);
        if (crise.getStatus() != StatusCrise.ABERTA) {
            throw new RegraNegocioException(
                    "Apenas crises no status ABERTA podem ser excluídas. "
                    + "Para desativar sem remover registros históricos, "
                    + "adicione o campo 'ativo' à entidade Crise em uma migration futura.");
        }
        criseRepository.delete(crise);
    }

    // ─────────────────────────────────────────────
    // Privados
    // ─────────────────────────────────────────────

    private void validarTransicao(StatusCrise atual, StatusCrise novo) {
        if (atual == StatusCrise.ENCERRADA) {
            throw new RegraNegocioException("Crise encerrada não pode mudar de status");
        }
        boolean valida = switch (atual) {
            case ABERTA -> novo == StatusCrise.EM_ANDAMENTO
                    || novo == StatusCrise.RESOLVIDA
                    || novo == StatusCrise.ENCERRADA;
            case EM_ANDAMENTO -> novo == StatusCrise.RESOLVIDA
                    || novo == StatusCrise.ENCERRADA;
            case RESOLVIDA -> novo == StatusCrise.ENCERRADA;
            default -> false;
        };
        if (!valida) {
            throw new RegraNegocioException(
                    "Transição de status inválida: " + atual.name() + " → " + novo.name());
        }
    }

    private Crise buscarCrise(Long id) {
        return criseRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Crise não encontrada com ID: " + id));
    }

    private Usuario buscarResponsavelAtivo(Long id) {
        Usuario responsavel = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Responsável não encontrado com ID: " + id));
        if (!Boolean.TRUE.equals(responsavel.getAtivo())) {
            throw new RegraNegocioException("O responsável informado está inativo");
        }
        return responsavel;
    }

    private Usuario obterUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário autenticado não encontrado: " + email));
    }

    private CriseResponseDTO toResponseDTO(Crise crise) {
        return CriseResponseDTO.builder()
                .id(crise.getId())
                .titulo(crise.getTitulo())
                .descricao(crise.getDescricao())
                .tipo(crise.getTipo() != null ? crise.getTipo().name() : null)
                .nivel(crise.getNivel().name())
                .status(crise.getStatus().name())
                .responsavelId(crise.getResponsavel() != null ? crise.getResponsavel().getId() : null)
                .responsavelNome(crise.getResponsavel() != null ? crise.getResponsavel().getNome() : null)
                .criadoPorId(crise.getCriador().getId())
                .criadoPorNome(crise.getCriador().getNome())
                .dataCriacao(crise.getDataAbertura())
                .dataAtualizacao(crise.getDataAtualizacao())
                .build();
    }
}
