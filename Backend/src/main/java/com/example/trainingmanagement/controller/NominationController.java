package com.example.trainingmanagement.controller;

import com.example.trainingmanagement.dto.NominationRequest;
import com.example.trainingmanagement.entity.Nomination;
import com.example.trainingmanagement.dto.EligibilityResult;
import com.example.trainingmanagement.service.EligibilityService;
import com.example.trainingmanagement.service.OfficerService;
import com.example.trainingmanagement.service.TrainingService;
import com.example.trainingmanagement.service.NominationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nominations")
public class NominationController {

    private final NominationService nominationService;
    private final OfficerService officerService;
    private final TrainingService trainingService;
    private final EligibilityService eligibilityService;

    public NominationController(NominationService nominationService,
                                OfficerService officerService,
                                TrainingService trainingService,
                                EligibilityService eligibilityService) {
        this.nominationService = nominationService;
        this.officerService = officerService;
        this.trainingService = trainingService;
        this.eligibilityService = eligibilityService;
    }

    @PostMapping
    public ResponseEntity<Nomination> createNomination(@Valid @RequestBody NominationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(nominationService.createNomination(request));
    }

    @GetMapping
    public List<Nomination> getAllNominations() {
        return nominationService.getAllNominations();
    }

    @GetMapping("/eligibility")
    public EligibilityResult checkEligibility(@RequestParam Long officerId, @RequestParam Long trainingId) {
        return eligibilityService.checkEligibility(
                officerService.getOfficerById(officerId),
                trainingService.getTrainingById(trainingId));
    }

    @GetMapping("/{id}")
    public Nomination getNominationById(@PathVariable Long id) {
        return nominationService.getNominationById(id);
    }

    @GetMapping("/training/{trainingId}")
    public List<Nomination> getNominationsByTrainingId(@PathVariable Long trainingId) {
        return nominationService.getNominationsByTrainingId(trainingId);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Boolean>> cancelNomination(@PathVariable Long id) {
        boolean promoted = nominationService.cancelNomination(id);
        return ResponseEntity.ok(Map.of("promoted", promoted));
    }
}
