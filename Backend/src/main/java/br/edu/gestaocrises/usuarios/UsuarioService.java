package br.edu.gestaocrises.usuarios;

import br.edu.gestaocrises.auditoria.AuditoriaService;
import br.edu.gestaocrises.common.RecursoNaoEncontradoException;
import br.edu.gestaocrises.common.RegraNegocioException;
import br.edu.gestaocrises.perfis.Perfil;
import br.edu.gestaocrises.perfis.PerfilNome;
import br.edu.gestaocrises.perfis.PerfilRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarUsuario(id));
    }

    @Transactional
    public UsuarioResponseDTO criar(UsuarioCreateDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RegraNegocioException("E-mail já cadastrado: " + dto.getEmail());
        }
        Perfil perfil = resolverPerfil(dto.getPerfil());
        Usuario usuario = Usuario.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .senha(passwordEncoder.encode(dto.getSenha()))
                .perfil(perfil)
                .ativo(true)
                .build();
        UsuarioResponseDTO resultado = toResponseDTO(usuarioRepository.save(usuario));
        auditoriaService.registrarLog(obterUsuarioAutenticadoOuNulo(),
                "CRIACAO_USUARIO", "USUARIO", resultado.getId(),
                "Usuário criado: " + dto.getNome());
        return resultado;
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = buscarUsuario(id);
        if (usuarioRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new RegraNegocioException("E-mail já cadastrado: " + dto.getEmail());
        }
        Perfil perfil = resolverPerfil(dto.getPerfil());
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setPerfil(perfil);
        UsuarioResponseDTO resultado = toResponseDTO(usuarioRepository.save(usuario));
        auditoriaService.registrarLog(obterUsuarioAutenticadoOuNulo(),
                "EDICAO_USUARIO", "USUARIO", id,
                "Usuário editado: " + dto.getNome());
        return resultado;
    }

    @Transactional
    public void alterarSenha(Long id, UsuarioSenhaDTO dto) {
        Usuario usuario = buscarUsuario(id);
        usuario.setSenha(passwordEncoder.encode(dto.getNovaSenha()));
        usuarioRepository.save(usuario);
        auditoriaService.registrarLog(obterUsuarioAutenticadoOuNulo(),
                "ALTERACAO_SENHA", "USUARIO", id,
                "Senha alterada para o usuário: " + usuario.getEmail());
    }

    @Transactional
    public UsuarioResponseDTO alterarStatus(Long id, UsuarioStatusDTO dto) {
        Usuario usuario = buscarUsuario(id);
        if (Boolean.FALSE.equals(dto.getAtivo())) {
            verificarAutoDesativacao(usuario);
        }
        usuario.setAtivo(dto.getAtivo());
        UsuarioResponseDTO resultado = toResponseDTO(usuarioRepository.save(usuario));
        String acao = Boolean.FALSE.equals(dto.getAtivo()) ? "DESATIVACAO_USUARIO" : "ATIVACAO_USUARIO";
        auditoriaService.registrarLog(obterUsuarioAutenticadoOuNulo(),
                acao, "USUARIO", id,
                "Status do usuário alterado para: " + dto.getAtivo());
        return resultado;
    }

    @Transactional
    public void desativar(Long id) {
        Usuario usuario = buscarUsuario(id);
        verificarAutoDesativacao(usuario);
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
        auditoriaService.registrarLog(obterUsuarioAutenticadoOuNulo(),
                "DESATIVACAO_USUARIO", "USUARIO", id,
                "Usuário desativado: " + usuario.getNome());
    }

    // ─────────────────────────────────────────────
    // Privados
    // ─────────────────────────────────────────────

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com ID: " + id));
    }

    private Perfil resolverPerfil(String perfilNome) {
        PerfilNome nome;
        try {
            nome = PerfilNome.valueOf(perfilNome.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException(
                    "Perfil inválido: " + perfilNome + ". Perfis disponíveis: ADMIN, GERENTE, ANALISTA");
        }
        return perfilRepository.findByNome(nome)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Perfil não encontrado: " + perfilNome));
    }

    private void verificarAutoDesativacao(Usuario usuario) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            if (auth.getName().equals(usuario.getEmail())) {
                throw new RegraNegocioException("Não é permitido desativar o próprio usuário");
            }
        }
    }

    private Usuario obterUsuarioAutenticadoOuNulo() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()
                    || "anonymousUser".equals(auth.getPrincipal())) {
                return null;
            }
            return usuarioRepository.findByEmail(auth.getName()).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .perfil(usuario.getPerfil().getNome().name())
                .ativo(usuario.getAtivo())
                .build();
    }
}
