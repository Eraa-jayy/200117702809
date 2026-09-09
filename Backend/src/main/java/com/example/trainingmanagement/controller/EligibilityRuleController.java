package com.example.trainingmanagement.controller;

import com.example.trainingmanagement.dto.EligibilityRuleRequest;
import com.example.trainingmanagement.entity.EligibilityRule;
import com.example.trainingmanagement.service.EligibilityRuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

@RestController
@RequestMapping("/api/eligibility-rules")
public class EligibilityRuleController {
    private final EligibilityRuleService ruleService;

    public EligibilityRuleController(EligibilityRuleService ruleService) {
        this.ruleService = ruleService;
    }

    @GetMapping("/training/{trainingId}")
    public List<EligibilityRule> getByTraining(@PathVariable Long trainingId) {
        return ruleService.getByTraining(trainingId);
    }

    @PostMapping("/training/{trainingId}")
    public org.springframework.http.ResponseEntity<EligibilityRule> add(
            @PathVariable Long trainingId, @Valid @RequestBody EligibilityRuleRequest request) {
        return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED)
                .body(ruleService.add(trainingId, request));
    }

    @PostMapping
    public org.springframework.http.ResponseEntity<EligibilityRule> add(@Valid @RequestBody EligibilityRuleRequest request) {
        return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED)
                .body(ruleService.add(request));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        ruleService.delete(id);
    }

    @PutMapping("/{id}")
    public EligibilityRule update(@PathVariable Long id, @Valid @RequestBody EligibilityRuleRequest request) {
        return ruleService.update(id, request);
    }
}
