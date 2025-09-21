package com.example.studentcourse.controller;

import com.example.studentcourse.dto.EnrollmentDTO;
import com.example.studentcourse.dto.GradeDTO;
import com.example.studentcourse.model.Enrollment;
import com.example.studentcourse.model.Student;
import com.example.studentcourse.repository.EnrollmentRepository;
import com.example.studentcourse.service.EnrollmentService;
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

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests using full Spring context + MockMvc.
 * Updated to expect 4xx (client error) when service throws IllegalArgumentException,
 * because the application maps these exceptions to client errors.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Student makeStudent(Long id, String name) {
        Student s = new Student();
        s.setId(id);
        s.setNome(name);
        s.setCpf("CPF" + id);
        s.setEmail(name.toLowerCase() + "@ex.com");
        return s;
    }

    private Enrollment makeEnrollment(Long id, Student student, Double grade) {
        Enrollment e = new Enrollment();
        e.setId(id);
        e.setStudent(student);
        e.setGrade(grade);
        return e;
    }

    @Test
    @DisplayName("POST /api/enrollments -> 201 Created")
    void enroll_shouldReturnCreated() throws Exception {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.studentId = 1L; dto.subjectId = 2L;

        Enrollment returned = makeEnrollment(100L, makeStudent(1L, "Alice"), null);
        Mockito.when(enrollmentService.enroll(1L, 2L)).thenReturn(returned);

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.student.id").value(1));

        verify(enrollmentService).enroll(1L, 2L);
    }

    @Test
    @DisplayName("POST /api/enrollments -> service throws -> 4xx")
    void enroll_whenServiceThrows_shouldReturnClientError() throws Exception {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.studentId = 1L; dto.subjectId = 2L;

        // app maps IllegalArgumentException to 4xx (client error)
        Mockito.when(enrollmentService.enroll(anyLong(), anyLong()))
                .thenThrow(new IllegalArgumentException("already enrolled"));

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is4xxClientError());

        verify(enrollmentService).enroll(1L, 2L);
    }

    @Test
    @DisplayName("PUT /api/enrollments/{id}/grade -> 200 OK")
    void grade_shouldReturnOk() throws Exception {
        GradeDTO dto = new GradeDTO(); dto.grade = 8.5;

        Enrollment updated = makeEnrollment(5L, makeStudent(1L, "Alice"), 8.5);
        Mockito.when(enrollmentService.setGrade(5L, 8.5)).thenReturn(updated);

        mockMvc.perform(put("/api/enrollments/5/grade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.grade").value(8.5));

        verify(enrollmentService).setGrade(5L, 8.5);
    }

    @Test
    @DisplayName("PUT /api/enrollments/{id}/grade -> service throws -> 4xx")
    void grade_whenServiceThrows_shouldReturnClientError() throws Exception {
        GradeDTO dto = new GradeDTO(); dto.grade = 2.0;

        // change to IllegalArgumentException so framework maps to client error (consistent with app behavior)
        Mockito.when(enrollmentService.setGrade(anyLong(), anyDouble()))
                .thenThrow(new IllegalArgumentException("not found"));

        mockMvc.perform(put("/api/enrollments/999/grade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is4xxClientError());

        verify(enrollmentService).setGrade(999L, 2.0);
    }

    @Test
    @DisplayName("GET /api/enrollments -> returns repository list")
    void findAll_shouldReturnList() throws Exception {
        Enrollment e1 = makeEnrollment(1L, makeStudent(1L,"A"), 9.0);
        Enrollment e2 = makeEnrollment(2L, makeStudent(2L,"B"), 6.0);

        Mockito.when(enrollmentRepository.findAll()).thenReturn(List.of(e1, e2));

        mockMvc.perform(get("/api/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(enrollmentRepository).findAll();
    }

    @Test
    @DisplayName("GET /api/enrollments/subject/{id}/approved -> returns students")
    void approved_shouldReturnStudents() throws Exception {
        Student s1 = makeStudent(1L, "Alice");
        Student s2 = makeStudent(2L, "Bob");
        Mockito.when(enrollmentService.getApprovedBySubject(10L)).thenReturn(List.of(s1, s2));

        mockMvc.perform(get("/api/enrollments/subject/10/approved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome").value("Alice"));

        verify(enrollmentService).getApprovedBySubject(10L);
    }

    @Test
    @DisplayName("GET /api/enrollments/subject/{id}/failed -> returns students")
    void failed_shouldReturnStudents() throws Exception {
        Student s1 = makeStudent(3L, "Carol");
        Mockito.when(enrollmentService.getFailedBySubject(20L)).thenReturn(List.of(s1));

        mockMvc.perform(get("/api/enrollments/subject/20/failed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Carol"));

        verify(enrollmentService).getFailedBySubject(20L);
    }

    @Test
    @DisplayName("PUT /api/enrollments/{id} -> update success")
    void update_shouldReturnOk() throws Exception {
        EnrollmentDTO dto = new EnrollmentDTO(); dto.studentId = 7L; dto.subjectId = 8L;

        Enrollment updated = makeEnrollment(55L, makeStudent(7L,"Updated"), null);
        Mockito.when(enrollmentService.updateEnrollment(55L, 7L, 8L)).thenReturn(updated);

        mockMvc.perform(put("/api/enrollments/55")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(55))
                .andExpect(jsonPath("$.student.id").value(7));

        verify(enrollmentService).updateEnrollment(55L, 7L, 8L);
    }

    @Test
    @DisplayName("PUT /api/enrollments/{id} -> service throws -> 4xx")
    void update_whenServiceThrows_shouldReturnClientError() throws Exception {
        EnrollmentDTO dto = new EnrollmentDTO(); dto.studentId = 7L; dto.subjectId = 8L;

        Mockito.when(enrollmentService.updateEnrollment(anyLong(), anyLong(), anyLong()))
                .thenThrow(new IllegalArgumentException("conflict"));

        mockMvc.perform(put("/api/enrollments/55")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is4xxClientError());

        verify(enrollmentService).updateEnrollment(55L, 7L, 8L);
    }

    @Test
    @DisplayName("DELETE /api/enrollments/{id} -> 204 No Content")
    void delete_shouldReturnNoContent() throws Exception {
        doNothing().when(enrollmentService).deleteEnrollment(42L);

        mockMvc.perform(delete("/api/enrollments/42"))
                .andExpect(status().isNoContent());

        verify(enrollmentService).deleteEnrollment(42L);
    }

    @Test
    @DisplayName("DELETE /api/enrollments/{id} -> service throws -> 4xx")
    void delete_whenServiceThrows_shouldReturnClientError() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("not found")).when(enrollmentService).deleteEnrollment(99L);

        mockMvc.perform(delete("/api/enrollments/99"))
                .andExpect(status().is4xxClientError());

        verify(enrollmentService).deleteEnrollment(99L);
    }
}
