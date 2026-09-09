package com.example.trainingmanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NominationRequest {

    @NotNull(message = "Officer id is required")
    private Long officerId;

    @NotNull(message = "Training id is required")
    private Long trainingId;

    @NotNull(message = "Department id is required")
    private Long departmentId;
}