package com.checklist.controller;

import com.checklist.dao.UsuarioDAO;
import com.checklist.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioDAO usuarioDAO;

    public AuthController(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        try {
            String email = credentials.get("email");
            String senha = credentials.get("senha");

            Usuario usuario = usuarioDAO.buscarUsuarioPorEmail(email);
            if (usuario != null && usuario.getSenha().equals(senha)) {
                session.setAttribute("user", usuario);
                return ResponseEntity.ok(usuario);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Email ou senha inválidos"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Erro interno"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario usuario, HttpSession session) {
        try {
            if (usuarioDAO.buscarUsuarioPorEmail(usuario.getEmail()) != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Email já cadastrado"));
            }
            if (usuarioDAO.incluirUsuario(usuario)) {
                // Removido o login automático por sessão para alinhar com o frontend
                return ResponseEntity.ok(usuario);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Erro ao cadastrar usuário"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Erro interno"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("user");
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/users")
    public ResponseEntity<?> listUsers(HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!user.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acesso negado: apenas administradores"));
        }

        try {
            java.util.List<Usuario> usuarios = usuarioDAO.listarTodos();
            java.util.List<Map<String, Object>> result = usuarios.stream().map(u -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", u.getId());
                map.put("nome", u.getNome());
                map.put("email", u.getEmail());
                map.put("role", u.getRole());
                map.put("senhaCriptografada", u.getSenhaEncrypted());
                return map;
            }).collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Erro desconhecido"));
        }
    }
}
