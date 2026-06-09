package com.checklist.controller;

import com.checklist.dao.TagDAO;
import com.checklist.model.Tag;
import com.checklist.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagDAO tagDAO;

    public TagController(TagDAO tagDAO) {
        this.tagDAO = tagDAO;
    }

    @GetMapping
    public ResponseEntity<?> listar(HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("user");
        if (user == null) {
            System.err.println("DEBUG: [TagController] Não autorizado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            System.out.println("DEBUG: [TagController] Listando todas as tags");
            List<Tag> tags = tagDAO.listarTodas();
            System.out.println("DEBUG: [TagController] Tags retornadas: " + (tags != null ? tags.size() : 0));
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            System.err.println("DEBUG: [TagController] Erro: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", e.getMessage() != null ? e.getMessage() : "Erro desconhecido"));
        }
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Tag tag, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            if (tagDAO.incluirTag(tag)) {
                return ResponseEntity.ok(tag);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", "Erro ao criar tag"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", e.getMessage() != null ? e.getMessage() : "Erro desconhecido"));
        }
    }
}
