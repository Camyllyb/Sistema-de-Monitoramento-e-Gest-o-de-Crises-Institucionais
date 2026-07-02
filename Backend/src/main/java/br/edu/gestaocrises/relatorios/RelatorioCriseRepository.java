package br.edu.gestaocrises.relatorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelatorioCriseRepository extends JpaRepository<RelatorioCrise, Long> {
    boolean existsByCriseId(Long criseId);
    java.util.List<RelatorioCrise> findByCriseId(Long criseId);
    java.util.List<RelatorioCrise> findAllByOrderByDataGeracaoDesc();
}
