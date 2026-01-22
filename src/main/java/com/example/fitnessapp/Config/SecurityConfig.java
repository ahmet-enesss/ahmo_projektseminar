package com.example.fitnessapp.Config;

import com.example.fitnessapp.Security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CORS konfigurieren
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // CSRF deaktivieren für stateless Basic Auth
            .csrf(csrf -> csrf.disable())
            // Session-Management: stateless (keine Session-Cookies)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // HTTP Basic Authentication
            .httpBasic(httpBasic -> {})
            // Authorization-Regeln
            .authorizeHttpRequests(auth -> auth
                // Öffentliche Endpunkte
                .requestMatchers("/api/auth/validate").authenticated() // Validiert Basic Auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Alle anderen API-Endpunkte erfordern Authentifizierung
                .requestMatchers("/api/**").authenticated()
                // Alle anderen Requests erlauben (für statische Ressourcen, etc.)
                .anyRequest().permitAll()
            )
            // UserDetailsService setzen
            .userDetailsService(userDetailsService)
            // Exception Handling für 401/403
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentifizierung erforderlich\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"Zugriff verweigert\"}");
                })
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false); // Basic Auth ist stateless
        configuration.setExposedHeaders(List.of("WWW-Authenticate"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
