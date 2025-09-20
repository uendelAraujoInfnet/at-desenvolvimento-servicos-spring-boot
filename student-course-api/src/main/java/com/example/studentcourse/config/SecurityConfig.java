package com.example.studentcourse.config;

import com.example.studentcourse.model.Professor;
import com.example.studentcourse.repository.ProfessorRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService(ProfessorRepository professorRepository) {
        return username -> professorRepository.findByUsername(username)
                .map(professor -> User.withUsername(professor.getUsername())
                        .password(professor.getPassword())
                        .roles("PROFESSOR")
                        .build()).orElseThrow(() ->
                        new UsernameNotFoundException("Professor not found ( Professor não encontrado)"));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth ->
                auth.requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/professors").permitAll()
                        .anyRequest().authenticated()).httpBasic(Customizer.withDefaults())
                        .cors(Customizer.withDefaults());

        http.headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
