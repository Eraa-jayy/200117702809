package com.example.trainingmanagement.service;

import com.example.trainingmanagement.dto.EligibilityRuleRequest;
import com.example.trainingmanagement.entity.EligibilityRule;
import com.example.trainingmanagement.entity.Training;
import com.example.trainingmanagement.exception.ResourceNotFoundException;
import com.example.trainingmanagement.repository.EligibilityRuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EligibilityRuleService {
    private final EligibilityRuleRepository ruleRepository;
    private final TrainingService trainingService;
    private final EligibilityService eligibilityService;

    public EligibilityRuleService(EligibilityRuleRepository ruleRepository,
                                  TrainingService trainingService,
                                  EligibilityService eligibilityService) {
        this.ruleRepository = ruleRepository;
        this.trainingService = trainingService;
        this.eligibilityService = eligibilityService;
    }

    public List<EligibilityRule> getByTraining(Long trainingId) {
        trainingService.getTrainingById(trainingId);
        return ruleRepository.findByTrainingIdOrderByIdAsc(trainingId);
    }

    public EligibilityRule add(Long trainingId, EligibilityRuleRequest request) {
        Training training = trainingService.getTrainingById(trainingId);
        String value = request.getRuleValue().trim();
        if (!eligibilityService.isValidRuleValue(request.getRuleType(), value)) {
            throw new IllegalArgumentException("Invalid eligibility rule value.");
        }
        if (ruleRepository.existsByTrainingIdAndRuleTypeAndRuleValue(trainingId, request.getRuleType(), value)) {
            throw new IllegalArgumentException("This eligibility rule already exists.");
        }
        return ruleRepository.save(new EligibilityRule(null, training, request.getRuleType(), value));
    }

    public EligibilityRule add(EligibilityRuleRequest request) {
        if (request.getTrainingId() == null) {
            throw new IllegalArgumentException("Training id is required.");
        }
        return add(request.getTrainingId(), request);
    }

    public void delete(Long id) {
        if (!ruleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Eligibility rule not found with id: " + id);
        }
        ruleRepository.deleteById(id);
    }

    public EligibilityRule update(Long id, EligibilityRuleRequest request) {
        EligibilityRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Eligibility rule not found with id: " + id));
        String value = request.getRuleValue().trim();
        if (!eligibilityService.isValidRuleValue(request.getRuleType(), value)) {
            throw new IllegalArgumentException("Invalid eligibility rule value.");
        }
        rule.setRuleType(request.getRuleType());
        rule.setRuleValue(value);
        return ruleRepository.save(rule);
    }
}
