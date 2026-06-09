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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<Tag> tags = tagDAO.buscarTagsPorUsuario(user.getId());
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", e.getMessage() != null ? e.getMessage() : "Erro desconhecido"));
        }
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Tag tag, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            tag.setIdUser(user.getId());
            if (tagDAO.incluirTag(tag)) {
                return ResponseEntity.ok(tag);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", "Erro ao criar tag"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", e.getMessage() != null ? e.getMessage() : "Erro desconhecido"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable int id, @RequestBody Tag tag, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            Tag existente = tagDAO.buscarTag(id);
            if (existente == null || existente.getIdUser() != user.getId()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            tag.setId(id);
            tag.setIdUser(user.getId());
            if (tagDAO.alterarTag(tag)) {
                return ResponseEntity.ok(tag);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", "Erro ao atualizar tag"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", e.getMessage() != null ? e.getMessage() : "Erro desconhecido"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable int id, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            Tag existente = tagDAO.buscarTag(id);
            if (existente == null || existente.getIdUser() != user.getId()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            if (tagDAO.excluirTag(id)) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", "Erro ao deletar tag"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", e.getMessage() != null ? e.getMessage() : "Erro desconhecido"));
        }
    }
}
