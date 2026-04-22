package com.checklist.demo.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.checklist.demo.dao.TarefaTagDAO;
import com.checklist.demo.model.TarefaTag;
import com.checklist.demo.model.Usuario;

@RestController
@RequestMapping("/api/tarefa-tags")
public class TarefaTagController {

    private final TarefaTagDAO tarefaTagDAO;

    public TarefaTagController(TarefaTagDAO tarefaTagDAO) {
        this.tarefaTagDAO = tarefaTagDAO;
    }

    private Usuario getLoggedUser(HttpSession session) {
        return (Usuario) session.getAttribute("user");
    }

    @GetMapping
    public ResponseEntity<?> listarTodas(HttpSession session) {
        if (getLoggedUser(session) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            List<TarefaTag> lista = tarefaTagDAO.listarTodos();
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> criarRelacionamento(@RequestBody TarefaTag tt, HttpSession session) {
        if (getLoggedUser(session) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            if (tarefaTagDAO.incluirRelacionamento(tt)) {
                return ResponseEntity.status(HttpStatus.CREATED).body(tt);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar relacionamento");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
