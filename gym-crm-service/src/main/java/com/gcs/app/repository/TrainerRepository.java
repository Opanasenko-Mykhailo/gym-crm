package com.gcs.app.repository;

import com.gcs.app.model.Trainee;
import com.gcs.app.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    @Query("SELECT t FROM Trainer t JOIN FETCH t.user u LEFT JOIN FETCH t.trainees WHERE u.username = :username")
    Optional<Trainer> findByUsername(@Param("username") String username);

    @Query("SELECT t FROM Trainer t WHERE t NOT IN" +
            "(SELECT tr FROM Trainee trn JOIN trn.trainers tr WHERE trn = :trainee)")
    List<Trainer> findAllNotAssignedToTrainee(@Param("trainee") Trainee trainee);
}