package com.checklist.demo;

import java.util.ArrayList;
import java.util.List;

/**
 * Gerenciador do relacionamento N:1 entre Categoria e Tarefa.
 * Uma categoria pode ter várias tarefas (1:N).
 * 
 * Armazena a lista de IDs de tarefas para cada categoria usando
 * um arquivo de índice com chave = idCategoria e valor = lista de ids das tarefas.
 */
public class CategoriaTarefasManager {
    // Explicado em docs/aux/categoriaTarefasManager/categoriaTarefasManager.md
    private ArquivoIndex<RegistroListaTarefas> arquivoRelacoes;
    
    /**
     * Construtor da classe CategoriaTarefasManager.
     * Inicializa o arquivo que armazena as relações.
     * 
     * @throws Exception Se houver erro na inicialização
     */
    // Explicado em docs/aux/categoriaTarefasManager/construtor.md
    public CategoriaTarefasManager() throws Exception {
        arquivoRelacoes = new ArquivoIndex<>("categoria_tarefas_rel", 
                                              RegistroListaTarefas.class.getConstructor());
    }
    
    /**
     * Adiciona uma tarefa à lista de tarefas de uma categoria.
     * 
     * @param idCategoria Identificador da categoria
     * @param idTarefa Identificador da tarefa a ser adicionada
     * @throws Exception Se houver erro na operação
     */
    // Explicado em docs/aux/categoriaTarefasManager/adicionarTarefa.md
    public void adicionarTarefa(int idCategoria, int idTarefa) throws Exception {
        // Busca o registro existente para esta categoria
        RegistroListaTarefas registro = arquivoRelacoes.read(idCategoria);
        List<Integer> tarefas;
        
        if (registro == null) {
            // Categoria não tem tarefas ainda
            tarefas = new ArrayList<>();
        } else {
            // Categoria já possui tarefas
            tarefas = registro.getTarefas();
        }
        
        // Evita duplicação
        if (!tarefas.contains(idTarefa)) {
            tarefas.add(idTarefa);
            RegistroListaTarefas novoRegistro = new RegistroListaTarefas(idCategoria, tarefas);
            arquivoRelacoes.save(novoRegistro);
        }
    }
    
    /**
     * Remove uma tarefa da lista de tarefas de uma categoria.
     * 
     * @param idCategoria Identificador da categoria
     * @param idTarefa Identificador da tarefa a ser removida
     * @throws Exception Se houver erro na operação
     */
    // Explicado em docs/aux/categoriaTarefasManager/removerTarefa.md
    public void removerTarefa(int idCategoria, int idTarefa) throws Exception {
        RegistroListaTarefas registro = arquivoRelacoes.read(idCategoria);
        if (registro != null) {
            List<Integer> tarefas = registro.getTarefas();
            tarefas.remove(Integer.valueOf(idTarefa));
            
            if (tarefas.isEmpty()) {
                // Se não há mais tarefas, remove o registro
                arquivoRelacoes.delete(idCategoria);
            } else {
                // Atualiza a lista
                registro.setTarefas(tarefas);
                arquivoRelacoes.update(registro);
            }
        }
    }
    
    /**
     * Busca todas as tarefas associadas a uma categoria.
     * 
     * @param idCategoria Identificador da categoria
     * @return Lista de IDs das tarefas da categoria
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/categoriaTarefasManager/buscarTarefasDaCategoria.md
    public List<Integer> buscarTarefasDaCategoria(int idCategoria) throws Exception {
        RegistroListaTarefas registro = arquivoRelacoes.read(idCategoria);
        if (registro != null) {
            return registro.getTarefas();
        }
        return new ArrayList<>();
    }
    
    /**
     * Conta quantas tarefas estão associadas a uma categoria.
     * 
     * @param idCategoria Identificador da categoria
     * @return Número de tarefas da categoria
     * @throws Exception Se houver erro na contagem
     */
    // Explicado em docs/aux/categoriaTarefasManager/contarTarefasDaCategoria.md
    public int contarTarefasDaCategoria(int idCategoria) throws Exception {
        return buscarTarefasDaCategoria(idCategoria).size();
    }
    
    /**
     * Fecha a conexão com o arquivo de relações.
     * 
     * @throws Exception Se houver erro no fechamento
     */
    // Explicado em docs/aux/categoriaTarefasManager/close.md
    public void close() throws Exception {
        arquivoRelacoes.close();
    }
}