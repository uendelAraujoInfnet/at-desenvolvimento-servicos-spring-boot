package com.example.studentcourse.service;

import com.example.studentcourse.model.Enrollment;
import com.example.studentcourse.model.Student;
import com.example.studentcourse.model.Subject;
import com.example.studentcourse.repository.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private StudentService studentService;

    @Mock
    private SubjectService subjectService;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Student studentA;
    private Subject subjectX;

    @BeforeEach
    void setUp() {
        studentA = new Student(); studentA.setId(1L); studentA.setNome("Alice"); studentA.setCpf("CPF1");
        subjectX = new Subject(); subjectX.setId(2L); subjectX.setCodigo("S2"); subjectX.setNome("Subject X");
    }

    @Test
    void enroll_success_shouldSaveAndReturn() {
        when(studentService.findById(1L)).thenReturn(studentA);
        when(subjectService.findById(2L)).thenReturn(subjectX);
        when(enrollmentRepository.findByStudentIdAndSubjectId(1L, 2L)).thenReturn(Optional.empty());

        Enrollment toSave = new Enrollment(); toSave.setId(10L); toSave.setStudent(studentA); toSave.setSubject(subjectX);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(toSave);

        Enrollment result = enrollmentService.enroll(1L, 2L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void enroll_whenAlreadyEnrolled_shouldThrow() {
        when(studentService.findById(1L)).thenReturn(studentA);
        when(subjectService.findById(2L)).thenReturn(subjectX);

        Enrollment existing = new Enrollment(); existing.setId(99L);
        when(enrollmentRepository.findByStudentIdAndSubjectId(1L, 2L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> enrollmentService.enroll(1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Aluno já cadastrado");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void setGrade_found_shouldSetAndSave() {
        Enrollment e = new Enrollment(); e.setId(5L); e.setStudent(studentA); e.setSubject(subjectX); e.setGrade(null);

        when(enrollmentRepository.findById(5L)).thenReturn(Optional.of(e));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        Enrollment updated = enrollmentService.setGrade(5L, 7.5);
        assertThat(updated.getGrade()).isEqualTo(7.5);
        verify(enrollmentRepository).save(e);
    }

    @Test
    void setGrade_notFound_shouldThrow() {
        when(enrollmentRepository.findById(77L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> enrollmentService.setGrade(77L, 6.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Matrícula não encontrada");
    }

    @Test
    void getApprovedBySubject_filtersAndMapsCorrectly() {
        Student a = studentA;
        Student b = new Student(); b.setId(2L); b.setNome("Bob");

        Enrollment e1 = new Enrollment(); e1.setId(1L); e1.setStudent(a); e1.setSubject(subjectX); e1.setGrade(9.0);
        Enrollment e2 = new Enrollment(); e2.setId(2L); e2.setStudent(b); e2.setSubject(subjectX); e2.setGrade(5.0);
        Enrollment e3 = new Enrollment(); e3.setId(3L); e3.setStudent(b); e3.setSubject(subjectX); e3.setGrade(null);

        when(enrollmentRepository.findBySubjectId(2L)).thenReturn(List.of(e1, e2, e3));

        List<Student> approved = enrollmentService.getApprovedBySubject(2L);
        assertThat(approved).hasSize(1).containsExactly(a);
    }

    @Test
    void getFailedBySubject_filtersAndMapsCorrectly() {
        Student a = studentA;
        Student b = new Student(); b.setId(2L); b.setNome("Bob");

        Enrollment e1 = new Enrollment(); e1.setId(1L); e1.setStudent(a); e1.setSubject(subjectX); e1.setGrade(6.0);
        Enrollment e2 = new Enrollment(); e2.setId(2L); e2.setStudent(b); e2.setSubject(subjectX); e2.setGrade(8.0);

        when(enrollmentRepository.findBySubjectId(2L)).thenReturn(List.of(e1, e2));

        List<Student> failed = enrollmentService.getFailedBySubject(2L);
        assertThat(failed).hasSize(1).containsExactly(a);
    }

    @Test
    void updateEnrollment_success_shouldSaveUpdated() {
        Enrollment existing = new Enrollment(); existing.setId(50L); existing.setStudent(studentA); existing.setSubject(subjectX);

        Student newStudent = new Student(); newStudent.setId(7L); newStudent.setNome("New");
        Subject newSubject = new Subject(); newSubject.setId(8L); newSubject.setCodigo("C8"); newSubject.setNome("NewSub");

        when(enrollmentRepository.findById(50L)).thenReturn(Optional.of(existing));
        when(studentService.findById(7L)).thenReturn(newStudent);
        when(subjectService.findById(8L)).thenReturn(newSubject);
        when(enrollmentRepository.findByStudentIdAndSubjectId(7L, 8L)).thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        Enrollment updated = enrollmentService.updateEnrollment(50L, 7L, 8L);
        assertThat(updated.getStudent()).isEqualTo(newStudent);
        assertThat(updated.getSubject()).isEqualTo(newSubject);
        verify(enrollmentRepository).save(existing);
    }

    @Test
    void updateEnrollment_notFound_shouldThrow() {
        when(enrollmentRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> enrollmentService.updateEnrollment(999L, 7L, 8L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Matrícula não encontrada");
    }

    @Test
    void updateEnrollment_conflictDifferentEnrollment_shouldThrow() {
        Enrollment existing = new Enrollment(); existing.setId(60L);
        Enrollment other = new Enrollment(); other.setId(61L);

        when(enrollmentRepository.findById(60L)).thenReturn(Optional.of(existing));
        when(studentService.findById(10L)).thenReturn(studentA);
        when(subjectService.findById(11L)).thenReturn(subjectX);
        when(enrollmentRepository.findByStudentIdAndSubjectId(10L, 11L)).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> enrollmentService.updateEnrollment(60L, 10L, 11L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Já existe matrícula");
    }

    @Test
    void updateEnrollment_conflictSameEnrollment_shouldAllow() {
        Enrollment existing = new Enrollment(); existing.setId(70L);
        when(enrollmentRepository.findById(70L)).thenReturn(Optional.of(existing));

        Student s = new Student(); s.setId(5L);
        Subject subj = new Subject(); subj.setId(6L);

        when(studentService.findById(5L)).thenReturn(s);
        when(subjectService.findById(6L)).thenReturn(subj);
        when(enrollmentRepository.findByStudentIdAndSubjectId(5L, 6L)).thenReturn(Optional.of(existing));
        when(enrollmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Enrollment updated = enrollmentService.updateEnrollment(70L, 5L, 6L);
        assertThat(updated).isNotNull();
        verify(enrollmentRepository).save(existing);
    }

    @Test
    void deleteEnrollment_success_shouldDelete() {
        Enrollment e = new Enrollment(); e.setId(200L);
        when(enrollmentRepository.findById(200L)).thenReturn(Optional.of(e));
        doNothing().when(enrollmentRepository).delete(e);

        enrollmentService.deleteEnrollment(200L);

        verify(enrollmentRepository).delete(e);
    }

    @Test
    void deleteEnrollment_notFound_shouldThrow() {
        when(enrollmentRepository.findById(300L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> enrollmentService.deleteEnrollment(300L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Matrícula não encontrada");
    }
}
