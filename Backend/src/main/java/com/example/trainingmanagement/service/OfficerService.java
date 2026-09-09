package com.example.trainingmanagement.service;

import com.example.trainingmanagement.dto.OfficerRequest;
import com.example.trainingmanagement.entity.Department;
import com.example.trainingmanagement.entity.Officer;
import com.example.trainingmanagement.exception.ResourceNotFoundException;
import com.example.trainingmanagement.repository.OfficerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OfficerService {

    private final OfficerRepository officerRepository;
    private final DepartmentService departmentService;

    public OfficerService(OfficerRepository officerRepository, DepartmentService departmentService) {
        this.officerRepository = officerRepository;
        this.departmentService = departmentService;
    }

    public Officer createOfficer(OfficerRequest request) {
        Department department = departmentService.getDepartmentById(request.getDepartmentId());
        Officer officer = new Officer();
        officer.setEmployeeId(request.getEmployeeId());
        officer.setName(request.getName());
        officer.setEmail(request.getEmail());
        officer.setDepartment(department);
        return officerRepository.save(officer);
    }

    public List<Officer> getAllOfficers() {
        return officerRepository.findAll();
    }

    public Officer getOfficerById(Long id) {
        return officerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found with id: " + id));
    }

    public Officer updateOfficer(Long id, OfficerRequest request) {
        Officer officer = getOfficerById(id);
        Department department = departmentService.getDepartmentById(request.getDepartmentId());
        officer.setEmployeeId(request.getEmployeeId());
        officer.setName(request.getName());
        officer.setEmail(request.getEmail());
        officer.setDepartment(department);
        return officerRepository.save(officer);
    }

    public void deleteOfficer(Long id) {
        Officer officer = getOfficerById(id);
        officerRepository.delete(officer);
    }
}