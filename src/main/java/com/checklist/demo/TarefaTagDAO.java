package com.checklist.demo;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe de acesso a dados (DAO) para a entidade TarefaTag (relacionamento N:N).
 * Gerencia as operações de CRUD do relacionamento entre tarefas e tags.
 * 
 * Este DAO é responsável por:
 * - Associar tags a tarefas
 * - Remover associações
 * - Buscar tags de uma tarefa
 * - Buscar tarefas de uma tag
 */
public class TarefaTagDAO {
    // Explicado em docs/aux/tarefaTagDAO/tarefaTagDAO.md
    private final ArquivoIndex<TarefaTag> arqTarefaTags;

    /**
     * Construtor da classe TarefaTagDAO.
     * Inicializa o arquivo com índice para os relacionamentos.
     * 
     * @throws Exception Se houver erro na inicialização
     */
    // Explicado em docs/aux/tarefaTagDAO/construtor.md
    public TarefaTagDAO() throws Exception {
        arqTarefaTags = new ArquivoIndex<>("tarefa_tags", TarefaTag.class.getConstructor());
    }

    /**
     * Busca todas as tags associadas a uma tarefa.
     * Realiza busca sequencial em todos os relacionamentos.
     * 
     * @param idTarefa Identificador da tarefa
     * @return Lista de objetos TarefaTag da tarefa
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/tarefaTagDAO/buscarTagsPorTarefa.md
    public List<TarefaTag> buscarTagsPorTarefa(int idTarefa) throws Exception {
        List<TarefaTag> resultado = new ArrayList<>();
        List<TarefaTag> todos = arqTarefaTags.listAll();
        
        for (TarefaTag rel : todos) {
            if (rel.getIdTarefa() == idTarefa) {
                resultado.add(rel);
            }
        }
        
        return resultado;
    }
    
    /**
     * Busca todas as tarefas associadas a uma tag.
     * Realiza busca sequencial em todos os relacionamentos.
     * 
     * @param idTag Identificador da tag
     * @return Lista de objetos TarefaTag da tag
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/tarefaTagDAO/buscarTarefasPorTag.md
    public List<TarefaTag> buscarTarefasPorTag(int idTag) throws Exception {
        List<TarefaTag> resultado = new ArrayList<>();
        List<TarefaTag> todos = arqTarefaTags.listAll();
        
        for (TarefaTag rel : todos) {
            if (rel.getIdTag() == idTag) {
                resultado.add(rel);
            }
        }
        
        return resultado;
    }

    /**
     * Cria uma nova associação entre uma tarefa e uma tag.
     * Verifica se o relacionamento já existe para evitar duplicação.
     * 
     * @param relacionamento Objeto TarefaTag a ser incluído
     * @return true se incluído com sucesso
     * @throws Exception Se o relacionamento já existir
     */
    // Explicado em docs/aux/tarefaTagDAO/incluirRelacionamento.md
    public boolean incluirRelacionamento(TarefaTag relacionamento) throws Exception {
        // Verificar se o relacionamento já existe
        List<TarefaTag> existentes = buscarTagsPorTarefa(relacionamento.getIdTarefa());
        for (TarefaTag existente : existentes) {
            if (existente.getIdTag() == relacionamento.getIdTag()) {
                throw new Exception("Relacionamento já existe!");
            }
        }
        
        return arqTarefaTags.create(relacionamento) > 0;
    }

    /**
     * Remove a associação entre uma tag e uma tarefa.
     * 
     * @param idTag Identificador da tag
     * @param idTarefa Identificador da tarefa
     * @return true se removido com sucesso
     * @throws Exception Se houver erro na remoção
     */
    // Explicado em docs/aux/tarefaTagDAO/excluirRelacionamento.md
    public boolean excluirRelacionamento(int idTag, int idTarefa) throws Exception {
        List<TarefaTag> relacionamentos = buscarTagsPorTarefa(idTarefa);
        for (TarefaTag rel : relacionamentos) {
            if (rel.getIdTag() == idTag) {
                int idComposto = idTarefa * 1000000 + idTag;
                return arqTarefaTags.delete(idComposto);
            }
        }
        return false;
    }
    
    /**
     * Remove todas as associações de uma tarefa.
     * Utilizado quando uma tarefa é excluída.
     * 
     * @param idTarefa Identificador da tarefa
     * @return true se todas as associações foram removidas
     * @throws Exception Se houver erro na remoção
     */
    // Explicado em docs/aux/tarefaTagDAO/excluirTagsPorTarefa.md
    public boolean excluirTagsPorTarefa(int idTarefa) throws Exception {
        List<TarefaTag> tags = buscarTagsPorTarefa(idTarefa);
        boolean sucesso = true;
        for (TarefaTag tag : tags) {
            if (!excluirRelacionamento(tag.getIdTag(), idTarefa)) {
                sucesso = false;
            }
        }
        return sucesso;
    }
    
    /**
     * Lista todos os relacionamentos tarefa-tag cadastrados.
     * 
     * @return Lista com todos os relacionamentos
     * @throws Exception Se houver erro na listagem
     */
    // Explicado em docs/aux/tarefaTagDAO/listarTodos.md
    public List<TarefaTag> listarTodos() throws Exception {
        return arqTarefaTags.listAll();
    }
    
    /**
     * Fecha a conexão com o arquivo de relacionamentos.
     * 
     * @throws Exception Se houver erro no fechamento
     */
    // Explicado em docs/aux/tarefaTagDAO/close.md
    public void close() throws Exception {
        arqTarefaTags.close();
    }
}