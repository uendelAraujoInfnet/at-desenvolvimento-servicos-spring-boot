package com.example.studentcourse.repository;

import com.example.studentcourse.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfessorRepository  extends JpaRepository<Professor,Long> {

    Optional<Professor> findByUsername(String username);
}
