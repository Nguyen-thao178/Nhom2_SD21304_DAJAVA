package vn.polycafe.auth;

import java.util.Map;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import vn.polycafe.common.ApiException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth=auth; }
    public record Login(@NotBlank @Size(max=60) String username,@NotBlank @Size(max=72) String password) {}
    @GetMapping("/csrf") Map<String,String> csrf(CsrfToken token) { return Map.of("token",token.getToken(),"headerName",token.getHeaderName()); }
    @PostMapping("/login") StaffUser login(@Valid @RequestBody Login body,HttpServletRequest req,HttpServletResponse res) {
        var user=auth.login(body.username().trim(),body.password());
        if(user==null) throw new ApiException(401,"Sai tài khoản, mật khẩu hoặc tài khoản đang bị khóa.");
        req.getSession(); req.changeSessionId(); req.getSession().setAttribute("staff",user);
        res.setHeader("Cache-Control","no-store"); return user;
    }
    @GetMapping("/me") StaffUser me(@AuthenticationPrincipal StaffUser user,HttpServletResponse res) { res.setHeader("Cache-Control","no-store");return user; }
    @PostMapping("/logout") Map<String,String> logout(@AuthenticationPrincipal StaffUser user,HttpServletRequest req) {
        auth.logout(user); var session=req.getSession(false); if(session!=null) session.invalidate(); return Map.of("message","Đã đăng xuất.");
    }
}
