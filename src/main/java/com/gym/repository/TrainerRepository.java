package com.gym.repository;

import com.gym.model.Trainer;
import com.gym.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    Optional<Trainer> findByUserUsername(String username);

    @Query("""
            SELECT t FROM Training t
            WHERE t.trainer.user.username = :username
            AND (:fromDate IS NULL OR t.trainingDate >= :fromDate)
            AND (:toDate IS NULL OR t.trainingDate <= :toDate)
            AND (:traineeName IS NULL OR t.trainee.user.lastName = :traineeName)
            """)
    List<Training> findTrainings(
            @Param("username") String username,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("traineeName") String traineeName
    );
}
