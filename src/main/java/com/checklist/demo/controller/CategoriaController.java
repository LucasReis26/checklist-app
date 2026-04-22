package com.checklist.demo.controller;

import com.checklist.demo.Categoria;
import com.checklist.demo.CategoriaDAO;
import com.checklist.demo.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaDAO categoriaDAO;

    public CategoriaController(CategoriaDAO categoriaDAO) {
        this.categoriaDAO = categoriaDAO;
    }

    private Usuario getLoggedUser(HttpSession session) {
        return (Usuario) session.getAttribute("user");
    }

    @GetMapping
    public ResponseEntity<?> listarCategorias(HttpSession session) {
        Usuario user = getLoggedUser(session);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            List<Categoria> categorias = categoriaDAO.buscarCategoriasPorUsuario(user.getId());
            return ResponseEntity.ok(categorias);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> criarCategoria(@RequestBody Categoria categoria, HttpSession session) {
        Usuario user = getLoggedUser(session);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            categoria.setIdUser(user.getId());
            if (categoriaDAO.incluirCategoria(categoria)) {
                return ResponseEntity.status(HttpStatus.CREATED).body(categoria);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar categoria");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirCategoria(@PathVariable int id, HttpSession session) {
        Usuario user = getLoggedUser(session);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            Categoria existente = categoriaDAO.buscarCategoria(id);
            if (existente == null || existente.getIdUser() != user.getId()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if (categoriaDAO.excluirCategoria(id)) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao excluir categoria");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
