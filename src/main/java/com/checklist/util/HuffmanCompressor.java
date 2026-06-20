package com.checklist.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Implementação do algoritmo de compressão Huffman.
 * Correção:
 * Compressão a nível de arquivo - cada arquivo é comprimido individualmente.
 */
public class HuffmanCompressor {
    
    static class Node implements Comparable<Node> {
        byte data;
        int freq;
        Node left, right;
        
        Node(byte data, int freq) {
            this.data = data;
            this.freq = freq;
        }
        
        Node(int freq, Node left, Node right) {
            this.freq = freq;
            this.left = left;
            this.right = right;
        }
        
        boolean isLeaf() {
            return left == null && right == null;
        }
        
        @Override
        public int compareTo(Node o) {
            return Integer.compare(this.freq, o.freq);
        }
    }
    
    /**
     * Compacta uma lista de arquivos em um único arquivo usando Huffman.
     * CADA arquivo é comprimido INDIVIDUALMENTE antes de ser empacotado.
     * 
     * @param filePaths Lista de caminhos dos arquivos a serem compactados
     * @param outputPath Caminho do arquivo de saída
     * @throws IOException Se houver erro na leitura/escrita
     */
    public static void compressFiles(List<String> filePaths, String outputPath) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // Escreve o número de arquivos
        dos.writeInt(filePaths.size());
        
        long totalOriginalSize = 0;
        long totalCompressedSize = 0;
        
        for (String path : filePaths) {
            File file = new File(path);
            if (!file.exists() || file.length() == 0) continue;
            
            byte[] data = Files.readAllBytes(file.toPath());
            totalOriginalSize += data.length;
            
            // COMPRIME CADA ARQUIVO INDIVIDUALMENTE
            byte[] compressedData = huffmanCompress(data);
            totalCompressedSize += compressedData.length;
            
            // Escreve o arquivo comprimido no pacote
            dos.writeUTF(path);                    // Nome do arquivo
            dos.writeLong(file.length());          // Tamanho original
            dos.writeInt(compressedData.length);   // Tamanho comprimido
            dos.write(compressedData);             // Dados comprimidos
        }
        dos.close();
        
        // Escreve o arquivo final
        Files.write(Paths.get(outputPath), baos.toByteArray());
        
        System.out.println("Arquivo compactado gerado: " + outputPath);
        System.out.println("Tamanho original (todos arquivos): " + totalOriginalSize + " bytes");
        System.out.println("Tamanho compactado (Huffman): " + totalCompressedSize + " bytes");
        System.out.println("Tamanho do pacote final: " + baos.size() + " bytes");
        double taxa = (1 - (double)baos.size() / totalOriginalSize) * 100;
        System.out.printf("Taxa de compressão: %.2f%%\n", taxa);
    }
    
    /**
     * Algoritmo de compressão Huffman para um único array de bytes.
     * 
     * @param input Dados de entrada
     * @return Dados compactados
     */
    private static byte[] huffmanCompress(byte[] input) {
        if (input.length == 0) return new byte[0];
        
        // 1. Contar frequência de cada byte
        int[] freq = new int[256];
        for (byte b : input) {
            freq[b & 0xFF]++;
        }
        
        // 2. Construir árvore de Huffman
        PriorityQueue<Node> pq = new PriorityQueue<>();
        for (int i = 0; i < 256; i++) {
            if (freq[i] > 0) {
                pq.add(new Node((byte)i, freq[i]));
            }
        }
        
        if (pq.size() == 1) {
            Node leaf = pq.poll();
            Node parent = new Node(leaf.freq, leaf, null);
            pq.add(parent);
        }
        
        while (pq.size() > 1) {
            Node left = pq.poll();
            Node right = pq.poll();
            Node parent = new Node(left.freq + right.freq, left, right);
            pq.add(parent);
        }
        
        Node root = pq.poll();
        
        // 3. Gerar códigos para cada byte
        String[] codes = new String[256];
        generateCodes(root, "", codes);
        
        // 4. Escrever cabeçalho e dados compactados
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        try {
            // Escreve o número de símbolos distintos
            int numSymbols = 0;
            for (int i = 0; i < 256; i++) {
                if (freq[i] > 0) numSymbols++;
            }
            dos.writeInt(numSymbols);
            
            // Escreve os pares (byte, frequência)
            for (int i = 0; i < 256; i++) {
                if (freq[i] > 0) {
                    dos.writeByte(i);
                    dos.writeInt(freq[i]);
                }
            }
            
            // Escreve os dados compactados
            StringBuilder sb = new StringBuilder();
            for (byte b : input) {
                sb.append(codes[b & 0xFF]);
            }
            
            String bits = sb.toString();
            dos.writeInt(bits.length());
            
            byte[] compressedData = new byte[(bits.length() + 7) / 8];
            for (int i = 0; i < bits.length(); i++) {
                if (bits.charAt(i) == '1') {
                    compressedData[i / 8] |= (1 << (7 - (i % 8)));
                }
            }
            dos.write(compressedData);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return baos.toByteArray();
    }
    
    private static void generateCodes(Node node, String code, String[] codes) {
        if (node == null) return;
        
        if (node.isLeaf()) {
            codes[node.data & 0xFF] = code;
            return;
        }
        
        generateCodes(node.left, code + "0", codes);
        generateCodes(node.right, code + "1", codes);
    }
    
    /**
     * Descompacta um arquivo compactado com Huffman.
     */
    public static void decompressFiles(String compressedFile, String outputDir) throws IOException {
        byte[] data = Files.readAllBytes(Paths.get(compressedFile));
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        
        int numFiles = dis.readInt();
        
        File dir = new File(outputDir);
        if (!dir.exists()) dir.mkdirs();
        
        for (int i = 0; i < numFiles; i++) {
            String filename = dis.readUTF();
            long originalSize = dis.readLong();
            int compressedSize = dis.readInt();
            
            byte[] compressedData = new byte[compressedSize];
            dis.read(compressedData);
            
            // Descomprime o arquivo individual
            byte[] decompressed = huffmanDecompress(compressedData);
            
            String outputPath = outputDir + "/" + new File(filename).getName();
            Files.write(Paths.get(outputPath), decompressed);
            System.out.println("Restaurado: " + outputPath + " (" + decompressed.length + " bytes)");
        }
        
        dis.close();
    }
    
    /**
     * Descompacta dados Huffman de um único arquivo.
     */
    private static byte[] huffmanDecompress(byte[] compressed) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        DataInputStream dis = new DataInputStream(bais);
        
        int numSymbols = dis.readInt();
        int[] freq = new int[256];
        for (int i = 0; i < numSymbols; i++) {
            int symbol = dis.readByte() & 0xFF;
            freq[symbol] = dis.readInt();
        }
        
        PriorityQueue<Node> pq = new PriorityQueue<>();
        for (int i = 0; i < 256; i++) {
            if (freq[i] > 0) {
                pq.add(new Node((byte)i, freq[i]));
            }
        }
        
        if (pq.size() == 1) {
            Node leaf = pq.poll();
            Node parent = new Node(leaf.freq, leaf, null);
            pq.add(parent);
        }
        
        while (pq.size() > 1) {
            Node left = pq.poll();
            Node right = pq.poll();
            Node parent = new Node(left.freq + right.freq, left, right);
            pq.add(parent);
        }
        
        Node root = pq.poll();
        
        int totalBits = dis.readInt();
        int bytesLength = (totalBits + 7) / 8;
        byte[] compressedData = new byte[bytesLength];
        dis.read(compressedData);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Node current = root;
        int bitCount = 0;
        
        for (int i = 0; i < bytesLength && bitCount < totalBits; i++) {
            byte b = compressedData[i];
            for (int j = 7; j >= 0 && bitCount < totalBits; j--) {
                int bit = (b >> j) & 1;
                if (bit == 0) {
                    current = current.left;
                } else {
                    current = current.right;
                }
                
                if (current.isLeaf()) {
                    baos.write(current.data & 0xFF);
                    current = root;
                }
                bitCount++;
            }
        }
        
        return baos.toByteArray();
    }
}