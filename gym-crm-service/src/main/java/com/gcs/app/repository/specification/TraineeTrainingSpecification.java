package com.gcs.app.repository.specification;

import com.gcs.app.facade.dto.TraineeTrainingSearchCriteriaDto;
import com.gcs.app.model.Training;
import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TraineeTrainingSpecification extends TrainingSpecification {

    public static Specification<Training> findByCriteria(TraineeTrainingSearchCriteriaDto searchFilter) {
        return traineeUsernamePredicate(searchFilter.getUsername())
                .and(dateRangePredicate(searchFilter.getFromDate(), searchFilter.getToDate()))
                .and(trainerNamePredicate(searchFilter.getTrainerName()))
                .and(trainingTypePredicate(searchFilter.getTrainingTypeName()));
    }

    private static Specification<Training> traineeUsernamePredicate(@Nullable String username) {
        return (root, query, cb) -> {
            if (isNotBlank(username)) {
                return cb.equal(root.get("trainee").get("user").get("username"), username);
            }
            return cb.conjunction();
        };
    }

    private static Specification<Training> trainerNamePredicate(@Nullable String trainerName) {
        return (root, query, cb) -> {
            if (isNotBlank(trainerName)) {
                Expression<String> fullName = cb.concat(
                        cb.concat(root.get("trainer").get("user").get("firstName"), " "),
                        root.get("trainer").get("user").get("lastName")
                );
                return cb.like(cb.lower(fullName), "%" + trainerName.toLowerCase() + "%");
            }
            return cb.conjunction();
        };
    }

    private static Specification<Training> trainingTypePredicate(@Nullable String trainingType) {
        return (root, query, cb) -> {
            if (isNotBlank(trainingType)) {
                return cb.equal(cb.lower(root.get("type").get("name")), trainingType.toLowerCase());
            }
            return cb.conjunction();
        };
    }
}