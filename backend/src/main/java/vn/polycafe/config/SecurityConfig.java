package vn.polycafe.config;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.*;
import vn.polycafe.auth.SessionFilter;
import vn.polycafe.auth.UserRepository;

@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}") String allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "X-CSRF-TOKEN", "X-Expected-Updated-At", "Accept"));
        config.setExposedHeaders(List.of("Cache-Control"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean SecurityFilterChain security(HttpSecurity http, UserRepository users) throws Exception {
        http.cors(Customizer.withDefaults())
            .authorizeHttpRequests(a -> a.dispatcherTypeMatchers(jakarta.servlet.DispatcherType.FORWARD, jakarta.servlet.DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                .requestMatchers("/login", "/customer", "/qr/*", "/assets/**", "/api/v1/auth/csrf", "/api/v1/auth/login", "/error").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/qr/*/menu").permitAll()
                .requestMatchers("/api/v1/auth/me","/api/v1/auth/logout").authenticated()
                .requestMatchers("/api/customer/**","/api/qr/*/orders").hasRole("CUSTOMER")
                .anyRequest().hasRole("STAFF"))
            .csrf(c -> c.csrfTokenRepository(new HttpSessionCsrfTokenRepository()).csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
            .headers(h -> h.contentSecurityPolicy(c -> c.policyDirectives("default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; object-src 'none'; base-uri 'self'; frame-ancestors 'none'; form-action 'self'")))
            .requestCache(c -> c.disable()).formLogin(c -> c.disable()).httpBasic(c -> c.disable()).logout(c -> c.disable())
            .addFilterBefore(new SessionFilter(users), AnonymousAuthenticationFilter.class)
            .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> {
                if (req.getRequestURI().equals(req.getContextPath() + "/") || req.getRequestURI().startsWith(req.getContextPath() + "/app")) {
                    res.sendRedirect(req.getContextPath() + "/login");
                    return;
                }
                res.setStatus(401);
                res.setContentType("application/json;charset=UTF-8");
                res.getWriter().write("{\"message\":\"Vui lòng đăng nhập.\"}");
            })
            .accessDeniedHandler((req, res, ex) -> {
                res.setStatus(403);
                res.setContentType("application/json;charset=UTF-8");
                res.getWriter().write("{\"message\":\"Không có quyền hoặc phiên bảo mật hết hạn. Vui lòng tải lại.\"}");
            }));
        return http.build();
    }
}
