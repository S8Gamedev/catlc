package com.majerpro.learning_platform.config;

import com.majerpro.learning_platform.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/", "/login", "/register",
                                "/css/**", "/js/**", "/images/**", "/webjars/**"
                        ).permitAll()
                        .requestMatchers("/api/users/register", "/api/users/login").permitAll()

                        // ADD THIS (temporary dev/testing)
                        .requestMatchers("/api/revision/**","api/**").permitAll()
                        // (optional) if you have analytics endpoints:
                        // .requestMatchers("/api/analytics/**").permitAll()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )

                // CHANGE THIS: ignore CSRF only for /api/**
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")); // keeps CSRF for web forms

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return identifier -> {
            var userOpt = userRepository.findByEmail(identifier);
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByUsername(identifier);
            }

            var user = userOpt.orElseThrow(() ->
                    new org.springframework.security.core.userdetails.UsernameNotFoundException(
                            "User not found: " + identifier
                    )
            );

            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword()) // don't encode again
                    .roles("USER")
                    .build();
        };
    }
}
