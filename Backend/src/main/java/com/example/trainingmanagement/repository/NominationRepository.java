package com.example.trainingmanagement.repository;

import com.example.trainingmanagement.entity.Nomination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NominationRepository extends JpaRepository<Nomination, Long> {

    boolean existsByOfficerIdAndTrainingId(Long officerId, Long trainingId);

    long countByTrainingId(Long trainingId);

    List<Nomination> findByTrainingId(Long trainingId);
}
