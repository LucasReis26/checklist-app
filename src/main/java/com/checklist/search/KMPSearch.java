package com.checklist.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do algoritmo de casamento de padrões Knuth-Morris-Pratt (KMP).
 * 
 * Complexidade: O(n + m) onde n = tamanho do texto, m = tamanho do padrão
 */
public class KMPSearch {
    
    /**
     * Busca todas as ocorrências de um padrão em um texto usando KMP.
     * 
     * @param text Texto onde será feita a busca
     * @param pattern Padrão a ser procurado
     * @return Lista de posições onde o padrão foi encontrado
     */
    public static List<Integer> search(String text, String pattern) {
        List<Integer> positions = new ArrayList<>();
        
        if (text == null || pattern == null || pattern.isEmpty() || text.isEmpty()) {
            return positions;
        }
        
        if (pattern.length() > text.length()) {
            return positions;
        }
        
        // 1. Construir a tabela de prefixo (função de falha)
        int[] lps = computeLPS(pattern);
        
        // 2. Realizar a busca
        int i = 0; // Índice no texto
        int j = 0; // Índice no padrão
        
        while (i < text.length()) {
            if (pattern.charAt(j) == text.charAt(i)) {
                i++;
                j++;
            }
            
            if (j == pattern.length()) {
                // Padrão encontrado na posição i - j
                positions.add(i - j);
                j = lps[j - 1];
            } else if (i < text.length() && pattern.charAt(j) != text.charAt(i)) {
                // Caractere não coincide
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }
        
        return positions;
    }
    
    /**
     * Constrói a tabela LPS (Longest Proper Prefix which is also Suffix).
     * 
     * @param pattern Padrão para o qual a tabela será construída
     * @return Array LPS
     */
    private static int[] computeLPS(String pattern) {
        int[] lps = new int[pattern.length()];
        int length = 0; // Comprimento do maior prefixo/sufixo
        int i = 1;
        
        lps[0] = 0; // LPS[0] sempre é 0
        
        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(length)) {
                length++;
                lps[i] = length;
                i++;
            } else {
                if (length != 0) {
                    length = lps[length - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        
        return lps;
    }
    
    /**
     * Verifica se um padrão existe no texto.
     * 
     * @param text Texto onde será feita a busca
     * @param pattern Padrão a ser procurado
     * @return true se o padrão foi encontrado
     */
    public static boolean contains(String text, String pattern) {
        return !search(text, pattern).isEmpty();
    }
    
    /**
     * Conta quantas vezes um padrão aparece no texto.
     * 
     * @param text Texto onde será feita a busca
     * @param pattern Padrão a ser procurado
     * @return Número de ocorrências
     */
    public static int count(String text, String pattern) {
        return search(text, pattern).size();
    }
}