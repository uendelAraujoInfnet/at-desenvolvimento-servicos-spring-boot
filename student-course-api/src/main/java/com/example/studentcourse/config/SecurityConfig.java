package com.example.studentcourse.config;

import com.example.studentcourse.repository.ProfessorRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // EntryPoint que retorna JSON e NÃO seta WWW-Authenticate (evita popup)
    @Bean
    public AuthenticationEntryPoint restAuthEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String json = "{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}";
            response.getWriter().write(json);
        };
    }

    // UserDetailsService que carrega professor por username (assume ProfessorRepository existe)
    @Bean
    public UserDetailsService userDetailsService(ProfessorRepository professorRepository) {
        return username -> professorRepository.findByUsername(username)
                .map(professor -> User.withUsername(professor.getUsername())
                        .password(professor.getPassword()) // senha em texto (NoOp)
                        .roles("PROFESSOR")
                        .build()).orElseThrow(() ->
                        new UsernameNotFoundException("Professor not found"));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationEntryPoint restAuthEntryPoint) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/professors").permitAll()
                        .anyRequest().authenticated()
                )
                // aplicar o entrypoint customizado ao httpBasic explicitamente
                .httpBasic(basic -> basic.authenticationEntryPoint(restAuthEntryPoint))
                .cors(Customizer.withDefaults());

        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }

    // PasswordEncoder NoOp (senha em plain) — DEV ONLY
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}


//package com.example.studentcourse.config;
//
//import com.example.studentcourse.model.Professor;
//import com.example.studentcourse.repository.ProfessorRepository;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.NoOpPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableMethodSecurity
//public class SecurityConfig {
//
//    @Bean
//    public UserDetailsService userDetailsService(ProfessorRepository professorRepository) {
//        return username -> professorRepository.findByUsername(username)
//                .map(professor -> User.withUsername(professor.getUsername())
//                        .password(professor.getPassword())
//                        .roles("PROFESSOR")
//                        .build()).orElseThrow(() ->
//                        new UsernameNotFoundException("Professor not found ( Professor não encontrado)"));
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth ->
//                auth.requestMatchers("/h2-console/**").permitAll()
//                        .requestMatchers("/api/professors").permitAll()
//                        .anyRequest().authenticated()).httpBasic(Customizer.withDefaults())
//                        .cors(Customizer.withDefaults());
//
//        http.headers(headers -> headers
//                .frameOptions(frame -> frame.sameOrigin()));
//        return http.build();
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return NoOpPasswordEncoder.getInstance();
//    }
//}
