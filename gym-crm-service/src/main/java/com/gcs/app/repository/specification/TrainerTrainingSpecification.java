package com.gcs.app.repository.specification;

import com.gcs.app.facade.dto.TrainerTrainingSearchCriteriaDto;
import com.gcs.app.model.Training;
import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TrainerTrainingSpecification extends TrainingSpecification {

    public static Specification<Training> findByCriteria(TrainerTrainingSearchCriteriaDto searchFilter) {
        return trainerUsernamePredicate(searchFilter.getUsername())
                .and(dateRangePredicate(searchFilter.getFromDate(), searchFilter.getToDate()))
                .and(traineeNamePredicate(searchFilter.getTraineeName()));
    }

    private static Specification<Training> trainerUsernamePredicate(@Nullable String username) {
        return (root, query, cb) -> {
            if (isNotBlank(username)) {
                return cb.equal(root.get("trainer").get("user").get("username"), username);
            }
            return cb.conjunction();
        };
    }

    private static Specification<Training> traineeNamePredicate(@Nullable String traineeName) {
        return (root, query, cb) -> {
            if (isNotBlank(traineeName)) {
                Expression<String> fullName = cb.concat(
                        cb.concat(root.get("trainee").get("user").get("firstName"), " "),
                        root.get("trainee").get("user").get("lastName")
                );
                return cb.like(cb.lower(fullName), "%" + traineeName.toLowerCase() + "%");
            }
            return cb.conjunction();
        };
    }
}