package com.example.studentcourse.service;

import com.example.studentcourse.model.Enrollment;
import com.example.studentcourse.model.Student;
import com.example.studentcourse.model.Subject;
import com.example.studentcourse.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentService studentService;
    private final SubjectService subjectService;

    public EnrollmentService(EnrollmentRepository enrollmentRepository, StudentService studentService, SubjectService subjectService) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentService = studentService;
        this.subjectService = subjectService;
    }

    @Transactional
    public Enrollment enroll(Long studentId, Long subjectId) {
        Student student = studentService.findById(studentId);
        Subject subject = subjectService.findById(subjectId);
        enrollmentRepository.findByStudentIdAndSubjectId(studentId, subjectId).ifPresent(e ->{
            throw new IllegalArgumentException("Student already registered in the subject ( Aluno já cadastrado na disciplina ) ");
        });
        Enrollment enrollment = new Enrollment(student, subject);
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public Enrollment setGrade(Long enrollmentId, Double grade) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(() ->
                new IllegalArgumentException("Registration not found ( Matrícula não encontrada )"));
        enrollment.setGrade(grade);
        return enrollmentRepository.save(enrollment);
    }

    public List<Student> getApprovedBySubject(Long subjectId) {
        return enrollmentRepository.findBySubjectId(subjectId).stream()
                .filter(en -> en.getGrade() != null && en.getGrade() >= 7.0)
                .map(Enrollment::getStudent).collect(Collectors.toList());
    }

    public List<Student> getFailedBySubject(Long subjectId) {
        return enrollmentRepository.findBySubjectId(subjectId).stream()
                .filter(en -> en.getGrade() != null && en.getGrade() < 7.0)
                .map(Enrollment::getStudent).collect(Collectors.toList());
    }

    @Transactional
    public Enrollment updateEnrollment(Long enrollmentId, Long newStudentId, Long newSubjectId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(() ->
                new IllegalArgumentException("Registration not found ( Matrícula não encontrada )"));
        Student student = studentService.findById(newStudentId);
        Subject subject = subjectService.findById(newSubjectId);
        enrollmentRepository.findByStudentIdAndSubjectId(newStudentId, newSubjectId).ifPresent(existingEnrollment ->{
            if(!existingEnrollment.getId().equals(enrollmentId)){
                throw new IllegalArgumentException("There is already an enrollment for this student in this subject ( Já existe matrícula para esse aluno nessa disciplina )");
            }
        });
        enrollment.setStudent(student);
        enrollment.setSubject(subject);
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void deleteEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(() ->
                new IllegalArgumentException("Registration not found ( Matrícula não encontrada )"));
        enrollmentRepository.delete(enrollment);
    }
}
