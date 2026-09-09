package com.example.trainingmanagement.repository;

import com.example.trainingmanagement.entity.EligibilityRule;
import com.example.trainingmanagement.entity.EligibilityRuleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EligibilityRuleRepository extends JpaRepository<EligibilityRule, Long> {
    List<EligibilityRule> findByTrainingIdOrderByIdAsc(Long trainingId);
    boolean existsByTrainingIdAndRuleTypeAndRuleValue(Long trainingId, EligibilityRuleType ruleType, String ruleValue);
}
