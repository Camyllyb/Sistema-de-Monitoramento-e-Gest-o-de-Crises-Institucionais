package br.edu.gestaocrises.relatorios;

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
public class RelatorioService {

    private final RelatorioCriseRepository relatorioRepository;
    private final CriseRepository criseRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    @Transactional
    public RelatorioResponseDTO gerarRelatorio(RelatorioCreateDTO dto) {
        Crise crise = buscarCrise(dto.getCriseId());
        validarStatusParaRelatorio(crise);
        validarRelatorioUnico(crise.getId());
        Usuario gerador = obterUsuarioAutenticado();

        RelatorioCrise relatorio = RelatorioCrise.builder()
                .titulo(dto.getTitulo())
                .conteudo(dto.getConteudo())
                .dataGeracao(OffsetDateTime.now())
                .crise(crise)
                .gerador(gerador)
                .build();

        RelatorioCrise relatorioSalvo = relatorioRepository.save(relatorio);
        auditoriaService.registrarLog(gerador, "GERACAO_RELATORIO", "RELATORIO_CRISE",
                relatorioSalvo.getId(), "Relatório gerado para crise ID " + crise.getId());
        return toResponseDTO(relatorioSalvo);
    }

    @Transactional(readOnly = true)
    public List<RelatorioResponseDTO> listarRelatorios() {
        return relatorioRepository.findAllByOrderByDataGeracaoDesc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public RelatorioResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarRelatorio(id));
    }

    // ─────────────────────────────────────────────
    // Privados
    // ─────────────────────────────────────────────

    private Crise buscarCrise(Long criseId) {
        return criseRepository.findById(criseId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Crise não encontrada com ID: " + criseId));
    }

    private RelatorioCrise buscarRelatorio(Long id) {
        return relatorioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Relatório não encontrado com ID: " + id));
    }

    private void validarStatusParaRelatorio(Crise crise) {
        StatusCrise status = crise.getStatus();
        if (status != StatusCrise.RESOLVIDA && status != StatusCrise.ENCERRADA) {
            throw new RegraNegocioException(
                    "Só é possível gerar relatório para crises RESOLVIDA ou ENCERRADA. "
                    + "Status atual: " + status.name());
        }
    }

    private void validarRelatorioUnico(Long criseId) {
        if (relatorioRepository.existsByCriseId(criseId)) {
            throw new RegraNegocioException(
                    "Já existe um relatório gerado para a crise com ID: " + criseId);
        }
    }

    private Usuario obterUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário autenticado não encontrado: " + email));
    }

    private RelatorioResponseDTO toResponseDTO(RelatorioCrise relatorio) {
        return RelatorioResponseDTO.builder()
                .id(relatorio.getId())
                .criseId(relatorio.getCrise().getId())
                .titulo(relatorio.getTitulo())
                .conteudo(relatorio.getConteudo())
                .dataGeracao(relatorio.getDataGeracao())
                .geradorId(relatorio.getGerador().getId())
                .geradorNome(relatorio.getGerador().getNome())
                .build();
    }
}
