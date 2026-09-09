package com.example.trainingmanagement.controller;

import com.example.trainingmanagement.dto.OfficerRequest;
import com.example.trainingmanagement.entity.Officer;
import com.example.trainingmanagement.service.OfficerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/officers")
public class OfficerController {

    private final OfficerService officerService;

    public OfficerController(OfficerService officerService) {
        this.officerService = officerService;
    }

    @PostMapping
    public ResponseEntity<Officer> createOfficer(@Valid @RequestBody OfficerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(officerService.createOfficer(request));
    }

    @GetMapping
    public List<Officer> getAllOfficers() {
        return officerService.getAllOfficers();
    }

    @GetMapping("/{id}")
    public Officer getOfficerById(@PathVariable Long id) {
        return officerService.getOfficerById(id);
    }

    @PutMapping("/{id}")
    public Officer updateOfficer(@PathVariable Long id, @Valid @RequestBody OfficerRequest request) {
        return officerService.updateOfficer(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOfficer(@PathVariable Long id) {
        officerService.deleteOfficer(id);
        return ResponseEntity.noContent().build();
    }
}