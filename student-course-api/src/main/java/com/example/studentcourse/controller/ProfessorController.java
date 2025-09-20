package com.example.studentcourse.controller;

import com.example.studentcourse.model.Professor;
import com.example.studentcourse.service.ProfessorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/professors")
public class ProfessorController {

    private final ProfessorService professorService;

    public ProfessorController(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @PostMapping
    public ResponseEntity<Professor> create(@RequestBody Professor professor) {
        Professor createdProfessor = professorService.create(professor);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProfessor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Professor>  update(@PathVariable Long id, @Valid @RequestBody Professor professor) {
        Professor  updatedProfessor = professorService.update(id, professor);
        return ResponseEntity.ok(updatedProfessor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        professorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
