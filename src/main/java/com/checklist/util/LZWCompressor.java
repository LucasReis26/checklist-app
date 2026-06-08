package com.checklist.util;

import com.checklist.persistence.Arquivo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação do algoritmo de compressão LZW.
 * Compressão baseada em dicionário de sequências de bytes.
 */
public class LZWCompressor {
    
    private static final int DICT_SIZE = 4096; // Tamanho máximo do dicionário
    private static final int CODE_SIZE = 12;   // Bits por código (2^12 = 4096)
    
    /**
     * Compacta uma lista de arquivos em um único arquivo usando LZW.
     * 
     * @param filePaths Lista de caminhos dos arquivos a serem compactados
     * @param outputPath Caminho do arquivo de saída
     * @throws IOException Se houver erro na leitura/escrita
     */
    public static void compressFiles(List<String> filePaths, String outputPath) throws IOException {
        // Empacota todos os arquivos
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        for (String path : filePaths) {
            java.io.File file = new java.io.File(path);
            if (!file.exists() || file.length() == 0) continue;
            
            byte[] data = Files.readAllBytes(Paths.get(path));
            dos.writeUTF(path);
            dos.writeLong(file.length());
            dos.write(data);
        }
        dos.close();
        
        byte[] input = baos.toByteArray();
        
        // Compacta usando LZW
        byte[] compressed = lzwCompress(input);
        
        Files.write(Paths.get(outputPath), compressed);
        
        System.out.println("Arquivo compactado gerado: " + outputPath);
        System.out.println("Tamanho original (todos arquivos): " + input.length + " bytes");
        System.out.println("Tamanho compactado (LZW): " + compressed.length + " bytes");
        double taxa = (1 - (double)compressed.length / input.length) * 100;
        System.out.printf("Taxa de compressão: %.2f%%\n", taxa);
    }
    
    /**
     * Algoritmo de compressão LZW.
     * 
     * @param input Dados de entrada
     * @return Dados compactados
     */
    private static byte[] lzwCompress(byte[] input) {
        if (input.length == 0) return new byte[0];
        
        // Inicializa dicionário com todos os bytes possíveis (0-255)
        Map<List<Byte>, Integer> dict = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dict.put(List.of((byte)i), i);
        }
        
        int nextCode = 256;
        List<Integer> output = new ArrayList<>();
        
        List<Byte> current = new ArrayList<>();
        current.add(input[0]);
        
        for (int i = 1; i < input.length; i++) {
            byte b = input[i];
            List<Byte> extended = new ArrayList<>(current);
            extended.add(b);
            
            if (dict.containsKey(extended)) {
                current = extended;
            } else {
                // Adiciona código da sequência atual à saída
                output.add(dict.get(current));
                
                // Adiciona nova sequência ao dicionário se ainda houver espaço
                if (nextCode < DICT_SIZE) {
                    dict.put(extended, nextCode++);
                }
                
                // Reinicia com o byte atual
                current = new ArrayList<>();
                current.add(b);
            }
        }
        
        // Adiciona o último código
        if (!current.isEmpty()) {
            output.add(dict.get(current));
        }
        
        // Converte códigos para bytes (usando CODE_SIZE bits por código)
        return encodeCodes(output);
    }
    
    /**
     * Codifica a lista de códigos em bytes.
     */
    private static byte[] encodeCodes(List<Integer> codes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        try {
            dos.writeInt(codes.size()); // Número de códigos
            
            int buffer = 0;
            int bitsInBuffer = 0;
            
            for (int code : codes) {
                buffer = (buffer << CODE_SIZE) | code;
                bitsInBuffer += CODE_SIZE;
                
                while (bitsInBuffer >= 8) {
                    bitsInBuffer -= 8;
                    byte outByte = (byte) ((buffer >> bitsInBuffer) & 0xFF);
                    dos.writeByte(outByte);
                }
            }
            
            // Escreve os bits restantes
            if (bitsInBuffer > 0) {
                byte outByte = (byte) ((buffer << (8 - bitsInBuffer)) & 0xFF);
                dos.writeByte(outByte);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Descompacta um arquivo compactado com LZW.
     * 
     * @param compressedFile Caminho do arquivo compactado
     * @param outputDir Diretório de saída
     * @throws IOException Se houver erro na leitura/escrita
     */
    public static void decompressFiles(String compressedFile, String outputDir) throws IOException {
        byte[] compressed = Files.readAllBytes(Paths.get(compressedFile));
        byte[] decompressed = lzwDecompress(compressed);
        
        // Restaura arquivos individuais
        ByteArrayInputStream bais = new ByteArrayInputStream(decompressed);
        DataInputStream dis = new DataInputStream(bais);
        
        java.io.File dir = new java.io.File(outputDir);
        if (!dir.exists()) dir.mkdirs();
        
        while (dis.available() > 0) {
            String filename = dis.readUTF();
            long fileSize = dis.readLong();
            byte[] data = new byte[(int) fileSize];
            dis.read(data);
            
            String outputPath = outputDir + "/" + new java.io.File(filename).getName();
            Files.write(Paths.get(outputPath), data);
            System.out.println("Restaurado: " + outputPath);
        }
        
        dis.close();
    }
    
    /**
     * Descompacta dados LZW.
     */
    private static byte[] lzwDecompress(byte[] compressed) throws IOException {
        // Decodifica códigos
        List<Integer> codes = decodeCodes(compressed);
        
        if (codes.isEmpty()) return new byte[0];
        
        // Reconstrói dicionário
        List<byte[]> dict = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            dict.add(new byte[]{(byte)i});
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        int firstCode = codes.get(0);
        byte[] firstBytes = dict.get(firstCode);
        baos.write(firstBytes);
        
        byte[] prev = firstBytes;
        
        for (int i = 1; i < codes.size(); i++) {
            int code = codes.get(i);
            byte[] entry;
            
            if (code < dict.size()) {
                entry = dict.get(code);
            } else if (code == dict.size()) {
                // Caso especial: código igual ao próximo a ser adicionado
                entry = new byte[prev.length + 1];
                System.arraycopy(prev, 0, entry, 0, prev.length);
                entry[prev.length] = prev[0];
            } else {
                throw new IOException("Código inválido: " + code);
            }
            
            baos.write(entry);
            
            // Adiciona nova entrada ao dicionário
            if (dict.size() < DICT_SIZE) {
                byte[] newEntry = new byte[prev.length + 1];
                System.arraycopy(prev, 0, newEntry, 0, prev.length);
                newEntry[prev.length] = entry[0];
                dict.add(newEntry);
            }
            
            prev = entry;
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Decodifica bytes de volta para códigos LZW.
     */
    private static List<Integer> decodeCodes(byte[] compressed) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        DataInputStream dis = new DataInputStream(bais);
        
        int numCodes = dis.readInt();
        List<Integer> codes = new ArrayList<>(numCodes);
        
        int buffer = 0;
        int bitsInBuffer = 0;
        int totalBitsRead = 0;
        
        while (totalBitsRead < numCodes * CODE_SIZE && dis.available() > 0) {
            while (bitsInBuffer < CODE_SIZE && dis.available() > 0) {
                buffer = (buffer << 8) | (dis.readByte() & 0xFF);
                bitsInBuffer += 8;
            }
            
            if (bitsInBuffer >= CODE_SIZE) {
                bitsInBuffer -= CODE_SIZE;
                int code = (buffer >> bitsInBuffer) & ((1 << CODE_SIZE) - 1);
                codes.add(code);
                totalBitsRead += CODE_SIZE;
            }
        }
        
        return codes;
    }
}
