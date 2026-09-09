package com.example.trainingmanagement.repository;

import com.example.trainingmanagement.entity.Officer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OfficerRepository extends JpaRepository<Officer, Long> {

    boolean existsByEmployeeId(String employeeId);

    Optional<Officer> findByEmployeeId(String employeeId);
}
