package com.example.trainingmanagement.config;

import com.example.trainingmanagement.entity.Department;
import com.example.trainingmanagement.entity.Officer;
import com.example.trainingmanagement.entity.Training;
import com.example.trainingmanagement.entity.NominationStatus;
import com.example.trainingmanagement.entity.EligibilityRule;
import com.example.trainingmanagement.entity.EligibilityRuleType;
import com.example.trainingmanagement.dto.NominationRequest;
import com.example.trainingmanagement.repository.DepartmentRepository;
import com.example.trainingmanagement.repository.NominationRepository;
import com.example.trainingmanagement.repository.OfficerRepository;
import com.example.trainingmanagement.repository.TrainingRepository;
import com.example.trainingmanagement.repository.EligibilityRuleRepository;
import com.example.trainingmanagement.service.NominationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final OfficerRepository officerRepository;
    private final TrainingRepository trainingRepository;
    private final NominationRepository nominationRepository;
    private final NominationService nominationService;
    private final EligibilityRuleRepository eligibilityRuleRepository;

    public DataInitializer(DepartmentRepository departmentRepository,
                           OfficerRepository officerRepository,
                           TrainingRepository trainingRepository,
                           NominationRepository nominationRepository,
                           NominationService nominationService,
                           EligibilityRuleRepository eligibilityRuleRepository) {
        this.departmentRepository = departmentRepository;
        this.officerRepository = officerRepository;
        this.trainingRepository = trainingRepository;
        this.nominationRepository = nominationRepository;
        this.nominationService = nominationService;
        this.eligibilityRuleRepository = eligibilityRuleRepository;
    }

    @Override
    public void run(String... args) {

        // Records created before Task 2 had no status. They were valid active
        // nominations, so retain them as confirmed participants.
        nominationRepository.updateMissingStatus(NominationStatus.CONFIRMED);

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
        Department budget = getOrCreateDepartment("Budget");
        Department planning = getOrCreateDepartment("Planning");
        Department ict = getOrCreateDepartment("ICT");

        saveOfficerIfMissing("EMP001", "A. Perera", "aperera@example.com", finance, "Senior Officer", 7);
        saveOfficerIfMissing("EMP002", "K. Silva", "ksilva@example.com", admin, "Junior Officer", 3);
        saveOfficerIfMissing("EMP003", "N. Fernando", "nfernando@example.com", it, "Senior Officer", 5);
        saveOfficerIfMissing("EMP004", "S. Perera", "sperera@example.com", hr, "Junior Officer", 8);
        saveOfficerIfMissing("EMP005", "R. Kumar", "rkumar@example.com", finance, "Senior Officer", 6);
        saveOfficerIfMissing("EMP006", "D. Fernando", "dfernando@example.com", admin, "Junior Officer", 2);
        saveOfficerIfMissing("EMP007", "M. Silva", "msilva@example.com", it, "Senior Officer", 9);
        saveOfficerIfMissing("EMP008", "P. Gomes", "pgomes@example.com", hr, "Junior Officer", 4);

        if (trainingRepository.count() == 0) {
            trainingRepository.save(new Training(null, "Leadership Development Programme",
                    LocalDate.of(2026, 9, 20), "Main Training Hall", "John Silva", 50));
            trainingRepository.save(new Training(null, "Project Management Training",
                    LocalDate.of(2026, 10, 5), "Conference Room A", "Mary Gomes", 30));
            trainingRepository.save(new Training(null, "Communication Skills Training",
                    LocalDate.of(2026, 10, 12), "Conference Room B", "Peter Jay", 25));
        }

        Training cybersecurity = trainingRepository.findByTitle("Cybersecurity Awareness Programme")
                .orElseGet(() -> trainingRepository.save(new Training(null,
                        "Cybersecurity Awareness Programme",
                        LocalDate.of(2026, 10, 20), "Computer Lab", "Security Team", 5)));
        cybersecurity.setMaximumParticipants(5);
        trainingRepository.save(cybersecurity);
        trainingRepository.findAll().forEach(training -> {
            training.setMaximumParticipants(5);
            trainingRepository.save(training);
        });

        seedEligibilityTrainingData(finance, budget, planning, it, ict);

        seedCybersecurityNominations(cybersecurity);
    }

    private Department getOrCreateDepartment(String name) {
        return departmentRepository.findByName(name)
                .orElseGet(() -> departmentRepository.save(new Department(null, name)));
    }

    private void seedEligibilityTrainingData(Department finance, Department budget,
                                             Department planning, Department it, Department ict) {
        Training financial = getOrCreateTraining("Financial Management Programme");
        addRuleIfMissing(financial, EligibilityRuleType.DEPARTMENT, finance.getName());
        addRuleIfMissing(financial, EligibilityRuleType.DEPARTMENT, budget.getName());
        addRuleIfMissing(financial, EligibilityRuleType.DEPARTMENT, planning.getName());

        Training technical = getOrCreateTraining("Technical Programme");
        addRuleIfMissing(technical, EligibilityRuleType.DEPARTMENT, it.getName());
        addRuleIfMissing(technical, EligibilityRuleType.DEPARTMENT, ict.getName());

        Training management = getOrCreateTraining("Management Development Programme");
        addRuleIfMissing(management, EligibilityRuleType.GRADE, "Senior Officer");
        addRuleIfMissing(management, EligibilityRuleType.MIN_YEARS_SERVICE, "5");
        getOrCreateTraining("General Awareness Programme");
    }

    private Training getOrCreateTraining(String title) {
        return trainingRepository.findByTitle(title).orElseGet(() -> trainingRepository.save(new Training(
                null, title, LocalDate.of(2026, 12, 1), "Main Training Hall", "Training Division", 5)));
    }

    private void addRuleIfMissing(Training training, EligibilityRuleType type, String value) {
        if (!eligibilityRuleRepository.existsByTrainingIdAndRuleTypeAndRuleValue(training.getId(), type, value)) {
            eligibilityRuleRepository.save(new EligibilityRule(null, training, type, value));
        }
    }

    private void saveOfficerIfMissing(String employeeId, String name, String email, Department department,
                                      String grade, Integer yearsOfService) {
        Officer officer = officerRepository.findByEmployeeId(employeeId)
                .orElseGet(() -> new Officer(null, employeeId, name, email, grade, yearsOfService, department));
        if (officer.getGrade() == null) {
            officer.setGrade(grade);
        }
        if (officer.getYearsOfService() == null) {
            officer.setYearsOfService(yearsOfService);
        }
        officerRepository.save(officer);
    }

    private void seedCybersecurityNominations(Training training) {
        List<String> employeeIds = Arrays.asList(
                "EMP001", "EMP002", "EMP003", "EMP004",
                "EMP005", "EMP006", "EMP007", "EMP008");

        for (String employeeId : employeeIds) {
            Officer officer = officerRepository.findByEmployeeId(employeeId).orElseThrow();
            boolean alreadyActive = nominationRepository.existsByOfficerIdAndTrainingIdAndStatusIn(
                    officer.getId(), training.getId(),
                    Arrays.asList(NominationStatus.CONFIRMED, NominationStatus.WAITING));
            if (!alreadyActive) {
                nominationService.createNomination(new NominationRequest(
                        officer.getId(), training.getId(), officer.getDepartment().getId()));
            }
        }
    }
}
