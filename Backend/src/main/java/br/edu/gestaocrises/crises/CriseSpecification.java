package br.edu.gestaocrises.crises;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CriseSpecification {

    private CriseSpecification() {}

    public static Specification<Crise> comFiltros(
            StatusCrise status,
            NivelCrise nivel,
            Long responsavelId,
            Long criadoPorId) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (nivel != null) {
                predicates.add(cb.equal(root.get("nivel"), nivel));
            }
            if (responsavelId != null) {
                predicates.add(cb.equal(root.get("responsavel").get("id"), responsavelId));
            }
            if (criadoPorId != null) {
                predicates.add(cb.equal(root.get("criador").get("id"), criadoPorId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
