package com.example.trainingmanagement.dto;

import java.time.LocalDateTime;

public record EligibilityResult(boolean eligible, String reason, LocalDateTime lastParticipationDate) {
}
