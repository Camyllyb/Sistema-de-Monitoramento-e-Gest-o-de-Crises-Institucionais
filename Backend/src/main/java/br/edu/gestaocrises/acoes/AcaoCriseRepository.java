package br.edu.gestaocrises.acoes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcaoCriseRepository extends JpaRepository<AcaoCrise, Long> {
    java.util.List<AcaoCrise> findByCriseIdOrderByDataAcaoDesc(Long criseId);
}
