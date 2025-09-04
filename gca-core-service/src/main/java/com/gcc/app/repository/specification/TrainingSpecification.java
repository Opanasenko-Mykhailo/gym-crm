package com.gcc.app.repository.specification;

import com.gcc.app.model.Training;
import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public abstract class TrainingSpecification {

    protected TrainingSpecification() {
    }

    protected static Specification<Training> dateRangePredicate(@Nullable LocalDate from, @Nullable LocalDate to) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (from != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("date"), from));
            }
            if (to != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("date"), to));
            }

            return predicate;
        };
    }
}
