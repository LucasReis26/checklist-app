package com.checklist.crypto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Implementação de criptografia XOR.
 * 
 * XOR é um algoritmo de criptografia simétrica onde cada byte é combinado
 * com uma chave usando a operação XOR (ou exclusivo).
 * 
 * Vantagens: Simples, rápido, fácil de implementar
 * Desvantagens: Vulnerável a ataques de força bruta se a chave for curta
 */
public class XORCrypto {
    
    private static final String DEFAULT_KEY = "ChecklistApp2024!@#SecureKey";
    
    /**
     * Criptografa um texto usando XOR com a chave padrão.
     * 
     * @param text Texto a ser criptografado
     * @return Texto criptografado em Base64
     */
    public static String encrypt(String text) {
        return encrypt(text, DEFAULT_KEY);
    }
    
    /**
     * Descriptografa um texto usando XOR com a chave padrão.
     * 
     * @param encrypted Texto criptografado em Base64
     * @return Texto descriptografado
     */
    public static String decrypt(String encrypted) {
        return decrypt(encrypted, DEFAULT_KEY);
    }
    
    /**
     * Criptografa um texto usando XOR com uma chave específica.
     * 
     * @param text Texto a ser criptografado
     * @param key Chave de criptografia
     * @return Texto criptografado em Base64
     */
    public static String encrypt(String text, String key) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = new byte[textBytes.length];
        
        for (int i = 0; i < textBytes.length; i++) {
            encryptedBytes[i] = (byte) (textBytes[i] ^ keyBytes[i % keyBytes.length]);
        }
        
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
    
    /**
     * Descriptografa um texto usando XOR com uma chave específica.
     * 
     * @param encrypted Texto criptografado em Base64
     * @param key Chave de criptografia
     * @return Texto descriptografado
     */
    public static String decrypt(String encrypted, String key) {
        if (encrypted == null || encrypted.isEmpty()) {
            return encrypted;
        }
        
        byte[] encryptedBytes = Base64.getDecoder().decode(encrypted);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] decryptedBytes = new byte[encryptedBytes.length];
        
        for (int i = 0; i < encryptedBytes.length; i++) {
            decryptedBytes[i] = (byte) (encryptedBytes[i] ^ keyBytes[i % keyBytes.length]);
        }
        
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
    
    /**
     * Criptografa um campo sensível do usuário (ex: senha).
     * 
     * @param senha Senha a ser criptografada
     * @return Senha criptografada
     */
    public static String encryptSenha(String senha) {
        return encrypt(senha);
    }
    
    /**
     * Descriptografa um campo sensível do usuário (ex: senha).
     * 
     * @param senhaEncrypted Senha criptografada
     * @return Senha descriptografada
     */
    public static String decryptSenha(String senhaEncrypted) {
        return decrypt(senhaEncrypted);
    }
}