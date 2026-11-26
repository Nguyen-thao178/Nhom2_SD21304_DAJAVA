package vn.polycafe.web;

import java.nio.charset.StandardCharsets;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.CharacterEncodingFilter;

/** Applies UTF-8 before security, parameter parsing and JSP rendering. */
@Configuration(proxyBeanMethods = false)
public class Utf8WebConfiguration {
    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        return new CharacterEncodingFilter(StandardCharsets.UTF_8.name(), true, true);
    }

    @Bean
    public FilterRegistrationBean<CharacterEncodingFilter> utf8FilterRegistration(
            CharacterEncodingFilter characterEncodingFilter) {
        var registration = new FilterRegistrationBean<>(characterEncodingFilter);
        registration.setName("polycafeUtf8Encoding");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }
}