package com.example.trainingmanagement.dto;

import com.example.trainingmanagement.entity.EligibilityRuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EligibilityRuleRequest {
    private Long trainingId;

    @NotNull
    private EligibilityRuleType ruleType;

    @NotBlank
    private String ruleValue;
}
