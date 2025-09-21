package com.example.studentcourse.service;

import com.example.studentcourse.model.Student;
import com.example.studentcourse.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StudentService
 */
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private Student sample;

    @BeforeEach
    void setUp() {
        sample = new Student(1L, "João", "11122233344", "joao@ex.com", "1111", "Rua A");
    }

    @Test
    void create_success_shouldSaveAndReturn() {
        when(studentRepository.findByCpf(eq(sample.getCpf()))).thenReturn(Optional.empty());
        when(studentRepository.save(any(Student.class))).thenReturn(sample);

        Student created = studentService.create(new Student(null, sample.getNome(), sample.getCpf(), sample.getEmail(), sample.getTelefone(), sample.getEndereco()));
        assertThat(created).isNotNull();
        assertThat(created.getCpf()).isEqualTo(sample.getCpf());
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void create_duplicateCpf_shouldThrow() {
        when(studentRepository.findByCpf(eq(sample.getCpf()))).thenReturn(Optional.of(sample));

        Student toCreate = new Student(null, "X", sample.getCpf(), "x@ex.com", null, null);
        assertThatThrownBy(() -> studentService.create(toCreate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CPF já cadastrado");

        verify(studentRepository, never()).save(any());
    }

    @Test
    void findAll_shouldReturnList() {
        when(studentRepository.findAll()).thenReturn(List.of(sample));
        List<Student> all = studentService.findAll();
        assertThat(all).hasSize(1).contains(sample);
        verify(studentRepository).findAll();
    }

    @Test
    void findById_found_shouldReturn() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(sample));
        Student s = studentService.findById(1L);
        assertThat(s).isEqualTo(sample);
    }

    @Test
    void findById_notFound_shouldThrow() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> studentService.findById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Aluno não encontrado");
    }

    @Test
    void update_success_whenCpfUnchanged_shouldSaveUpdated() {
        Student existing = new Student(5L, "Old", "CPF5", "old@ex.com", null, null);
        when(studentRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(studentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Student update = new Student(null, "NewName", "CPF5", "new@ex.com", "tel", "addr");
        Student res = studentService.update(5L, update);

        assertThat(res.getNome()).isEqualTo("NewName");
        assertThat(res.getCpf()).isEqualTo("CPF5");
        verify(studentRepository).save(existing);
    }

    @Test
    void update_whenCpfConflict_shouldThrow() {
        Student existing = new Student(6L, "A", "CPF6", "a@ex.com", null, null);
        Student other = new Student(7L, "B", "CPF7", "b@ex.com", null, null);

        when(studentRepository.findById(6L)).thenReturn(Optional.of(existing));
        // attempt to update cpf to CPF7 which already exists
        when(studentRepository.findByCpf(eq("CPF7"))).thenReturn(Optional.of(other));

        Student update = new Student(null, "A2", "CPF7", "a2@ex.com", null, null);
        assertThatThrownBy(() -> studentService.update(6L, update))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CPF já cadastrado");

        verify(studentRepository, never()).save(any());
    }

    @Test
    void delete_success_shouldDelete() {
        Student existing = new Student(8L, "Del", "CPF8", "d@ex.com", null, null);
        when(studentRepository.findById(8L)).thenReturn(Optional.of(existing));
        doNothing().when(studentRepository).delete(existing);

        studentService.delete(8L);

        verify(studentRepository).delete(existing);
    }

    @Test
    void delete_notFound_shouldThrow() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> studentService.delete(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Aluno não encontrado");
    }
}
