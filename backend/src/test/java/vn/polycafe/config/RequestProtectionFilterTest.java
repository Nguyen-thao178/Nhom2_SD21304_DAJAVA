package vn.polycafe.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class RequestProtectionFilterTest {
    @Test
    void throttlesNonexistentUsernamesBeforeDatabase() throws Exception {
        var filter = new RequestProtectionFilter();
        var calls = new AtomicInteger();
        for (int i = 0; i < 16; i++) {
            var request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            request.setRemoteAddr("192.0.2.1");
            request.setContent("{}".getBytes());
            var response = new MockHttpServletResponse();
            filter.doFilter(request, response, (a, b) -> calls.incrementAndGet());
            if (i == 15)
                assertEquals(429, response.getStatus());
        }
        assertEquals(15, calls.get());
    }

    @Test
    void throttlesLoginWithContextPath() throws Exception {
        var filter = new RequestProtectionFilter();
        var calls = new AtomicInteger();
        for (int i = 0; i < 16; i++) {
            var request = new MockHttpServletRequest("POST", "/polycafe/api/v1/auth/login");
            request.setContextPath("/polycafe");
            request.setRemoteAddr("192.0.2.2");
            request.setContent("{}".getBytes());
            var response = new MockHttpServletResponse();
            filter.doFilter(request, response, (a, b) -> calls.incrementAndGet());
            if (i == 15)
                assertEquals(429, response.getStatus());
        }
        assertEquals(15, calls.get());
    }

    @Test
    void rejectsOversizedBodyAndPreservesValidBody() throws Exception {
        var filter = new RequestProtectionFilter();
        var request = new MockHttpServletRequest("POST", "/api/v1/branch");
        request.setContent(new byte[1048577]);
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (a, b) -> fail("oversized body reached application"));
        assertEquals(413, response.getStatus());
        var small = new MockHttpServletRequest("POST", "/api/v1/branch");
        small.setContent("{\"name\":\"Cafe\"}".getBytes());
        filter.doFilter(small, new MockHttpServletResponse(),
                (a, b) -> assertEquals("{\"name\":\"Cafe\"}", new String(a.getInputStream().readAllBytes())));
    }
}
