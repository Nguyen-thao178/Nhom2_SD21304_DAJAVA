package vn.polycafe.auth;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class SessionFilter extends OncePerRequestFilter {
    private final UserRepository users;
    public SessionFilter(UserRepository users) { this.users=users; }
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
        var session=req.getSession(false);
        if(session!=null && session.getAttribute("staff") instanceof StaffUser saved) {
            var account=users.byId(saved.id());
            if(account.isPresent() && users.allowed(account.get()) && account.get().version()==saved.refreshVersion()) {
                var fresh=users.staff(account.get());
                if(fresh.roles().stream().anyMatch(java.util.Set.of("staff","shift_lead","owner","sysadmin","accountant")::contains)) {
                    var auth=new UsernamePasswordAuthenticationToken(fresh,null,List.of(new SimpleGrantedAuthority("ROLE_STAFF")));
                    var context=SecurityContextHolder.createEmptyContext(); context.setAuthentication(auth); SecurityContextHolder.setContext(context);
                } else session.invalidate();
            } else session.invalidate();
        }
        chain.doFilter(req,res);
    }
}
