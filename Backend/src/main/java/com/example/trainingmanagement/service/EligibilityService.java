package com.example.trainingmanagement.service;

import com.example.trainingmanagement.dto.EligibilityResult;
import com.example.trainingmanagement.entity.EligibilityRule;
import com.example.trainingmanagement.entity.EligibilityRuleType;
import com.example.trainingmanagement.entity.NominationStatus;
import com.example.trainingmanagement.entity.Officer;
import com.example.trainingmanagement.entity.Training;
import com.example.trainingmanagement.repository.EligibilityRuleRepository;
import com.example.trainingmanagement.repository.NominationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EligibilityService {

    private final EligibilityRuleRepository ruleRepository;
    private final NominationRepository nominationRepository;

    public EligibilityService(EligibilityRuleRepository ruleRepository,
                              NominationRepository nominationRepository) {
        this.ruleRepository = ruleRepository;
        this.nominationRepository = nominationRepository;
    }

    public EligibilityResult checkEligibility(Officer officer, Training training) {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(12);
        var recentParticipation = nominationRepository
                .findByOfficerIdAndTrainingIdAndStatus(officer.getId(), training.getId(), NominationStatus.CONFIRMED)
                .stream()
                .filter(nomination -> nomination.getNominationDate() != null
                        && nomination.getNominationDate().isAfter(cutoff))
                .max((first, second) -> first.getNominationDate().compareTo(second.getNominationDate()));
        if (recentParticipation.isPresent()) {
            return new EligibilityResult(false,
                    "Officer participated in this training within the previous 12 months.",
                    recentParticipation.get().getNominationDate());
        }

        List<EligibilityRule> rules = ruleRepository.findByTrainingIdOrderByIdAsc(training.getId());
        for (EligibilityRuleType type : EligibilityRuleType.values()) {
            List<EligibilityRule> typeRules = rules.stream()
                    .filter(rule -> rule.getRuleType() == type)
                    .toList();
            if (typeRules.isEmpty()) {
                continue;
            }

            boolean matched = typeRules.stream().anyMatch(rule -> matches(rule, officer));
            if (!matched) {
                return new EligibilityResult(false, reasonFor(type, typeRules), null);
            }
        }
        return new EligibilityResult(true, "Officer meets all eligibility requirements.", null);
    }

    private boolean matches(EligibilityRule rule, Officer officer) {
        String value = rule.getRuleValue().trim();
        return switch (rule.getRuleType()) {
            case DEPARTMENT -> officer.getDepartment() != null
                    && sameDepartment(officer.getDepartment().getName(), value);
            case GRADE -> officer.getGrade() != null && officer.getGrade().equalsIgnoreCase(value);
            case MIN_YEARS_SERVICE -> {
                try {
                    yield officer.getYearsOfService() != null
                            && officer.getYearsOfService() >= Integer.parseInt(value);
                } catch (NumberFormatException exception) {
                    yield false;
                }
            }
        };
    }

    private boolean sameDepartment(String officerDepartment, String configuredDepartment) {
        String officerValue = officerDepartment.trim().toLowerCase();
        String configuredValue = configuredDepartment.trim().toLowerCase();
        return officerValue.equals(configuredValue)
                || officerValue.replaceFirst("\\s+division$", "").equals(configuredValue)
                || configuredValue.replaceFirst("\\s+division$", "").equals(officerValue);
    }

    private String reasonFor(EligibilityRuleType type, List<EligibilityRule> rules) {
        return switch (type) {
            case DEPARTMENT -> "Officer's department is not eligible for this training programme.";
            case GRADE -> "Officer does not meet the required grade for this training programme.";
            case MIN_YEARS_SERVICE -> "Officer must have at least "
                    + rules.get(0).getRuleValue() + " years of service.";
        };
    }

    public boolean isValidRuleValue(EligibilityRuleType type, String value) {
        if (type == EligibilityRuleType.MIN_YEARS_SERVICE) {
            try {
                return Integer.parseInt(value.trim()) >= 0;
            } catch (NumberFormatException exception) {
                return false;
            }
        }
        return !value.trim().isEmpty();
    }
}
