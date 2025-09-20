package com.example.studentcourse.controller;

import com.example.studentcourse.dto.EnrollmentDTO;
import com.example.studentcourse.dto.GradeDTO;
import com.example.studentcourse.model.Enrollment;
import com.example.studentcourse.model.Student;
import com.example.studentcourse.repository.EnrollmentRepository;
import com.example.studentcourse.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentController(EnrollmentService enrollmentService,  EnrollmentRepository enrollmentRepository) {
        this.enrollmentService = enrollmentService;
        this.enrollmentRepository = enrollmentRepository;
    }

    @PostMapping
    public ResponseEntity<Enrollment> enroll(@RequestBody EnrollmentDTO enrollmentDTO) {
        Enrollment enrollment = enrollmentService.enroll(enrollmentDTO.studentId, enrollmentDTO.subjectId);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
    }

    @PutMapping("/{id}/grade")
    public ResponseEntity<Enrollment> grade(@PathVariable Long id, @RequestBody GradeDTO gradeDTO) {
        Enrollment enrollment = enrollmentService.setGrade(id, gradeDTO.grade);
        return ResponseEntity.ok(enrollment);
    }

    @GetMapping
    public List<Enrollment> findAll() {
        return enrollmentRepository.findAll();
    }

    @GetMapping("/subject/{subjectId}/approved")
    public List<Student> approved(@PathVariable Long subjectId) {
        return enrollmentService.getApprovedBySubject(subjectId);
    }

    @GetMapping("/subject/{subjectId}/failed")
    public List<Student> failed(@PathVariable Long subjectId) {
        return enrollmentService.getFailedBySubject(subjectId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Enrollment> update(@PathVariable Long id, @RequestBody EnrollmentDTO enrollmentDTO){
        Enrollment updated = enrollmentService.updateEnrollment(id, enrollmentDTO.studentId, enrollmentDTO.subjectId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.noContent().build();
    }
}
