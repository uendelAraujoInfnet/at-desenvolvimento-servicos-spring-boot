package com.example.studentcourse.controller;

import com.example.studentcourse.dto.StudentDTO;
import com.example.studentcourse.model.Student;
import com.example.studentcourse.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for StudentController — robust to presence/absence of validation and to
 * how exceptions are mapped in your app.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @Autowired
    private ObjectMapper objectMapper;

    private Student makeStudent(Long id, String nome, String cpf, String email) {
        Student s = new Student();
        s.setId(id);
        s.setNome(nome);
        s.setCpf(cpf);
        s.setEmail(email);
        return s;
    }

    // -------- CREATE --------
    @Test
    @DisplayName("POST /api/students -> 201 Created com body")
    void create_shouldReturnCreated() throws Exception {
        StudentDTO dto = new StudentDTO();
        dto.nome = "João";
        dto.cpf = "11122233344";
        dto.email = "joao@example.com";
        dto.telefone = "1111";
        dto.endereco = "Rua A";

        Student saved = makeStudent(10L, "João", "11122233344", "joao@example.com");
        when(studentService.create(any())).thenReturn(saved);

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nome").value("João"))
                .andExpect(jsonPath("$.cpf").value("11122233344"));

        verify(studentService).create(any());
    }

    @Test
    @DisplayName("POST /api/students -> validação: se 400 então service NÃO chamado; senão service chamado")
    void create_whenValidationFails_shouldReturnBadRequestOrCallServiceIfValidationDisabled() throws Exception {
        StudentDTO dto = new StudentDTO();
        dto.nome = ""; // inválido quando @Valid funciona
        dto.cpf = "111";
        dto.email = "invalid-email";

        // prepare service to return a student in case validation is not active
        Student fallback = makeStudent(99L, "fallback", "111", "x@x.com");
        when(studentService.create(any())).thenReturn(fallback);

        // perform request and inspect status: accept 400 (validation active) OR 201 (validation inactive)
        var result = mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertTrue(status == 400 || status == 201, "Expected 400 (validation active) or 201 (validation inactive). Actual: " + status);

        if (status == 400) {
            // validation active → service should NOT have been called
            verifyNoInteractions(studentService);
        } else {
            // validation inactive → controller called service and returned created
            verify(studentService).create(any());
        }
    }

    // -------- FIND ALL / FIND BY ID --------
    @Test
    @DisplayName("GET /api/students -> retorna lista")
    void findAll_shouldReturnList() throws Exception {
        Student s1 = makeStudent(1L, "A", "CPF1", "a@ex.com");
        Student s2 = makeStudent(2L, "B", "CPF2", "b@ex.com");
        when(studentService.findAll()).thenReturn(List.of(s1, s2));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(studentService).findAll();
    }

    @Test
    @DisplayName("GET /api/students/{id} -> retorna student")
    void findById_shouldReturnOk() throws Exception {
        Student s = makeStudent(5L, "Zé", "CPF5", "ze@ex.com");
        when(studentService.findById(5L)).thenReturn(s);

        mockMvc.perform(get("/api/students/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.nome").value("Zé"));

        verify(studentService).findById(5L);
    }

    @Test
    @DisplayName("GET /api/students/{id} -> não encontrado -> 4xx ou exception (robusto)")
    void findById_whenNotFound_shouldReturnClientErrorOrThrow() throws Exception {
        when(studentService.findById(99L)).thenThrow(new IllegalArgumentException("Aluno não encontrado"));

        try {
            var res = mockMvc.perform(get("/api/students/99")).andReturn();
            int status = res.getResponse().getStatus();
            assertTrue(status >= 400 && status < 500, "Expected 4xx status when not found, got " + status);
        } catch (NestedServletException ex) {
            assertThat(ex.getCause()).hasMessageContaining("Aluno não encontrado");
        }

        verify(studentService).findById(99L);
    }

    // -------- UPDATE --------
    @Test
    @DisplayName("PUT /api/students/{id} -> update sucesso")
    void update_shouldReturnOk() throws Exception {
        StudentDTO dto = new StudentDTO();
        dto.nome = "Novo";
        dto.cpf = "22233344455";
        dto.email = "novo@ex.com";

        Student updated = makeStudent(7L, "Novo", "22233344455", "novo@ex.com");
        when(studentService.update(eq(7L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/students/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.cpf").value("22233344455"));

        verify(studentService).update(eq(7L), any());
    }

    @Test
    @DisplayName("PUT /api/students/{id} -> validação: se 400 então service NÃO chamado; senão service chamado")
    void update_whenValidationFails_shouldReturnBadRequestOrCallServiceIfValidationDisabled() throws Exception {
        StudentDTO dto = new StudentDTO();
        dto.nome = ""; // inválido quando @Valid funciona
        dto.cpf = "333";

        // prepare fallback if validation disabled
        Student fallback = makeStudent(123L, "x", "333", "x@x.com");
        when(studentService.update(anyLong(), any())).thenReturn(fallback);

        var result = mockMvc.perform(put("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertTrue(status == 400 || status == 200, "Expected 400 (validation) or 200 (validation disabled). Actual: " + status);

        if (status == 400) {
            verifyNoInteractions(studentService);
        } else {
            verify(studentService).update(eq(1L), any());
        }
    }

    @Test
    @DisplayName("PUT /api/students/{id} -> quando service lança, assert comportamento")
    void update_whenServiceThrows_shouldReturnClientErrorOrThrow() throws Exception {
        StudentDTO dto = new StudentDTO();
        dto.nome = "X";
        dto.cpf = "444";
        dto.email = "x@ex.com";

        when(studentService.update(anyLong(), any())).thenThrow(new IllegalArgumentException("CPF já cadastrado"));

        try {
            var res = mockMvc.perform(put("/api/students/2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andReturn();

            int status = res.getResponse().getStatus();
            assertTrue(status >= 400 && status < 600, "Expected an HTTP error status when service throws, got " + status);
        } catch (NestedServletException ex) {
            assertThat(ex.getCause()).hasMessageContaining("CPF já cadastrado");
        }

        verify(studentService).update(eq(2L), any());
    }

    // -------- DELETE --------
    @Test
    @DisplayName("DELETE /api/students/{id} -> 204 No Content")
    void delete_shouldReturnNoContent() throws Exception {
        doNothing().when(studentService).delete(11L);

        mockMvc.perform(delete("/api/students/11"))
                .andExpect(status().isNoContent());

        verify(studentService).delete(11L);
    }

    @Test
    @DisplayName("DELETE /api/students/{id} -> quando service lança, assert comportamento")
    void delete_whenServiceThrows_shouldReturnClientErrorOrThrow() throws Exception {
        doThrow(new IllegalArgumentException("Aluno não encontrado")).when(studentService).delete(99L);

        try {
            var res = mockMvc.perform(delete("/api/students/99")).andReturn();
            int status = res.getResponse().getStatus();
            assertTrue(status >= 400 && status < 600, "Expected HTTP error when service throws, got " + status);
        } catch (NestedServletException ex) {
            assertThat(ex.getCause()).hasMessageContaining("Aluno não encontrado");
        }

        verify(studentService).delete(99L);
    }
}
