package com.example.studentcourse.service;

import com.example.studentcourse.model.Subject;
import com.example.studentcourse.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public Subject create(Subject subject) {
        subjectRepository.findByCodigo(subject.getCodigo()).ifPresent(e -> {
            throw new IllegalArgumentException("Subject already exists ( Código já existe )");
        });
        return subjectRepository.save(subject);
    }

    public List<Subject> findAll() {
        return subjectRepository.findAll();
    }

    public Subject findById(Long id) {
        return subjectRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("Subject not found ( Disciplina não encontrada )"));
    }

    @Transactional
    public Subject update(Long id, Subject subjectUpdate) {
        Subject existingSubject = findById(id);
        if(!existingSubject.getCodigo().equals(subjectUpdate.getCodigo())) {
            subjectRepository.findByCodigo(subjectUpdate.getCodigo()).ifPresent(e -> {
                throw new IllegalArgumentException("Subject already exists ( Código já existe )");
            });
        }
        existingSubject.setNome(subjectUpdate.getNome());
        existingSubject.setCodigo(subjectUpdate.getCodigo());
        return subjectRepository.save(existingSubject);
    }

    @Transactional
    public void delete(Long id) {
        Subject existingSubject = findById(id);
        subjectRepository.delete(existingSubject);
    }
}
