package com.example.studentcourse.service;

import com.example.studentcourse.model.Professor;
import com.example.studentcourse.repository.ProfessorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfessorService {

    private final ProfessorRepository professorRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfessorService(ProfessorRepository professorRepository, PasswordEncoder passwordEncoder) {
        this.professorRepository = professorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Professor create(Professor professor) {
        professorRepository.findByUsername(professor.getUsername()).ifPresent(e -> {
            throw new IllegalArgumentException("User already exists ( Usuário já existe )");
        });
        professor.setPassword(passwordEncoder.encode(professor.getPassword()));
        return professorRepository.save(professor);
    }

    @Transactional
    public Professor update(Long id, Professor professorUpdate) {
        Professor existingProfessor = professorRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Professor not found ( Professor não encontrado )"));
        if(!existingProfessor.getUsername().equals(professorUpdate.getUsername())) {
            professorRepository.findByUsername(professorUpdate.getUsername()).ifPresent(e -> {
                throw new IllegalArgumentException("Username already existente ( Username já existente )");
            });
        }
        existingProfessor.setUsername(professorUpdate.getUsername());
        if (professorUpdate.getPassword() != null && !professorUpdate.getPassword().isBlank()) {
            existingProfessor.setPassword(passwordEncoder.encode(professorUpdate.getPassword()));
        }
        existingProfessor.setNome(professorUpdate.getNome());
        return professorRepository.save(existingProfessor);
    }

    @Transactional
    public void delete(Long id) {
        Professor existingProfessor = professorRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Professor not found ( Professor não encontrado )"));
        professorRepository.delete(existingProfessor);
    }
}
