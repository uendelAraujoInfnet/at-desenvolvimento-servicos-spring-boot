package com.example.studentcourse.controller;

import com.example.studentcourse.dto.ProfessorDTO;
import com.example.studentcourse.model.Professor;
import com.example.studentcourse.service.ProfessorService;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-level tests for ProfessorController.
 * Uses full context + MockMvc because @WebMvcTest might fail in some projects.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class ProfessorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfessorService professorService;

    @Autowired
    private ObjectMapper objectMapper;

    private Professor makeProfessor(Long id, String username, String password, String nome) {
        Professor p = new Professor();
        p.setId(id);
        p.setUsername(username);
        p.setPassword(password);
        p.setNome(nome);
        return p;
    }

    @Test
    @DisplayName("POST /api/professors -> 201 Created and returns professor (password overwritten by controller)")
    void create_shouldReturnCreated() throws Exception {
        ProfessorDTO dto = new ProfessorDTO();
        dto.username = "prof1";
        dto.password = "plain";
        dto.nome = "Prof One";

        // service will return entity with encoded password; controller resets to plain password before returning
        Professor saved = makeProfessor(10L, "prof1", "encoded-pass", "Prof One");
        Mockito.when(professorService.create(any())).thenReturn(saved);

        mockMvc.perform(post("/api/professors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.username").value("prof1"))
                // controller sets password back to incoming DTO password before returning
                .andExpect(jsonPath("$.password").value("plain"))
                .andExpect(jsonPath("$.nome").value("Prof One"));

        // verify service called with professor having same username (password encoding handled in service)
        verify(professorService).create(Mockito.argThat(p -> "prof1".equals(p.getUsername())));
    }

    @Test
    @DisplayName("POST /api/professors -> 4xx when service throws (user exists)")
    void create_whenServiceThrows_shouldReturnClientError() throws Exception {
        ProfessorDTO dto = new ProfessorDTO();
        dto.username = "prof2";
        dto.password = "pwd";
        dto.nome = "Name";

        Mockito.when(professorService.create(any())).thenThrow(new IllegalArgumentException("User already exists"));

        mockMvc.perform(post("/api/professors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is4xxClientError());

        verify(professorService).create(any());
    }

    @Test
    @DisplayName("PUT /api/professors/{id} -> 200 OK and returns updated professor")
    void update_shouldReturnOk() throws Exception {
        ProfessorDTO dto = new ProfessorDTO();
        dto.username = "newuser";
        dto.password = "newpwd";
        dto.nome = "New Name";

        Professor returned = makeProfessor(5L, "newuser", "encoded", "New Name");
        Mockito.when(professorService.update(eq(5L), any())).thenReturn(returned);

        mockMvc.perform(put("/api/professors/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.username").value("newuser"))
                // controller replaces password with plain from DTO
                .andExpect(jsonPath("$.password").value("newpwd"))
                .andExpect(jsonPath("$.nome").value("New Name"));

        verify(professorService).update(eq(5L), Mockito.argThat(p -> "newuser".equals(p.getUsername())));
    }

    @Test
    @DisplayName("PUT /api/professors/{id} -> 4xx when service throws (not found or username conflict)")
    void update_whenServiceThrows_shouldReturnClientError() throws Exception {
        ProfessorDTO dto = new ProfessorDTO();
        dto.username = "foo";
        dto.password = "bar";
        dto.nome = "Name";

        Mockito.when(professorService.update(anyLong(), any())).thenThrow(new IllegalArgumentException("Professor not found"));

        mockMvc.perform(put("/api/professors/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is4xxClientError());

        verify(professorService).update(eq(99L), any());
    }

    @Test
    @DisplayName("DELETE /api/professors/{id} -> 204 No Content")
    void delete_shouldReturnNoContent() throws Exception {
        // service returns void; mock to do nothing
        Mockito.doNothing().when(professorService).delete(7L);

        mockMvc.perform(delete("/api/professors/7"))
                .andExpect(status().isNoContent());

        verify(professorService).delete(7L);
    }

    @Test
    @DisplayName("DELETE /api/professors/{id} -> 4xx when service throws")
    void delete_whenServiceThrows_shouldReturnClientError() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Professor not found")).when(professorService).delete(88L);

        mockMvc.perform(delete("/api/professors/88"))
                .andExpect(status().is4xxClientError());

        verify(professorService).delete(88L);
    }
}
