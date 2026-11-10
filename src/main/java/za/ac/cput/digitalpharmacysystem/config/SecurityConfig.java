package za.ac.cput.digitalpharmacysystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/auth/**", "/index/**", "/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products/**", "/categories/**", "/product/**").permitAll()

                        // Customer / Patient endpoints
                        .requestMatchers(HttpMethod.POST, "/orders/place", "/prescription/upload").hasAnyRole("CUSTOMER", "PATIENT")
                        .requestMatchers("/patient-profile/**").hasAnyRole("CUSTOMER", "PATIENT", "ADMIN")

                        // Pharmacist endpoints
                        .requestMatchers("/prescription/{id}/approve", "/prescription/{id}/reject", "/prescription/{id}/clarify").hasRole("PHARMACIST")
                        .requestMatchers("/orders/{id}/pack").hasAnyRole("PHARMACIST", "ADMIN")

                        // Courier endpoints
                        .requestMatchers("/delivery/courier/**", "/delivery/{id}/status").hasRole("COURIER")

                        // Admin endpoints
                        .requestMatchers("/user/create", "/user/all", "/user/{id}/delete").hasRole("ADMIN")
                        .requestMatchers("/audit-log/**").hasRole("ADMIN")

                        // Any other request requires authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}