package br.edu.gestaocrises.perfis;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "perfil")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private PerfilNome nome;

    @NotBlank(message = "A descrição do perfil é obrigatória")
    @Size(max = 255, message = "A descrição do perfil deve ter no máximo 255 caracteres")
    @Column(nullable = false, length = 255)
    private String descricao;
}
