package br.edu.gestaocrises.perfis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PerfilService {

    private final PerfilRepository perfilRepository;

    @Transactional(readOnly = true)
    public List<PerfilResponseDTO> listar() {
        return perfilRepository.findAll().stream()
                .map(p -> PerfilResponseDTO.builder()
                        .id(p.getId())
                        .nome(p.getNome().name())
                        .build())
                .toList();
    }
}
