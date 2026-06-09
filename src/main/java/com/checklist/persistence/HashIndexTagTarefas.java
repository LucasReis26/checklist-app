package com.checklist.persistence;

import com.checklist.model.Tag;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do índice hash para o relacionamento Tag ↔ Tarefas (N:N).
 * 
 * Estrutura: Chave = ID da tag, Valor = Lista de IDs das tarefas associadas
 * Utiliza HashExtensivel com RegistroListaTarefas para persistência.
 * 
 * Este índice permite busca eficiente (O(1) médio) de todas as tarefas de uma tag.
 */
public class HashIndexTagTarefas implements HashIndex<Integer, List<Integer>> {
    private HashExtensivel<RegistroListaTarefas> hashIndex;
    
    /**
     * Construtor da classe HashIndexTagTarefas.
     * Inicializa o hash extensível com o arquivo "idx_tag_tarefas".
     * 
     * @throws Exception Se houver erro na inicialização
     */
    public HashIndexTagTarefas() throws Exception {
        this.hashIndex = HashExtensivel.getInstance("idx_tag_tarefas", 
                                               RegistroListaTarefas.class.getConstructor());
    }
    
    /**
     * Insere um mapeamento entre uma tag e suas tarefas.
     * 
     * @param idTag Chave (ID da tag)
     * @param tarefas Valor (lista de IDs das tarefas associadas)
     * @throws Exception Se houver erro na inserção
     */
    @Override
    public void inserir(Integer idTag, List<Integer> tarefas) throws Exception {
        RegistroListaTarefas registro = new RegistroListaTarefas(idTag, tarefas);
        hashIndex.inserir(idTag, registro);
    }
    
    /**
     * Busca as tarefas associadas a uma tag.
     * 
     * @param idTag Chave (ID da tag)
     * @return Lista de IDs das tarefas ou lista vazia se não encontrado
     * @throws Exception Se houver erro na busca
     */
    @Override
    public List<Integer> buscar(Integer idTag) throws Exception {
        RegistroListaTarefas registro = hashIndex.buscar(idTag);
        if (registro != null) {
            return registro.getTarefas();
        }
        return new ArrayList<>();
    }
    
    /**
     * Remove o mapeamento de uma tag.
     * 
     * @param idTag Chave (ID da tag)
     * @return true se removido com sucesso, false caso contrário
     * @throws Exception Se houver erro na remoção
     */
    @Override
    public boolean remover(Integer idTag) throws Exception {
        return hashIndex.remover(idTag);
    }
    
    /**
     * Busca todos os mapeamentos para uma tag.
     * 
     * @param idTag Chave (ID da tag)
     * @return Lista contendo as tarefas ou lista vazia
     * @throws Exception Se houver erro na busca
     */
    @Override
    public List<List<Integer>> buscarTodos(Integer idTag) throws Exception {
        List<List<Integer>> resultados = new ArrayList<>();
        List<Integer> tarefas = buscar(idTag);
        if (!tarefas.isEmpty()) {
            resultados.add(tarefas);
        }
        return resultados;
    }
    
    /**
     * Fecha a conexão com o arquivo de hash.
     * 
     * @throws Exception Se houver erro no fechamento
     */
    public void close() throws Exception {
        hashIndex.close();
    }
}
