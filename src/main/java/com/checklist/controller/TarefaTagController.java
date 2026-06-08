package com.checklist.controller;

import com.checklist.dao.TarefaDAO;
import com.checklist.dao.TarefaTagDAO;
import com.checklist.model.Tarefa;
import com.checklist.model.TarefaTag;
import com.checklist.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tarefa-tags")
public class TarefaTagController {

    private final TarefaTagDAO tarefaTagDAO;
    private final TarefaDAO tarefaDAO;

    public TarefaTagController(TarefaTagDAO tarefaTagDAO, TarefaDAO tarefaDAO) {
        this.tarefaTagDAO = tarefaTagDAO;
        this.tarefaDAO = tarefaDAO;
    }

    @GetMapping
    public ResponseEntity<?> listar(HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            List<Tarefa> tarefasUser = tarefaDAO.buscarTarefasPorUsuario(user.getId());
            Set<Integer> idsTarefas = tarefasUser.stream().map(Tarefa::getId).collect(Collectors.toSet());

            List<TarefaTag> todas = tarefaTagDAO.listarTodos();
            List<TarefaTag> tagsUser = todas.stream()
                    .filter(tt -> idsTarefas.contains(tt.getIdTarefa()))
                    .collect(Collectors.toList());
                    
            return ResponseEntity.ok(tagsUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", e.getMessage() != null ? e.getMessage() : "Erro desconhecido"));
        }
    }

    @PostMapping
    public ResponseEntity<?> associar(@RequestBody TarefaTag tarefaTag, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            Tarefa tarefa = tarefaDAO.buscarTarefa(tarefaTag.getIdTarefa());
            if (tarefa == null || tarefa.getIdUser() != user.getId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("error", "Tarefa não pertence ao usuário"));
            }

            if (tarefaTagDAO.incluirRelacionamento(tarefaTag)) {
                return ResponseEntity.ok(tarefaTag);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", "Erro ao associar tag"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", e.getMessage() != null ? e.getMessage() : "Erro desconhecido"));
        }
    }
}
