package com.example.studentcourse.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;

@Entity
@Table(name = "enrollments", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id" , "subject_id"} ))
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Student student;

    @ManyToOne(optional = false)
    private Subject subject;

    @Nullable
    private Double grade;

    public Enrollment() {}

    public Enrollment(Long id, Student student, Subject subject, @Nullable Double grade) {
        this.id = id;
        this.student = student;
        this.subject = subject;
        this.grade = grade;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    @Nullable
    public Double getGrade() {
        return grade;
    }

    public void setGrade(@Nullable Double grade) {
        this.grade = grade;
    }
}
