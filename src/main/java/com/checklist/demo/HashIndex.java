package com.checklist.demo;

import java.util.List;

/**
 * Interface que define o contrato para índices hash extensível.
 * 
 * @param <K> Tipo da chave (ex: Integer para IDs)
 * @param <V> Tipo do valor armazenado (ex: List<Long> para endereços, List<Integer> para tags)
 * 
 * Esta interface é implementada pelas classes que gerenciam índices hash
 * para relacionamentos como:
 * - Usuário → Tarefas (HashIndexUsuario)
 * - Tarefa → Tags (HashIndexTarefaTags)
 */
public interface HashIndex<K, V> {
    // Explicado em docs/aux/hashIndex/inserir.md
    void inserir(K chave, V valor) throws Exception;
    
    // Explicado em docs/aux/hashIndex/buscar.md
    V buscar(K chave) throws Exception;
    
    // Explicado em docs/aux/hashIndex/remover.md
    boolean remover(K chave) throws Exception;
    
    /**
     * Busca todos os valores associados a uma chave.
     * Utilizado quando uma chave pode ter múltiplos valores.
     * 
     * @param chave Chave a ser buscada
     * @return Lista de valores associados à chave
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/hashIndex/buscarTodos.md
    List<V> buscarTodos(K chave) throws Exception;
}