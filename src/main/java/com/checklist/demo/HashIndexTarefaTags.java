package com.checklist.demo;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do índice hash para o relacionamento Tarefa ↔ Tags (N:N).
 * 
 * Estrutura: Chave = ID da tarefa, Valor = Lista de IDs das tags associadas
 * Utiliza HashExtensivel com RegistroListaTags para persistência.
 * 
 * Este índice permite busca eficiente (O(1) médio) de todas as tags de uma tarefa.
 */
public class HashIndexTarefaTags implements HashIndex<Integer, List<Integer>> {
    // Explicado em docs/aux/hashIndexTarefaTags/hashIndexTarefaTags.md
    private HashExtensivel<RegistroListaTags> hashIndex;
    
    /**
     * Construtor da classe HashIndexTarefaTags.
     * Inicializa o hash extensível com o arquivo "idx_tarefa_tags".
     * 
     * @throws Exception Se houver erro na inicialização
     */
    // Explicado em docs/aux/hashIndexTarefaTags/construtor.md
    public HashIndexTarefaTags() throws Exception {
        this.hashIndex = new HashExtensivel<>("idx_tarefa_tags", 
                                               RegistroListaTags.class.getConstructor());
    }
    
    /**
     * Insere um mapeamento entre uma tarefa e suas tags.
     * 
     * @param idTarefa Chave (ID da tarefa)
     * @param tags Valor (lista de IDs das tags associadas)
     * @throws Exception Se houver erro na inserção
     */
    @Override
    // Explicado em docs/aux/hashIndexTarefaTags/inserir.md
    public void inserir(Integer idTarefa, List<Integer> tags) throws Exception {
        RegistroListaTags registro = new RegistroListaTags(idTarefa, tags);
        hashIndex.inserir(idTarefa, registro);
    }
    
    /**
     * Busca as tags associadas a uma tarefa.
     * 
     * @param idTarefa Chave (ID da tarefa)
     * @return Lista de IDs das tags ou lista vazia se não encontrado
     * @throws Exception Se houver erro na busca
     */
    @Override
    // Explicado em docs/aux/hashIndexTarefaTags/buscar.md
    public List<Integer> buscar(Integer idTarefa) throws Exception {
        RegistroListaTags registro = hashIndex.buscar(idTarefa);
        if (registro != null) {
            return registro.getTags();
        }
        return new ArrayList<>();
    }
    
    /**
     * Remove o mapeamento de uma tarefa.
     * 
     * @param idTarefa Chave (ID da tarefa)
     * @return true se removido com sucesso, false caso contrário
     * @throws Exception Se houver erro na remoção
     */
    @Override
    // Explicado em docs/aux/hashIndexTarefaTags/remover.md
    public boolean remover(Integer idTarefa) throws Exception {
        return hashIndex.remover(idTarefa);
    }
    
    /**
     * Busca todos os mapeamentos para uma tarefa.
     * Como a chave é única, retorna uma lista com um único elemento ou vazia.
     * 
     * @param idTarefa Chave (ID da tarefa)
     * @return Lista contendo as tags ou lista vazia
     * @throws Exception Se houver erro na busca
     */
    @Override
    // Explicado em docs/aux/hashIndexTarefaTags/buscarTodos.md
    public List<List<Integer>> buscarTodos(Integer idTarefa) throws Exception {
        List<List<Integer>> resultados = new ArrayList<>();
        List<Integer> tags = buscar(idTarefa);
        if (!tags.isEmpty()) {
            resultados.add(tags);
        }
        return resultados;
    }
    
    /**
     * Fecha a conexão com o arquivo de hash.
     * 
     * @throws Exception Se houver erro no fechamento
     */
    // Explicado em docs/aux/hashIndexTarefaTags/close.md
    public void close() throws Exception {
        hashIndex.close();
    }
}