package com.example.studentcourse.service;

import com.example.studentcourse.model.Student;
import com.example.studentcourse.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student create(Student student) {
        studentRepository.findByCpf(student.getCpf()).ifPresent(e -> {
            throw new IllegalStateException("CPF already registered ( CPF já cadastrado )");
        });
        return studentRepository.save(student);
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Student findById(Long id) {
        return studentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Student not found ( Aluno não encontrado )"));
    }

    @Transactional
    public Student update(Long id, Student studentUpdate) {
        Student existingStudent = findById(id);

        if(!existingStudent.getCpf().equals(studentUpdate.getCpf())) {
            studentRepository.findByCpf(studentUpdate.getCpf()).ifPresent(e -> {
                throw new IllegalArgumentException("CPF alreadys registered ( CPF já cadastrado )");
            });
        }

        existingStudent.setNome(studentUpdate.getNome());
        existingStudent.setCpf(studentUpdate.getCpf());
        existingStudent.setEmail(studentUpdate.getEmail());
        existingStudent.setTelefone(studentUpdate.getTelefone());
        existingStudent.setEndereco(studentUpdate.getEndereco());
        return studentRepository.save(existingStudent);
    }

    @Transactional
    public void delete(Long id) {
        Student existingStudent = findById(id);
        studentRepository.delete(existingStudent);
    }
}
