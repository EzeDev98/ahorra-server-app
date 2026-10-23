package com.ahorra.common.security;

import com.ahorra.features.auth.service.impl.TokenService;
import com.ahorra.features.session.repository.UserSessionRepository;
import com.ahorra.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
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

    private final CustomAccessDenied customAccessDenied;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;

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
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh-token"). permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login"). permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/password-reset/request"). permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/password-reset/verify-otp"). permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/password-reset/complete"). permitAll()
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
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDenied))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterAfter(new ActiveSessionFilter(userRepository, userSessionRepository), BearerTokenAuthenticationFilter.class)
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

    @Bean
    public JwtDecoder jwtEncoder(TokenService tokenService) {
        return tokenService.accessTokenDecoder();
    }
}
