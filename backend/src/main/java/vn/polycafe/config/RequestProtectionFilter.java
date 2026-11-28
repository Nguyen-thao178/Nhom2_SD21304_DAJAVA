package vn.polycafe.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

/** Bounded body and per-IP login throttle, including usernames that do not exist. */
@Component
@Order(-110)
public class RequestProtectionFilter extends OncePerRequestFilter {
    private static final int MAX_BODY=1048576;
    private final ConcurrentHashMap<String,Window> windows=new ConcurrentHashMap<>();
    private static class Window {
        final long start=System.nanoTime();
        int count;
        synchronized boolean allow() { return ++count<=15; }
    }
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)
            throws IOException,ServletException {
        response.setHeader("Cache-Control","no-store");
        String path=request.getRequestURI().substring(request.getContextPath().length());
        if("/api/v1/auth/login".equals(path) || (request.getMethod().equals("POST") && path.startsWith("/api/qr/"))) {
            long now=System.nanoTime();
            // Do not trust X-Forwarded-For supplied by a public client.
            String ip=request.getRemoteAddr();
            if(windows.size()>=10000) windows.entrySet().removeIf(e -> now-e.getValue().start>60_000_000_000L);
            if(windows.size()>=10000 && !windows.containsKey(ip)) { reject(response,429);return; }
            Window window=windows.compute(ip,(key,old) -> old==null || now-old.start>60_000_000_000L ? new Window() : old);
            if(!window.allow()) { response.setHeader("Retry-After","60"); reject(response,429);return; }
        }
        if(java.util.Set.of("POST","PUT","PATCH").contains(request.getMethod())) {
            if(request.getContentLengthLong()>MAX_BODY) {reject(response,413);return;}
            byte[] bytes=request.getInputStream().readNBytes(MAX_BODY+1);
            if(bytes.length>MAX_BODY) {reject(response,413);return;}
            chain.doFilter(new HttpServletRequestWrapper(request) {
                @Override public ServletInputStream getInputStream() {
                    ByteArrayInputStream stream=new ByteArrayInputStream(bytes);
                    return new ServletInputStream() {
                        @Override public int read() {return stream.read();}
                        @Override public int read(byte[] b,int off,int len) {return stream.read(b,off,len);}
                        @Override public boolean isFinished() {return stream.available()==0;}
                        @Override public boolean isReady() {return true;}
                        @Override public void setReadListener(ReadListener listener) {throw new UnsupportedOperationException("Synchronous JSON API");}
                    };
                }
                @Override public BufferedReader getReader() {return new BufferedReader(new InputStreamReader(getInputStream(),StandardCharsets.UTF_8));}
            },response);
            return;
        }
        chain.doFilter(request,response);
    }
    private void reject(HttpServletResponse res,int status) throws IOException {
        res.setStatus(status);res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"message\":\"Yêu cầu vượt giới hạn cho phép.\"}");
    }
}
