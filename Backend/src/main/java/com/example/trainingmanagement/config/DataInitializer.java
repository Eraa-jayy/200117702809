package com.example.trainingmanagement.config;

import com.example.trainingmanagement.entity.Department;
import com.example.trainingmanagement.entity.Officer;
import com.example.trainingmanagement.entity.Training;
import com.example.trainingmanagement.repository.DepartmentRepository;
import com.example.trainingmanagement.repository.OfficerRepository;
import com.example.trainingmanagement.repository.TrainingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final OfficerRepository officerRepository;
    private final TrainingRepository trainingRepository;

    public DataInitializer(DepartmentRepository departmentRepository,
                           OfficerRepository officerRepository,
                           TrainingRepository trainingRepository) {
        this.departmentRepository = departmentRepository;
        this.officerRepository = officerRepository;
        this.trainingRepository = trainingRepository;
    }

    @Override
    public void run(String... args) {

        if (departmentRepository.count() == 0) {
            Department finance = new Department(null, "Finance Division");
            Department admin = new Department(null, "Administration Division");
            Department hr = new Department(null, "Human Resources Division");
            Department it = new Department(null, "IT Division");
            departmentRepository.save(finance);
            departmentRepository.save(admin);
            departmentRepository.save(hr);
            departmentRepository.save(it);
        }

        // Do not assume that a database starts its generated IDs at one. Existing
        // databases may already contain rows or have advanced identity values.
        Department finance = departmentRepository.findByName("Finance Division").orElseThrow();
        Department admin = departmentRepository.findByName("Administration Division").orElseThrow();
        Department hr = departmentRepository.findByName("Human Resources Division").orElseThrow();
        Department it = departmentRepository.findByName("IT Division").orElseThrow();

        if (officerRepository.count() == 0) {
            officerRepository.save(new Officer(null, "EMP001", "A. Perera", "aperera@example.com", finance));
            officerRepository.save(new Officer(null, "EMP002", "K. Silva", "ksilva@example.com", admin));
            officerRepository.save(new Officer(null, "EMP003", "N. Fernando", "nfernando@example.com", it));
            officerRepository.save(new Officer(null, "EMP004", "S. Perera", "sperera@example.com", hr));
        }

        if (trainingRepository.count() == 0) {
            trainingRepository.save(new Training(null, "Leadership Development Programme",
                    LocalDate.of(2026, 9, 20), "Main Training Hall", "John Silva", 50));
            trainingRepository.save(new Training(null, "Project Management Training",
                    LocalDate.of(2026, 10, 5), "Conference Room A", "Mary Gomes", 30));
            trainingRepository.save(new Training(null, "Communication Skills Training",
                    LocalDate.of(2026, 10, 12), "Conference Room B", "Peter Jay", 25));
        }
    }
}
