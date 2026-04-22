package com.checklist.demo.controller;

import com.checklist.demo.LogConclusao;
import com.checklist.demo.LogConclusaoDAO;
import com.checklist.demo.TarefaDAO;
import com.checklist.demo.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogConclusaoDAO logConclusaoDAO;

    private final TarefaDAO tarefaDAO;

    public LogController(LogConclusaoDAO logConclusaoDAO, TarefaDAO tarefaDAO) {
        this.logConclusaoDAO = logConclusaoDAO;
        this.tarefaDAO = tarefaDAO;
    }

    private Usuario getLoggedUser(HttpSession session) {
        return (Usuario) session.getAttribute("user");
    }

    @GetMapping
    public ResponseEntity<?> listarLogs(HttpSession session) {
        Usuario user = getLoggedUser(session);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            java.util.List<com.checklist.demo.Tarefa> tarefas = tarefaDAO.buscarTarefasPorUsuario(user.getId());
            java.util.Set<Integer> taskIds = tarefas.stream()
                .map(com.checklist.demo.Tarefa::getId)
                .collect(java.util.stream.Collectors.toSet());

            List<LogConclusao> logs = logConclusaoDAO.listarTodos().stream()
                .filter(l -> taskIds.contains(l.getIdTarefa()))
                .toList();
            
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
