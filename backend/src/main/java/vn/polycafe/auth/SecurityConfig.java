package vn.polycafe.auth;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }
    @Bean SecurityFilterChain security(HttpSecurity http,UserRepository users) throws Exception {
        http.authorizeHttpRequests(a -> a.dispatcherTypeMatchers(jakarta.servlet.DispatcherType.FORWARD,jakarta.servlet.DispatcherType.ERROR).permitAll()
                .requestMatchers("/login","/assets/**","/api/auth/csrf","/api/auth/login","/error").permitAll().anyRequest().authenticated())
            .csrf(c -> c.csrfTokenRepository(new HttpSessionCsrfTokenRepository()).csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
            .headers(h -> h.contentSecurityPolicy(c -> c.policyDirectives("default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; object-src 'none'; base-uri 'self'; frame-ancestors 'none'; form-action 'self'")))
            .requestCache(c -> c.disable()).formLogin(c -> c.disable()).httpBasic(c -> c.disable()).logout(c -> c.disable())
            .addFilterBefore(new SessionFilter(users),AnonymousAuthenticationFilter.class)
            .exceptionHandling(e -> e.authenticationEntryPoint((req,res,ex) -> {
                if(req.getRequestURI().equals(req.getContextPath()+"/") || req.getRequestURI().startsWith(req.getContextPath()+"/app")) {res.sendRedirect(req.getContextPath()+"/login");return;}
                res.setStatus(401);res.setContentType("application/json;charset=UTF-8");res.getWriter().write("{\"message\":\"Vui lòng đăng nhập.\"}");})
                .accessDeniedHandler((req,res,ex) -> {res.setStatus(403);res.setContentType("application/json;charset=UTF-8");res.getWriter().write("{\"message\":\"Không có quyền hoặc phiên bảo mật hết hạn. Vui lòng tải lại.\"}");}));
        return http.build();
    }
}
