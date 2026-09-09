package com.example.trainingmanagement.repository;

import com.example.trainingmanagement.entity.Nomination;
import com.example.trainingmanagement.entity.NominationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NominationRepository extends JpaRepository<Nomination, Long> {

    boolean existsByOfficerIdAndTrainingId(Long officerId, Long trainingId);

    boolean existsByOfficerIdAndTrainingIdAndStatusIn(Long officerId, Long trainingId,
                                                      List<NominationStatus> statuses);

    List<Nomination> findByOfficerIdAndTrainingIdAndStatus(
            Long officerId, Long trainingId, NominationStatus status);

    long countByTrainingIdAndStatus(Long trainingId, NominationStatus status);

    List<Nomination> findByTrainingIdOrderByNominationDateAsc(Long trainingId);

    List<Nomination> findByTrainingIdAndStatusOrderByNominationDateAsc(Long trainingId,
                                                                         NominationStatus status);

    @Modifying
    @Transactional
    @Query("update Nomination n set n.status = :status where n.status is null")
    int updateMissingStatus(NominationStatus status);
}
