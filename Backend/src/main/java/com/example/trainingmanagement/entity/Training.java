package com.example.trainingmanagement.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "trainings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotBlank(message = "Venue is required")
    private String venue;

    @NotBlank(message = "Trainer is required")
    private String trainer;

    @NotNull(message = "Maximum participants is required")
    @Min(value = 1, message = "Maximum participants must be at least 1")
    private Integer maximumParticipants;
}
