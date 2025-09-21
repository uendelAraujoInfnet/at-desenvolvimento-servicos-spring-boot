package com.example.studentcourse.service;

import com.example.studentcourse.model.Professor;
import com.example.studentcourse.repository.ProfessorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProfessorService using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ProfessorServiceTest {

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfessorService professorService;

    private Professor input;

    @BeforeEach
    void setUp() {
        input = new Professor("user1", "plainpwd", "Nome");
    }

    @Test
    void create_success_shouldEncodePasswordAndSave() {
        when(professorRepository.findByUsername("user1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plainpwd")).thenReturn("encodedPwd");
        Professor saved = new Professor(11L, "user1", "encodedPwd", "Nome");
        when(professorRepository.save(any(Professor.class))).thenReturn(saved);

        Professor result = professorService.create(input);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(11L);
        // ensure password was encoded before saving
        verify(passwordEncoder).encode("plainpwd");
        verify(professorRepository).save(Mockito.argThat(p -> "encodedPwd".equals(p.getPassword())));
    }

    @Test
    void create_whenUsernameExists_shouldThrow() {
        when(professorRepository.findByUsername("user1")).thenReturn(Optional.of(new Professor()));
        assertThatThrownBy(() -> professorService.create(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuário já existe");
        verify(professorRepository, never()).save(any());
    }

    @Test
    void update_success_changeUsernameAndPassword() {
        Professor existing = new Professor(20L, "old", "oldEncoded", "Old Name");
        when(professorRepository.findById(20L)).thenReturn(Optional.of(existing));

        Professor update = new Professor("newuser", "newpass", "New Name");
        // no conflict for username
        when(professorRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("newpass")).thenReturn("newEncoded");
        when(professorRepository.save(any(Professor.class))).thenAnswer(inv -> inv.getArgument(0));

        Professor result = professorService.update(20L, update);

        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getNome()).isEqualTo("New Name");
        // password should have been encoded before saving
        verify(passwordEncoder).encode("newpass");
        verify(professorRepository).save(existing);
    }

    @Test
    void update_success_noPasswordChange_whenBlank() {
        Professor existing = new Professor(21L, "same", "oldEncoded", "Old Name");
        when(professorRepository.findById(21L)).thenReturn(Optional.of(existing));

        Professor update = new Professor("same", "   ", "Updated Name"); // blank password should be ignored
        when(professorRepository.save(any(Professor.class))).thenAnswer(inv -> inv.getArgument(0));

        Professor result = professorService.update(21L, update);

        assertThat(result.getNome()).isEqualTo("Updated Name");
        // passwordEncoder should not be called
        verify(passwordEncoder, never()).encode(anyString());
        verify(professorRepository).save(existing);
    }

    @Test
    void update_whenProfessorNotFound_shouldThrow() {
        when(professorRepository.findById(99L)).thenReturn(Optional.empty());
        Professor update = new Professor("u","p","n");
        assertThatThrownBy(() -> professorService.update(99L, update))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Professor não encontrado");
    }

    @Test
    void update_whenUsernameConflict_shouldThrow() {
        Professor existing = new Professor(30L, "old", "x", "Old");
        when(professorRepository.findById(30L)).thenReturn(Optional.of(existing));

        Professor other = new Professor(31L, "other", "x", "Other");
        // repository returns an existing user with username 'other' -> conflict
        when(professorRepository.findByUsername("other")).thenReturn(Optional.of(other));

        Professor update = new Professor("other", "p", "n");
        assertThatThrownBy(() -> professorService.update(30L, update))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username já existente");
    }

    @Test
    void delete_success_shouldDelete() {
        Professor existing = new Professor(40L, "del", "pw", "Nome");
        when(professorRepository.findById(40L)).thenReturn(Optional.of(existing));
        doNothing().when(professorRepository).delete(existing);

        professorService.delete(40L);

        verify(professorRepository).delete(existing);
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        when(professorRepository.findById(77L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> professorService.delete(77L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Professor não encontrado");
    }
}
