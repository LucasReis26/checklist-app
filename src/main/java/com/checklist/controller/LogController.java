package com.checklist.controller;

import com.checklist.dao.LogConclusaoDAO;
import com.checklist.dao.TarefaDAO;
import com.checklist.model.LogConclusao;
import com.checklist.model.Tarefa;
import com.checklist.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogConclusaoDAO logDAO;
    private final TarefaDAO tarefaDAO;

    public LogController(LogConclusaoDAO logDAO, TarefaDAO tarefaDAO) {
        this.logDAO = logDAO;
        this.tarefaDAO = tarefaDAO;
    }

    @GetMapping
    public ResponseEntity<?> listar(HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            List<Tarefa> tarefasUser = tarefaDAO.buscarTarefasPorUsuario(user.getId());
            Set<Integer> idsTarefas = tarefasUser.stream().map(Tarefa::getId).collect(Collectors.toSet());
            
            List<LogConclusao> todosLogs = logDAO.listarTodos();
            List<LogConclusao> logsUser = todosLogs.stream()
                    .filter(log -> idsTarefas.contains(log.getIdTarefa()))
                    .collect(Collectors.toList());
                    
            return ResponseEntity.ok(logsUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
