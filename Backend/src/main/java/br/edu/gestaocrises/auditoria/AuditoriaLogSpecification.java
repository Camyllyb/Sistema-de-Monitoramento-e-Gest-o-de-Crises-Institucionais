package br.edu.gestaocrises.auditoria;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;

public class AuditoriaLogSpecification {

    private AuditoriaLogSpecification() {}

    public static Specification<AuditoriaLog> comFiltros(
            Long usuarioId,
            String acao,
            String entidade,
            OffsetDateTime dataInicio,
            OffsetDateTime dataFim) {

        return (root, query, cb) -> {
            Predicate predicado = cb.conjunction();

            if (usuarioId != null) {
                predicado = cb.and(predicado,
                        cb.equal(root.get("usuario").get("id"), usuarioId));
            }
            if (acao != null && !acao.isBlank()) {
                predicado = cb.and(predicado,
                        cb.like(cb.lower(root.get("acao")), "%" + acao.toLowerCase() + "%"));
            }
            if (entidade != null && !entidade.isBlank()) {
                predicado = cb.and(predicado,
                        cb.equal(cb.lower(root.get("entidade")), entidade.toLowerCase()));
            }
            if (dataInicio != null) {
                predicado = cb.and(predicado,
                        cb.greaterThanOrEqualTo(root.get("dataRegistro"), dataInicio));
            }
            if (dataFim != null) {
                predicado = cb.and(predicado,
                        cb.lessThanOrEqualTo(root.get("dataRegistro"), dataFim));
            }

            query.orderBy(cb.desc(root.get("dataRegistro")));
            return predicado;
        };
    }
}
