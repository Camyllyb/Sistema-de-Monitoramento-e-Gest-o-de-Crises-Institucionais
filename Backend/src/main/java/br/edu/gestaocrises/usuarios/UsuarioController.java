package br.edu.gestaocrises.usuarios;

import br.edu.gestaocrises.common.ApiResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<UsuarioResponseDTO>>> listar() {
        return ResponseEntity.ok(ApiResponseDTO.<List<UsuarioResponseDTO>>builder()
                .status(200)
                .mensagem("Usuários listados com sucesso")
                .dados(usuarioService.listar())
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<UsuarioResponseDTO>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.<UsuarioResponseDTO>builder()
                .status(200)
                .mensagem("Usuário encontrado com sucesso")
                .dados(usuarioService.buscarPorId(id))
                .build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<UsuarioResponseDTO>> criar(
            @Valid @RequestBody UsuarioCreateDTO dto) {
        UsuarioResponseDTO criado = usuarioService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.<UsuarioResponseDTO>builder()
                        .status(201)
                        .mensagem("Usuário criado com sucesso")
                        .dados(criado)
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<UsuarioResponseDTO>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.<UsuarioResponseDTO>builder()
                .status(200)
                .mensagem("Usuário atualizado com sucesso")
                .dados(usuarioService.atualizar(id, dto))
                .build());
    }

    @PatchMapping("/{id}/senha")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> alterarSenha(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioSenhaDTO dto) {
        usuarioService.alterarSenha(id, dto);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .status(200)
                .mensagem("Senha atualizada com sucesso")
                .build());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<UsuarioResponseDTO>> alterarStatus(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioStatusDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.<UsuarioResponseDTO>builder()
                .status(200)
                .mensagem("Status do usuário atualizado com sucesso")
                .dados(usuarioService.alterarStatus(id, dto))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> desativar(@PathVariable Long id) {
        usuarioService.desativar(id);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .status(200)
                .mensagem("Usuário desativado com sucesso")
                .build());
    }
}
