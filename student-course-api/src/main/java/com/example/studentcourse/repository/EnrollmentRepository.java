package com.example.studentcourse.repository;

import com.example.studentcourse.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findBySubjectId(Long subjectId);
    List<Enrollment> findByStudentId(Long studentId);

    Optional<Enrollment> findByStudentIdAndSubjectId(Long studentId, Long subjectId);
}
