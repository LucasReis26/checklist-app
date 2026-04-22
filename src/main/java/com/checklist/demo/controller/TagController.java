package com.checklist.demo.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.checklist.demo.dao.TagDAO;
import com.checklist.demo.model.Tag;
import com.checklist.demo.model.Usuario;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagDAO tagDAO;

    public TagController(TagDAO tagDAO) {
        this.tagDAO = tagDAO;
    }

    private Usuario getLoggedUser(HttpSession session) {
        return (Usuario) session.getAttribute("user");
    }

    @GetMapping
    public ResponseEntity<?> listarTags(HttpSession session) {
        if (getLoggedUser(session) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            List<Tag> tags = tagDAO.listarTodas();
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> criarTag(@RequestBody Tag tag, HttpSession session) {
        if (getLoggedUser(session) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            if (tagDAO.incluirTag(tag)) {
                return ResponseEntity.status(HttpStatus.CREATED).body(tag);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar tag");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirTag(@PathVariable int id, HttpSession session) {
        if (getLoggedUser(session) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            if (tagDAO.excluirTag(id)) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao excluir tag");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
