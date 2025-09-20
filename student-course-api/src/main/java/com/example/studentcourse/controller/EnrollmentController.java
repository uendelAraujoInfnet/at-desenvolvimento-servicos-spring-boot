package com.example.studentcourse.controller;

import com.example.studentcourse.model.Enrollment;
import com.example.studentcourse.model.Student;
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

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public ResponseEntity<Enrollment> enroll(@RequestBody Map<String, Long> payload) {
        Long studentId = payload.get("studentId");
        Long subjectId = payload.get("subjectId");
        Enrollment enrollment = enrollmentService.enroll(studentId, subjectId);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
    }

    @PutMapping("/{id}/grade")
    public ResponseEntity<Enrollment> grade(@PathVariable Long id, @RequestBody Map<String, Double> payload) {
        Double grade = payload.get("grade");
        Enrollment enrollment = enrollmentService.setGrade(id, grade);
        return ResponseEntity.ok(enrollment);
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
    public ResponseEntity<Enrollment> update(@PathVariable Long id, @RequestBody Map<String, Long> payload){
        Long studentId = payload.get("studentId");
        Long subjectId = payload.get("subjectId");
        Enrollment updated = enrollmentService.updateEnrollment(id, studentId, subjectId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.noContent().build();
    }
}
