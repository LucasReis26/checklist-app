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

        try {
            String path = BackupManager.backupHuffman();
            return ResponseEntity.ok(Map.of("message", "Backup Huffman realizado com sucesso", "file", path));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/lzw")
    public ResponseEntity<?> backupLZW(HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            String path = BackupManager.backupLZW();
            return ResponseEntity.ok(Map.of("message", "Backup LZW realizado com sucesso", "file", path));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listBackups(HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
