package za.ac.cput.digitalpharmacysystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String API = "/digitalpharmacysystem";
    private static final String PRESCRIPTIONS = API + "/prescription";
    private static final String API_PRESCRIPTIONS = "/api/prescriptions";

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
                        .requestMatchers("/", "/index/**", "/public/**",
                                API + "/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, API + "/product/**", API + "/category/**").permitAll()
                        .requestMatchers(API + "/product/**").hasAnyRole("PHARMACIST", "ADMIN")
                        .requestMatchers(API + "/category/**").hasRole("ADMIN")

                        .requestMatchers(API + "/user/names")
                                .hasAnyRole("PHARMACIST", "ADMIN")
                        .requestMatchers(HttpMethod.GET, API + "/user/getAll",
                                API + "/user/getall").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, API + "/user/create", API + "/user/staff")
                                .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, API + "/user/*/active").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, API + "/user/delete/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, API + "/user/read/*").authenticated()
                        .requestMatchers(HttpMethod.POST, API + "/user/update").authenticated()

                        .requestMatchers(HttpMethod.POST, API + "/order/create").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, API + "/order/getall")
                                .hasAnyRole("PHARMACIST", "ADMIN")
                        .requestMatchers(HttpMethod.GET, API + "/order/read/*",
                                API + "/order/customer/*").authenticated()
                        .requestMatchers(HttpMethod.POST, API + "/order/*/pay",
                                API + "/order/*/cancel").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, API + "/order/*/pack")
                                .hasAnyRole("PHARMACIST", "ADMIN")
                        .requestMatchers(HttpMethod.POST, API + "/order/*/collect")
                                .hasAnyRole("PHARMACIST", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, API + "/order/delete/*").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, PRESCRIPTIONS + "/upload",
                                API_PRESCRIPTIONS + "/upload").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, PRESCRIPTIONS + "/mine",
                                API_PRESCRIPTIONS + "/mine",
                                PRESCRIPTIONS + "/*/file",
                                API_PRESCRIPTIONS + "/*/file").authenticated()
                        .requestMatchers(HttpMethod.GET, PRESCRIPTIONS + "/pending",
                                API_PRESCRIPTIONS + "/pending",
                                PRESCRIPTIONS, API_PRESCRIPTIONS)
                                .hasAnyRole("PHARMACIST", "ADMIN")
                        .requestMatchers(HttpMethod.GET, PRESCRIPTIONS + "/patient/*",
                                API_PRESCRIPTIONS + "/patient/*",
                                PRESCRIPTIONS + "/*", API_PRESCRIPTIONS + "/*").authenticated()
                        .requestMatchers(HttpMethod.POST, PRESCRIPTIONS, API_PRESCRIPTIONS)
                                .hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, PRESCRIPTIONS + "/*/approve",
                                PRESCRIPTIONS + "/*/reject", PRESCRIPTIONS + "/*/clarify",
                                API_PRESCRIPTIONS + "/*/approve",
                                API_PRESCRIPTIONS + "/*/reject",
                                API_PRESCRIPTIONS + "/*/clarify")
                                .hasRole("PHARMACIST")
                        .requestMatchers(HttpMethod.PUT, PRESCRIPTIONS, API_PRESCRIPTIONS)
                                .hasAnyRole("PHARMACIST", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, PRESCRIPTIONS + "/*",
                                API_PRESCRIPTIONS + "/*").hasRole("ADMIN")

                        .requestMatchers(API + "/patientprofile/getAll").hasRole("ADMIN")
                        .requestMatchers(API + "/patientprofile/**").hasAnyRole("CUSTOMER", "ADMIN")

                        .requestMatchers(HttpMethod.GET, API + "/refills")
                                .hasAnyRole("PHARMACIST", "ADMIN")
                        .requestMatchers(HttpMethod.GET, API + "/refills/*")
                                .hasAnyRole("PHARMACIST", "ADMIN", "CUSTOMER")
                        .requestMatchers(HttpMethod.GET, API + "/refills/prescription/*",
                                API + "/refills/eligible/*").hasAnyRole("PHARMACIST", "ADMIN", "CUSTOMER")
                        .requestMatchers(HttpMethod.POST, API + "/refills/process/*")
                                .hasAnyRole("PHARMACIST", "ADMIN", "CUSTOMER")
                        .requestMatchers(HttpMethod.POST, API + "/refills",
                                API + "/refills/").hasAnyRole("PHARMACIST", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, API + "/refills",
                                API + "/refills/").hasAnyRole("PHARMACIST", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, API + "/refills/*").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, API + "/delivery/getall")
                                .hasAnyRole("PHARMACIST", "ADMIN")
                        .requestMatchers(HttpMethod.GET, API + "/delivery/courier/*/manifest")
                                .hasAnyRole("COURIER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, API + "/delivery/order/*").authenticated()
                        .requestMatchers(HttpMethod.GET, API + "/delivery/read/*")
                                .hasAnyRole("COURIER", "PHARMACIST", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, API + "/delivery/*/status")
                                .hasAnyRole("COURIER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, API + "/delivery/*/courier/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, API + "/delivery/create",
                                API + "/delivery/update").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, API + "/delivery/delete/*").hasRole("ADMIN")

                        .requestMatchers(API + "/auditlog/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
