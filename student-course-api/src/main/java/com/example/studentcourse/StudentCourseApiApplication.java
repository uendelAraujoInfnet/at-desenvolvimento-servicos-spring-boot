package com.example.studentcourse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "com.example.studentcourse.repository")
@SpringBootApplication
public class StudentCourseApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudentCourseApiApplication.class, args);
	}

}
