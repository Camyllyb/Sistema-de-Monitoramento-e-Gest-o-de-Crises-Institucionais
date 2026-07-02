package br.edu.gestaocrises.perfis;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    java.util.Optional<Perfil> findByNome(PerfilNome nome);
}
