package com.checklist.demo.persistence;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do índice hash para o relacionamento Usuário → Tarefas.
 * 
 * Estrutura: Chave = ID do usuário, Valor = Lista de endereços das tarefas
 * Utiliza HashExtensivel com RegistroListaEnderecos para persistência.
 * 
 * Este índice permite busca eficiente (O(1) médio) de todas as tarefas de um usuário.
 */
public class HashIndexUsuario implements HashIndex<Integer, List<Long>> {
    // Explicado em docs/aux/hashIndexUsuario/hashIndexUsuario.md
    private HashExtensivel<RegistroListaEnderecos> hashIndex;
    
    /**
     * Construtor da classe HashIndexUsuario.
     * Inicializa o hash extensível com o arquivo "idx_usuario_tarefa".
     * 
     * @throws Exception Se houver erro na inicialização
     */
    // Explicado em docs/aux/hashIndexUsuario/construtor.md
    public HashIndexUsuario() throws Exception {
        this.hashIndex = new HashExtensivel<>("idx_usuario_tarefa", 
                                               RegistroListaEnderecos.class.getConstructor());
    }
    
    /**
     * Insere um mapeamento entre um usuário e os endereços de suas tarefas.
     * 
     * @param idUsuario Chave (ID do usuário)
     * @param enderecosTarefas Valor (lista de endereços das tarefas)
     * @throws Exception Se houver erro na inserção
     */
    @Override
    // Explicado em docs/aux/hashIndexUsuario/inserir.md
    public void inserir(Integer idUsuario, List<Long> enderecosTarefas) throws Exception {
        RegistroListaEnderecos registro = new RegistroListaEnderecos(idUsuario, enderecosTarefas);
        hashIndex.inserir(idUsuario, registro);
    }
    
    /**
     * Busca os endereços das tarefas de um usuário.
     * 
     * @param idUsuario Chave (ID do usuário)
     * @return Lista de endereços das tarefas ou lista vazia se não encontrado
     * @throws Exception Se houver erro na busca
     */
    @Override
    // Explicado em docs/aux/hashIndexUsuario/buscar.md
    public List<Long> buscar(Integer idUsuario) throws Exception {
        RegistroListaEnderecos registro = hashIndex.buscar(idUsuario);
        if (registro != null) {
            return registro.getEnderecos();
        }
        return new ArrayList<>();
    }
    
    /**
     * Remove o mapeamento de um usuário.
     * 
     * @param idUsuario Chave (ID do usuário)
     * @return true se removido com sucesso, false caso contrário
     * @throws Exception Se houver erro na remoção
     */
    @Override
    // Explicado em docs/aux/hashIndexUsuario/remover.md
    public boolean remover(Integer idUsuario) throws Exception {
        return hashIndex.remover(idUsuario);
    }
    
    /**
     * Busca todos os mapeamentos para um usuário.
     * Como a chave é única, retorna uma lista com um único elemento ou vazia.
     * 
     * @param idUsuario Chave (ID do usuário)
     * @return Lista contendo os endereços ou lista vazia
     * @throws Exception Se houver erro na busca
     */
    @Override
    // Explicado em docs/aux/hashIndexUsuario/buscarTodos.md
    public List<List<Long>> buscarTodos(Integer idUsuario) throws Exception {
        List<List<Long>> resultados = new ArrayList<>();
        List<Long> enderecos = buscar(idUsuario);
        if (!enderecos.isEmpty()) {
            resultados.add(enderecos);
        }
        return resultados;
    }
    
    /**
     * Fecha a conexão com o arquivo de hash.
     * 
     * @throws Exception Se houver erro no fechamento
     */
    // Explicado em docs/aux/hashIndexUsuario/close.md
    public void close() throws Exception {
        hashIndex.close();
    }
}
