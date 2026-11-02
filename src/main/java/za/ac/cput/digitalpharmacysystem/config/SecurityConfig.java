package za.ac.cput.digitalpharmacysystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/digitalpharmacysystem/auth/register",
                                "/digitalpharmacysystem/auth/login"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/digitalpharmacysystem/product/catalogue",
                                "/digitalpharmacysystem/product/rx",
                                "/digitalpharmacysystem/product/otc",
                                "/digitalpharmacysystem/category/getAll",
                                "/digitalpharmacysystem/category/read/**",
                                "/digitalpharmacysystem/category/search",
                                "/digitalpharmacysystem/category/dosageforms"
                        ).permitAll()

                        .requestMatchers(
                                "/digitalpharmacysystem/product/**",
                                "/digitalpharmacysystem/category/**"
                        ).hasAnyRole("ADMIN", "PHARMACIST")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}