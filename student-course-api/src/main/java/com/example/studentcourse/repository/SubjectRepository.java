package com.example.studentcourse.repository;

import com.example.studentcourse.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject,Long> {

    Optional<Subject> findByCodigo(String codigo);
}
