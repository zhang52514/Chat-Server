package cn.anoxia.chat.controller.auth;

import cn.anoxia.chat.common.domain.LoginRequest;
import cn.anoxia.chat.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        authService.logout(token);
        return ResponseEntity.ok("已登出");
    }

    @GetMapping("/protected")
    public ResponseEntity<?> testProtectedEndpoint() {
        return ResponseEntity.ok("你访问了受保护资源");
    }
}

