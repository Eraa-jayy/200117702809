package com.example.trainingmanagement.service;

import com.example.trainingmanagement.entity.Training;
import com.example.trainingmanagement.exception.ResourceNotFoundException;
import com.example.trainingmanagement.repository.TrainingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;

    public TrainingService(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    public Training createTraining(Training training) {
        return trainingRepository.save(training);
    }

    public List<Training> getAllTrainings() {
        return trainingRepository.findAll();
    }

    public Training getTrainingById(Long id) {
        return trainingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training not found with id: " + id));
    }

    public Training updateTraining(Long id, Training trainingDetails) {
        Training training = getTrainingById(id);
        training.setTitle(trainingDetails.getTitle());
        training.setDate(trainingDetails.getDate());
        training.setVenue(trainingDetails.getVenue());
        training.setTrainer(trainingDetails.getTrainer());
        training.setMaximumParticipants(trainingDetails.getMaximumParticipants());
        return trainingRepository.save(training);
    }

    public void deleteTraining(Long id) {
        Training training = getTrainingById(id);
        trainingRepository.delete(training);
    }
}