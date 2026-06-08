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
     * @return Nome do arquivo de backup gerado
     * @throws IOException Se houver erro no processo
     */
    public static String backupHuffman() throws IOException {
        // Cria diretório de backups se não existir
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) backupDir.mkdirs();
        
        // Gera nome do arquivo com timestamp
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String outputFile = BACKUP_DIR + "/backup_huffman_" + timestamp + ".huf";
        
        // Lista todos os arquivos de dados
        List<String> files = listDataFiles();
        
        if (files.isEmpty()) {
            System.out.println("Nenhum arquivo de dados encontrado para backup.");
            return null;
        }
        
        long originalSize = calculateTotalSize(files);
        
        System.out.println("\n=== Backup Huffman ===");
        System.out.println("Arquivos a serem compactados: " + files.size());
        for (String f : files) {
            System.out.println("  - " + f);
        }
        System.out.println("Tamanho total original: " + originalSize + " bytes (" + 
                          String.format("%.2f", originalSize / 1024.0) + " KB)");
        
        long startTime = System.nanoTime();
        
        // Executa compressão
        HuffmanCompressor.compressFiles(files, outputFile);
        
        long endTime = System.nanoTime();
        long compressedSize = new File(outputFile).length();
        
        System.out.println("\n--- Resultados ---");
        System.out.println("Arquivo gerado: " + outputFile);
        System.out.println("Tamanho compactado: " + compressedSize + " bytes (" + 
                          String.format("%.2f", compressedSize / 1024.0) + " KB)");
        
        double taxa = (1 - (double)compressedSize / originalSize) * 100;
        System.out.printf("Taxa de compressão: %.2f%%\n", taxa);
        System.out.printf("Tempo de execução: %.3f ms\n", (endTime - startTime) / 1_000_000.0);
        
        return outputFile;
    }
    
    /**
     * Cria um backup compactado usando LZW.
     * 
     * @return Nome do arquivo de backup gerado
     * @throws IOException Se houver erro no processo
     */
    public static String backupLZW() throws IOException {
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) backupDir.mkdirs();
        
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String outputFile = BACKUP_DIR + "/backup_lzw_" + timestamp + ".lzw";
        
        List<String> files = listDataFiles();
        
        if (files.isEmpty()) {
            System.out.println("Nenhum arquivo de dados encontrado para backup.");
            return null;
        }
        
        long originalSize = calculateTotalSize(files);
        
        System.out.println("\n=== Backup LZW ===");
        System.out.println("Arquivos a serem compactados: " + files.size());
        for (String f : files) {
            System.out.println("  - " + f);
        }
        System.out.println("Tamanho total original: " + originalSize + " bytes (" + 
                          String.format("%.2f", originalSize / 1024.0) + " KB)");
        
        long startTime = System.nanoTime();
        
        LZWCompressor.compressFiles(files, outputFile);
        
        long endTime = System.nanoTime();
        long compressedSize = new File(outputFile).length();
        
        System.out.println("\n--- Resultados ---");
        System.out.println("Arquivo gerado: " + outputFile);
        System.out.println("Tamanho compactado: " + compressedSize + " bytes (" + 
                          String.format("%.2f", compressedSize / 1024.0) + " KB)");
        
        double taxa = (1 - (double)compressedSize / originalSize) * 100;
        System.out.printf("Taxa de compressão: %.2f%%\n", taxa);
        System.out.printf("Tempo de execução: %.3f ms\n", (endTime - startTime) / 1_000_000.0);
        
        return outputFile;
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
        
        String restoreDir = DADOS_DIR + "_restaurado_" + 
                           new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        
        HuffmanCompressor.decompressFiles(backupFile, restoreDir);
        
        System.out.println("Arquivos restaurados em: " + restoreDir);
        System.out.println("Para usar os dados restaurados, copie os arquivos para a pasta " + DADOS_DIR);
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
        
        String restoreDir = DADOS_DIR + "_restaurado_" + 
                           new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        
        LZWCompressor.decompressFiles(backupFile, restoreDir);
        
        System.out.println("Arquivos restaurados em: " + restoreDir);
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
