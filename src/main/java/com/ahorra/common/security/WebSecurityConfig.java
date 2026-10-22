package com.ahorra.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    @Value("{app.cors.extra-origins:}")
    private List<String> extraOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
        return httpSecurity
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**"). permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/register-user"). permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/email/resend-verification"). permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/email/verify"). permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/keep-alive").permitAll()
                        .requestMatchers("/error").permitAll()

                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/configuration/**",
                                "/webjars/**",
                                "/api-docs/**"
                        ).permitAll()

                        .requestMatchers(maliciousProbesMatcher()).denyAll()
                        .anyRequest().authenticated())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .build();

    }

    private RequestMatcher maliciousProbesMatcher() {
        List<String> blockExtensions =  List.of(".php", ".asp", ".aspx", ".env", ".cgi", ".sh", ".bat");
        return request -> {
            String path = request.getRequestURI().toLowerCase();
            return blockExtensions.stream().anyMatch(path::endsWith);
        };
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = new ArrayList<>(List.of("http://localhost:*"));

        extraOrigins.stream().map(String::trim).filter(origin -> !origin.isEmpty()).forEach(origins::add);
        configuration.setAllowedOriginPatterns(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Content-Disposition", "X-Total-Count"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
