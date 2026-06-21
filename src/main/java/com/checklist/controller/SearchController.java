package com.checklist.controller;

import com.checklist.model.Tarefa;
import com.checklist.model.Usuario;
import com.checklist.search.SearchManager;
import com.checklist.search.SearchManager.SearchAlgorithm;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchManager searchManager;

    public SearchController(SearchManager searchManager) {
        this.searchManager = searchManager;
    }

    @GetMapping
    public ResponseEntity<?> search(
            @RequestParam String pattern,
            @RequestParam String algorithm,
            HttpSession session) {
        
        Usuario user = (Usuario) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            SearchAlgorithm alg = "BM".equalsIgnoreCase(algorithm) || "BOYER_MOORE".equalsIgnoreCase(algorithm)
                    ? SearchAlgorithm.BOYER_MOORE
                    : SearchAlgorithm.KMP;

            List<Tarefa> matchingTarefas = searchManager.searchTarefas(pattern, alg);
            
            // Filtra as tarefas para retornar apenas as que pertencem ao usuário logado
            // (a menos que seja o administrador padrão)
            boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole()) || 
                    (user.getEmail() != null && user.getEmail().toLowerCase().equals("admin@checklist.com"));
            
            List<Tarefa> userTasks = matchingTarefas.stream()
                    .filter(t -> isAdmin || t.getIdUser() == user.getId())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(userTasks);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Erro interno no servidor"));
        }
    }
}
