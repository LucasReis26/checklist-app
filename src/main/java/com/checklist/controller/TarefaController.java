package com.checklist.controller;

import com.checklist.dao.TarefaDAO;
import com.checklist.model.Tarefa;
import com.checklist.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tarefas")
public class TarefaController {

    private final TarefaDAO tarefaDAO;

    public TarefaController(TarefaDAO tarefaDAO) {
        this.tarefaDAO = tarefaDAO;
    }

    @GetMapping
    public ResponseEntity<?> listar(HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<Tarefa> tarefas = tarefaDAO.buscarTarefasPorUsuario(user.getId());
            return ResponseEntity.ok(tarefas);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", e.getMessage() != null ? e.getMessage() : "Erro desconhecido"));
        }
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Tarefa tarefa, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            tarefa.setIdUser(user.getId());
            if (tarefa.getDataCriacao() == null || tarefa.getDataCriacao().isEmpty()) {
                tarefa.setDataCriacao(LocalDate.now().toString());
            }
            if (tarefa.getStatus() == null || tarefa.getStatus().isEmpty()) {
                tarefa.setStatus("pendente");
            }
            if (tarefaDAO.incluirTarefa(tarefa)) {
                return ResponseEntity.ok(tarefa);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", "Erro ao criar tarefa"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", e.getMessage() != null ? e.getMessage() : "Erro desconhecido"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable int id, @RequestBody Tarefa tarefa, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            Tarefa existente = tarefaDAO.buscarTarefa(id);
            if (existente == null || existente.getIdUser() != user.getId()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Se o status mudou para concluída, podemos usar concluirTarefa para gerar o log
            if ("concluida".equals(tarefa.getStatus()) && !"concluida".equals(existente.getStatus())) {
                if (tarefaDAO.concluirTarefa(id, "")) {
                    return ResponseEntity.ok(tarefaDAO.buscarTarefa(id));
                }
            }

            tarefa.setId(id);
            tarefa.setIdUser(user.getId());
            if (tarefaDAO.alterarTarefa(tarefa)) {
                return ResponseEntity.ok(tarefa);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", "Erro ao atualizar tarefa"));
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
            Tarefa existente = tarefaDAO.buscarTarefa(id);
            if (existente == null || existente.getIdUser() != user.getId()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            if (tarefaDAO.excluirTarefa(id)) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", "Erro ao deletar tarefa"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", e.getMessage() != null ? e.getMessage() : "Erro desconhecido"));
        }
    }
}
