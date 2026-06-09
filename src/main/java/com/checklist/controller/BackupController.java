package com.checklist.controller;

import com.checklist.manager.BackupManager;
import com.checklist.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    @PostMapping("/huffman")
    public ResponseEntity<?> backupHuffman(HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!user.isAdmin()) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acesso negado: apenas administradores"));

        try {
            Map<String, Object> stats = BackupManager.backupHuffman();
            return ResponseEntity.ok(Map.of("message", "Backup Huffman realizado com sucesso", "stats", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Erro desconhecido"));
        }
    }

    @PostMapping("/lzw")
    public ResponseEntity<?> backupLZW(HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!user.isAdmin()) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acesso negado: apenas administradores"));

        try {
            Map<String, Object> stats = BackupManager.backupLZW();
            return ResponseEntity.ok(Map.of("message", "Backup LZW realizado com sucesso", "stats", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Erro desconhecido"));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getBackupStats(@RequestParam String name, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!user.isAdmin()) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acesso negado"));

        Map<String, Object> stats = BackupManager.getMetadata(name);
        if (stats == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/restore")
    public ResponseEntity<?> restoreBackup(@RequestParam String name, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!user.isAdmin()) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acesso negado"));

        try {
            String path = "./backups/" + name;
            File file = new File(path);
            if (!file.exists()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Arquivo não encontrado"));

            if (name.endsWith(".huf")) {
                BackupManager.restoreHuffman(path);
            } else if (name.endsWith(".lzw")) {
                BackupManager.restoreLZW(path);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Formato inválido"));
            }

            return ResponseEntity.ok(Map.of("message", "Restauração concluída. Os arquivos foram extraídos."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Erro na restauração"));
        }
    }
    @GetMapping("/list")
    public ResponseEntity<?> listBackups(HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!user.isAdmin()) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acesso negado: apenas administradores"));

        try {
            File backupDir = new File("./backups");
            if (!backupDir.exists()) return ResponseEntity.ok(List.of());

            File[] backups = backupDir.listFiles((dir, name) ->
                    name.endsWith(".huf") || name.endsWith(".lzw"));

            List<Map<String, Object>> result = new ArrayList<>();
            if (backups != null) {
                for (File f : backups) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", f.getName());
                    map.put("size", f.length());
                    map.put("lastModified", f.lastModified());
                    result.add(map);
                }
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Erro desconhecido"));
        }
    }
}
