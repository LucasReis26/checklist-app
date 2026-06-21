package com.checklist.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação do algoritmo de casamento de padrões Boyer-Moore.
 * 
 * Características:
 * - Bad Character Heuristic (obrigatório)
 * - Good Suffix Heuristic (opcional - implementado para melhor performance)
 * 
 * Complexidade: O(n/m) no melhor caso, O(n*m) no pior caso
 */
public class BoyerMooreSearch {
    
    /**
     * Busca todas as ocorrências de um padrão em um texto usando Boyer-Moore.
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
        
        // 1. Pré-processamento
        Map<Character, Integer> badCharTable = buildBadCharTable(pattern);
        int[] goodSuffixTable = buildGoodSuffixTable(pattern);
        
        int n = text.length();
        int m = pattern.length();
        int shift = 0; // Deslocamento do padrão sobre o texto
        
        while (shift <= n - m) {
            int j = m - 1; // Começa pelo último caractere do padrão
            
            // Compara da direita para a esquerda
            while (j >= 0 && pattern.charAt(j) == text.charAt(shift + j)) {
                j--;
            }
            
            if (j < 0) {
                // Padrão encontrado na posição 'shift'
                positions.add(shift);
                
                // Desloca para a próxima posição (usa good suffix se disponível)
                if (shift + m < n) {
                    int nextShift = m - goodSuffixTable[0];
                    shift += Math.max(1, nextShift);
                } else {
                    shift++;
                }
            } else {
                // Caractere não coincide
                int badCharShift = getBadCharShift(badCharTable, text.charAt(shift + j), j);
                int goodSuffixShift = goodSuffixTable[j + 1];
                
                // Usa o maior deslocamento entre bad character e good suffix
                shift += Math.max(badCharShift, goodSuffixShift);
            }
        }
        
        return positions;
    }
    
    /**
     * Constrói a tabela de Bad Character.
     * 
     * @param pattern Padrão para o qual a tabela será construída
     * @return Mapa com a última ocorrência de cada caractere
     */
    private static Map<Character, Integer> buildBadCharTable(String pattern) {
        Map<Character, Integer> table = new HashMap<>();
        
        for (int i = 0; i < pattern.length(); i++) {
            table.put(pattern.charAt(i), i);
        }
        
        return table;
    }
    
    /**
     * Obtém o deslocamento baseado na regra do Bad Character.
     * 
     * @param table Tabela de Bad Character
     * @param c Caractere que não coincide
     * @param j Posição atual no padrão
     * @return Deslocamento calculado
     */
    private static int getBadCharShift(Map<Character, Integer> table, char c, int j) {
        if (table.containsKey(c)) {
            return j - table.get(c);
        } else {
            return j + 1; // Caractere não está no padrão, pode deslocar completamente
        }
    }
    
    /**
     * Constrói a tabela de Good Suffix (opcional).
     * 
     * @param pattern Padrão para o qual a tabela será construída
     * @return Array de deslocamentos do good suffix
     */
    private static int[] buildGoodSuffixTable(String pattern) {
        int m = pattern.length();
        int[] goodSuffix = new int[m + 1];
        int[] border = new int[m + 1];
        
        // Caso base
        int i = m;
        int j = m + 1;
        border[i] = j;
        
        while (i > 0) {
            while (j <= m && pattern.charAt(i - 1) != pattern.charAt(j - 1)) {
                if (goodSuffix[j] == 0) {
                    goodSuffix[j] = j - i;
                }
                j = border[j];
            }
            i--;
            j--;
            border[i] = j;
        }
        
        j = border[0];
        for (i = 0; i <= m; i++) {
            if (goodSuffix[i] == 0) {
                goodSuffix[i] = j;
            }
            if (i == j) {
                j = border[j];
            }
        }
        
        return goodSuffix;
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