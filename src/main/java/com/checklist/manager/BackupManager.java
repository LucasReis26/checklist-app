package com.checklist.manager;

import com.checklist.persistence.Arquivo;
import com.checklist.util.HuffmanCompressor;
import com.checklist.util.LZWCompressor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gerenciador de backup do sistema.
 * Responsável por orquestrar a compressão de todos os arquivos de dados.
 */
public class BackupManager {
    
    private static final String DADOS_DIR = "./dados";
    private static final String BACKUP_DIR = "./backups";
    
    /**
     * Lista todos os arquivos de dados do sistema.
     * 
     * @return Lista de caminhos absolutos dos arquivos
     * @throws IOException Se houver erro ao listar arquivos
     */
    public static List<String> listDataFiles() throws IOException {
        File dir = new File(DADOS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
            return new ArrayList<>();
        }
        
        return Files.walk(Paths.get(DADOS_DIR))
            .filter(Files::isRegularFile)
            .map(Path::toString)
            .collect(Collectors.toList());
    }
    
    /**
     * Calcula o tamanho total de uma lista de arquivos.
     * 
     * @param filePaths Lista de caminhos dos arquivos
     * @return Tamanho total em bytes
     */
    public static long calculateTotalSize(List<String> filePaths) {
        return filePaths.stream()
            .mapToLong(path -> new File(path).length())
            .sum();
    }
    
    /**
     * Cria um backup compactado usando Huffman.
     * 
     * @return Mapa com estatísticas do backup
     * @throws IOException Se houver erro no processo
     */
    public static java.util.Map<String, Object> backupHuffman() throws IOException {
        // Cria diretório de backups se não existir
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) backupDir.mkdirs();
        
        // Gera nome do arquivo com timestamp
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String outputFile = BACKUP_DIR + "/backup_huffman_" + timestamp + ".huf";
        
        // Lista todos os arquivos de dados
        List<String> files = listDataFiles();
        
        if (files.isEmpty()) {
            return null;
        }
        
        long originalSize = calculateTotalSize(files);
        long startTime = System.nanoTime();
        
        // Executa compressão
        HuffmanCompressor.compressFiles(files, outputFile);
        
        long endTime = System.nanoTime();
        long compressedSize = new File(outputFile).length();
        double timeMs = (endTime - startTime) / 1_000_000.0;
        double compressionRatio = (1 - (double)compressedSize / originalSize) * 100;
        
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("filename", new File(outputFile).getName());
        stats.put("algorithm", "Huffman");
        stats.put("originalSize", originalSize);
        stats.put("compressedSize", compressedSize);
        stats.put("compressionRatio", compressionRatio);
        stats.put("executionTimeMs", timeMs);
        stats.put("fileCount", files.size());
        stats.put("timestamp", new java.util.Date().getTime());
        
        saveMetadata(outputFile, stats);
        
        return stats;
    }
    
    /**
     * Cria um backup compactado usando LZW.
     * 
     * @return Mapa com estatísticas do backup
     * @throws IOException Se houver erro no processo
     */
    public static java.util.Map<String, Object> backupLZW() throws IOException {
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) backupDir.mkdirs();
        
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String outputFile = BACKUP_DIR + "/backup_lzw_" + timestamp + ".lzw";
        
        List<String> files = listDataFiles();
        
        if (files.isEmpty()) {
            return null;
        }
        
        long originalSize = calculateTotalSize(files);
        long startTime = System.nanoTime();
        
        LZWCompressor.compressFiles(files, outputFile);
        
        long endTime = System.nanoTime();
        long compressedSize = new File(outputFile).length();
        double timeMs = (endTime - startTime) / 1_000_000.0;
        double compressionRatio = (1 - (double)compressedSize / originalSize) * 100;
        
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("filename", new File(outputFile).getName());
        stats.put("algorithm", "LZW");
        stats.put("originalSize", originalSize);
        stats.put("compressedSize", compressedSize);
        stats.put("compressionRatio", compressionRatio);
        stats.put("executionTimeMs", timeMs);
        stats.put("fileCount", files.size());
        stats.put("timestamp", new java.util.Date().getTime());
        
        saveMetadata(outputFile, stats);
        
        return stats;
    }

    private static void saveMetadata(String backupFile, java.util.Map<String, Object> stats) {
        try {
            String metaFile = backupFile + ".meta";
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            stats.forEach((k, v) -> {
                sb.append("  \"").append(k).append("\": ");
                if (v instanceof String) sb.append("\"").append(v).append("\"");
                else sb.append(v);
                sb.append(",\n");
            });
            if (sb.length() > 2) sb.setLength(sb.length() - 2);
            sb.append("\n}");
            Files.write(Paths.get(metaFile), sb.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static java.util.Map<String, Object> getMetadata(String backupName) {
        try {
            Path path = Paths.get(BACKUP_DIR, backupName + ".meta");
            if (!Files.exists(path)) return null;
            
            String content = new String(Files.readAllBytes(path));
            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            // Parser manual simplificado (evitando dependências extras para este caso específico)
            content = content.replace("{", "").replace("}", "").trim();
            String[] lines = content.split(",\n");
            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String key = parts[0].trim().replace("\"", "");
                    String val = parts[1].trim();
                    if (val.startsWith("\"")) {
                        stats.put(key, val.replace("\"", ""));
                    } else if (val.contains(".")) {
                        stats.put(key, Double.parseDouble(val));
                    } else {
                        stats.put(key, Long.parseLong(val));
                    }
                }
            }
            return stats;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Restaura um backup Huffman.
     * 
     * @param backupFile Caminho do arquivo de backup
     * @throws IOException Se houver erro no processo
     */
    public static void restoreHuffman(String backupFile) throws IOException {
        System.out.println("\n=== Restauração Huffman ===");
        System.out.println("Arquivo: " + backupFile);
        
        // No ambiente web, restauramos diretamente sobre a pasta de dados
        HuffmanCompressor.decompressFiles(backupFile, DADOS_DIR);
        
        System.out.println("Arquivos restaurados em: " + DADOS_DIR);
    }
    
    /**
     * Restaura um backup LZW.
     * 
     * @param backupFile Caminho do arquivo de backup
     * @throws IOException Se houver erro no processo
     */
    public static void restoreLZW(String backupFile) throws IOException {
        System.out.println("\n=== Restauração LZW ===");
        System.out.println("Arquivo: " + backupFile);
        
        LZWCompressor.decompressFiles(backupFile, DADOS_DIR);
        
        System.out.println("Arquivos restaurados em: " + DADOS_DIR);
    }
    
    /**
     * Lista todos os backups disponíveis.
     */
    public static void listBackups() {
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            System.out.println("Nenhum backup encontrado.");
            return;
        }
        
        File[] backups = backupDir.listFiles((dir, name) -> 
            name.endsWith(".huf") || name.endsWith(".lzw"));
        
        if (backups == null || backups.length == 0) {
            System.out.println("Nenhum backup encontrado.");
            return;
        }
        
        System.out.println("\n=== Backups Disponíveis ===");
        for (File f : backups) {
            System.out.printf("  %s - %,d bytes\n", f.getName(), f.length());
        }
    }
}
