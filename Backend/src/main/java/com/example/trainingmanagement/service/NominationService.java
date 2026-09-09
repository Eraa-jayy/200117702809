package com.example.trainingmanagement.service;

import com.example.trainingmanagement.dto.NominationRequest;
import com.example.trainingmanagement.entity.Department;
import com.example.trainingmanagement.entity.Nomination;
import com.example.trainingmanagement.entity.NominationStatus;
import com.example.trainingmanagement.entity.Officer;
import com.example.trainingmanagement.entity.Training;
import com.example.trainingmanagement.exception.DuplicateNominationException;
import com.example.trainingmanagement.exception.ResourceNotFoundException;
import com.example.trainingmanagement.repository.NominationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class NominationService {

    private final NominationRepository nominationRepository;
    private final OfficerService officerService;
    private final TrainingService trainingService;
    private final DepartmentService departmentService;

    public NominationService(NominationRepository nominationRepository,
                             OfficerService officerService,
                             TrainingService trainingService,
                             DepartmentService departmentService) {
        this.nominationRepository = nominationRepository;
        this.officerService = officerService;
        this.trainingService = trainingService;
        this.departmentService = departmentService;
    }

    /**
     * Core business rule: one officer can be nominated only once per training programme.
     */
    @Transactional
    public Nomination createNomination(NominationRequest request) {
        Officer officer = officerService.getOfficerById(request.getOfficerId());
        Training training = trainingService.getTrainingById(request.getTrainingId());
        Department department = departmentService.getDepartmentById(request.getDepartmentId());

        if (nominationRepository.existsByOfficerIdAndTrainingIdAndStatusIn(
                officer.getId(), training.getId(),
                Arrays.asList(NominationStatus.CONFIRMED, NominationStatus.WAITING))) {
            throw new DuplicateNominationException("Officer is already nominated for this training programme.");
        }

        // The existing unique constraint permits only one historical row per
        // officer/training pair, so replace an old cancelled record when the
        // officer is nominated again.
        List<Nomination> cancelledNominations = nominationRepository
                .findByOfficerIdAndTrainingIdAndStatus(
                        officer.getId(), training.getId(), NominationStatus.CANCELLED);
        if (!cancelledNominations.isEmpty()) {
            nominationRepository.deleteAll(cancelledNominations);
            nominationRepository.flush();
        }

        Nomination nomination = new Nomination();
        nomination.setOfficer(officer);
        nomination.setTraining(training);
        nomination.setDepartment(department);
        nomination.setStatus(nominationRepository.countByTrainingIdAndStatus(
                training.getId(), NominationStatus.CONFIRMED) < training.getMaximumParticipants()
                ? NominationStatus.CONFIRMED
                : NominationStatus.WAITING);
        return nominationRepository.save(nomination);
    }

    public List<Nomination> getAllNominations() {
        return nominationRepository.findAll();
    }

    public Nomination getNominationById(Long id) {
        return nominationRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nomination not found with id: " + id));
    }

    public List<Nomination> getNominationsByTrainingId(Long trainingId) {
        trainingService.getTrainingById(trainingId);
        return nominationRepository.findByTrainingIdOrderByNominationDateAsc(trainingId);
    }

    /**
     * Cancels without deleting the nomination so the waiting-list history remains visible.
     * Returns true when a waiting participant was promoted.
     */
    @Transactional
    public boolean cancelNomination(Long id) {
        Nomination nomination = getNominationById(id);
        if (nomination.getStatus() == NominationStatus.CANCELLED) {
            return false;
        }

        boolean promoteWaitingNomination = nomination.getStatus() == NominationStatus.CONFIRMED;
        nomination.setStatus(NominationStatus.CANCELLED);
        nominationRepository.save(nomination);

        if (!promoteWaitingNomination) {
            return false;
        }

        List<Nomination> waitingNominations = nominationRepository
                .findByTrainingIdAndStatusOrderByNominationDateAsc(
                        nomination.getTraining().getId(), NominationStatus.WAITING);
        if (waitingNominations.isEmpty()) {
            return false;
        }

        Nomination nextWaitingNomination = waitingNominations.get(0);
        nextWaitingNomination.setStatus(NominationStatus.CONFIRMED);
        nominationRepository.save(nextWaitingNomination);
        return true;
    }
}
