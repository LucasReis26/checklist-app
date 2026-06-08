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
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            // As tags parecem ser globais ou não vinculadas a usuário no DAO
            // Vou retornar todas por enquanto ou ver se Tag tem id_user
            List<Tag> tags = tagDAO.listarTodas();
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar tag");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
