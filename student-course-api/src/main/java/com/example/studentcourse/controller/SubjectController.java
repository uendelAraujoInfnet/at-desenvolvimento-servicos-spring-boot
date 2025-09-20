package com.example.studentcourse.controller;

import com.example.studentcourse.dto.SubjectDTO;
import com.example.studentcourse.model.Subject;
import com.example.studentcourse.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @PostMapping
    public ResponseEntity<Subject> create(@Valid @RequestBody SubjectDTO subjectDTO) {
        Subject subject = new Subject(subjectDTO.nome, subjectDTO.codigo);
        Subject createdSubject = subjectService.create(subject);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSubject);
    }

    @GetMapping
    public List<Subject> findAll() {
        return subjectService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subject> findById(@PathVariable Long id) {
        Subject subject = subjectService.findById(id);
        return ResponseEntity.ok(subject);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subject> update(@PathVariable Long id, @Valid @RequestBody SubjectDTO subjectDTO) {
        Subject subject = new Subject(subjectDTO.nome, subjectDTO.codigo);
        Subject updatedSubject = subjectService.update(id, subject);
        return ResponseEntity.ok(updatedSubject);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subjectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
