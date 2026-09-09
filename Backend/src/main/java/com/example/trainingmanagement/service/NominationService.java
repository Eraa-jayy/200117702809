package com.example.trainingmanagement.service;

import com.example.trainingmanagement.dto.NominationRequest;
import com.example.trainingmanagement.entity.Department;
import com.example.trainingmanagement.entity.Nomination;
import com.example.trainingmanagement.entity.Officer;
import com.example.trainingmanagement.entity.Training;
import com.example.trainingmanagement.exception.DuplicateNominationException;
import com.example.trainingmanagement.exception.MaxParticipantsReachedException;
import com.example.trainingmanagement.exception.ResourceNotFoundException;
import com.example.trainingmanagement.repository.NominationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public Nomination createNomination(NominationRequest request) {
        Officer officer = officerService.getOfficerById(request.getOfficerId());
        Training training = trainingService.getTrainingById(request.getTrainingId());
        Department department = departmentService.getDepartmentById(request.getDepartmentId());

        if (nominationRepository.existsByOfficerIdAndTrainingId(officer.getId(), training.getId())) {
            throw new DuplicateNominationException("Officer is already nominated for this training programme.");
        }

        if (nominationRepository.countByTrainingId(training.getId()) >= training.getMaximumParticipants()) {
            throw new MaxParticipantsReachedException("Maximum participant limit has been reached.");
        }

        Nomination nomination = new Nomination();
        nomination.setOfficer(officer);
        nomination.setTraining(training);
        nomination.setDepartment(department);
        return nominationRepository.save(nomination);
    }

    public List<Nomination> getAllNominations() {
        return nominationRepository.findAll();
    }

    public Nomination getNominationById(Long id) {
        return nominationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nomination not found with id: " + id));
    }

    public List<Nomination> getNominationsByTrainingId(Long trainingId) {
        trainingService.getTrainingById(trainingId);
        return nominationRepository.findByTrainingId(trainingId);
    }

    public void deleteNomination(Long id) {
        Nomination nomination = getNominationById(id);
        nominationRepository.delete(nomination);
    }
}