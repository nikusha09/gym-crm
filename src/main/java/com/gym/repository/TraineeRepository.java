package com.gym.repository;

import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    Optional<Trainee> findByUserUsername(String username);

    @Query("""
            SELECT t FROM Training t
            WHERE t.trainer.user.username = :username
            AND (:fromDate IS NULL OR t.trainingDate >= :fromDate)
            AND (:toDate IS NULL OR t.trainingDate <= :toDate)
            AND (:traineeName IS NULL OR t.trainee.user.lastName = :traineeName)
            AND (:trainingType IS NULL OR t.trainingType.trainingTypeName = :trainingType)
            """)
    List<Training> findTrainings(
            @Param("username") String username,
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate,
            @Param("traineeName") String traineeName,
            @Param("trainingType") String trainingType
    );

    @Query("""
            SELECT tr FROM Trainer tr
            WHERE tr.id NOT IN (
                SELECT t.id FROM Trainee tn
                JOIN tn.trainers t
                WHERE tn.user.username = :username
            )
            """)
    List<Trainer> getUnassignedTrainers(@Param("username") String username);
}
