package vn.polycafe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PolyCafeApplication extends org.springframework.boot.web.servlet.support.SpringBootServletInitializer {
    @Override protected org.springframework.boot.builder.SpringApplicationBuilder configure(org.springframework.boot.builder.SpringApplicationBuilder builder) {
        return builder.sources(PolyCafeApplication.class);
    }
    public static void main(String[] args) { SpringApplication.run(PolyCafeApplication.class, args); }
}
