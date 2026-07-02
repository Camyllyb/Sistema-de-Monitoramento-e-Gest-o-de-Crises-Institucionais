package br.edu.gestaocrises.auth;

import br.edu.gestaocrises.auditoria.AuditoriaService;
import br.edu.gestaocrises.common.CredenciaisInvalidasException;
import br.edu.gestaocrises.common.RecursoNaoEncontradoException;
import br.edu.gestaocrises.usuarios.Usuario;
import br.edu.gestaocrises.usuarios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final CustomUserDetailsService userDetailsService;
    private final AuditoriaService auditoriaService;

    public LoginResponseDTO login(LoginRequestDTO request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
            );
        } catch (AuthenticationException ex) {
            throw new CredenciaisInvalidasException("Credenciais inválidas");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CredenciaisInvalidasException("Credenciais inválidas"));

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new CredenciaisInvalidasException("Usuário inativo");
        }

        var userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String token = jwtService.generateToken(userDetails);

        auditoriaService.registrarLog(usuario, "LOGIN", "USUARIO", usuario.getId(),
                "Login realizado com sucesso");

        return LoginResponseDTO.builder()
                .token(token)
                .tipo("Bearer")
                .expiraEm(jwtService.getExpirationTime())
                .usuario(UsuarioAutenticadoDTO.builder()
                        .id(usuario.getId())
                        .nome(usuario.getNome())
                        .email(usuario.getEmail())
                        .perfil(usuario.getPerfil().getNome().name())
                        .build())
                .build();
    }

    public UsuarioAutenticadoDTO me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new CredenciaisInvalidasException("Usuário não autenticado");
        }

        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        return UsuarioAutenticadoDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .perfil(usuario.getPerfil().getNome().name())
                .build();
    }
}
