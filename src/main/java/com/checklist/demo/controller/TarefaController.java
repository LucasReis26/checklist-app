package com.checklist.demo.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.checklist.demo.dao.TarefaDAO;
import com.checklist.demo.model.Tarefa;
import com.checklist.demo.model.Usuario;

@RestController
@RequestMapping("/api/tarefas")
public class TarefaController {

    private final TarefaDAO tarefaDAO;

    public TarefaController(TarefaDAO tarefaDAO) {
        this.tarefaDAO = tarefaDAO;
    }

    private Usuario getLoggedUser(HttpSession session) {
        return (Usuario) session.getAttribute("user");
    }

    @GetMapping
    public ResponseEntity<?> listarTarefas(HttpSession session) {
        Usuario user = getLoggedUser(session);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            List<Tarefa> tarefas = tarefaDAO.buscarTarefasPorUsuario(user.getId());
            return ResponseEntity.ok(tarefas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> criarTarefa(@RequestBody Tarefa tarefa, HttpSession session) {
        Usuario user = getLoggedUser(session);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            tarefa.setIdUser(user.getId());
            if (tarefaDAO.incluirTarefa(tarefa)) {
                return ResponseEntity.status(HttpStatus.CREATED).body(tarefa);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar tarefa");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarTarefa(@PathVariable int id, @RequestBody Tarefa tarefa, HttpSession session) {
        Usuario user = getLoggedUser(session);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            Tarefa existente = tarefaDAO.buscarTarefa(id);
            if (existente == null || existente.getIdUser() != user.getId()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Se o status mudou para concluida, usar o método específico
            if ("concluida".equals(tarefa.getStatus()) && !"concluida".equals(existente.getStatus())) {
                if (tarefaDAO.concluirTarefa(id, "")) { // TODO: extrair resumo das tags se necessário
                    return ResponseEntity.ok(tarefaDAO.buscarTarefa(id));
                }
            } else {
                tarefa.setId(id);
                tarefa.setIdUser(user.getId());
                if (tarefaDAO.alterarTarefa(tarefa)) {
                    return ResponseEntity.ok(tarefa);
                }
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao atualizar tarefa");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirTarefa(@PathVariable int id, HttpSession session) {
        Usuario user = getLoggedUser(session);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            Tarefa existente = tarefaDAO.buscarTarefa(id);
            if (existente == null || existente.getIdUser() != user.getId()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if (tarefaDAO.excluirTarefa(id)) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao excluir tarefa");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
