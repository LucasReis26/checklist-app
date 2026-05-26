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
    private final HashIndexTarefaTags indexTarefaTags;
    private final HashIndexTagTarefas indexTagTarefas;

    /**
     * Construtor da classe TarefaTagDAO.
     * Inicializa o arquivo com índice para os relacionamentos e os índices Hash.
     * 
     * @throws Exception Se houver erro na inicialização
     */
    // Explicado em docs/aux/tarefaTagDAO/construtor.md
    public TarefaTagDAO() throws Exception {
        arqTarefaTags = new ArquivoIndex<>("tarefa_tags", TarefaTag.class.getConstructor());
        indexTarefaTags = new HashIndexTarefaTags();
        indexTagTarefas = new HashIndexTagTarefas();
    }

    /**
     * Busca todas as tags associadas a uma tarefa de forma eficiente usando Hash.
     * 
     * @param idTarefa Identificador da tarefa
     * @return Lista de objetos TarefaTag da tarefa
     * @throws Exception Se houver erro na busca
     */
    public List<TarefaTag> buscarTagsPorTarefa(int idTarefa) throws Exception {
        List<TarefaTag> resultado = new ArrayList<>();
        List<Integer> idsTags = indexTarefaTags.buscar(idTarefa);
        
        for (Integer idTag : idsTags) {
            resultado.add(new TarefaTag(idTag, idTarefa));
        }
        
        return resultado;
    }
    
    /**
     * Busca todas as tarefas associadas a uma tag de forma eficiente usando Hash.
     * 
     * @param idTag Identificador da tag
     * @return Lista de objetos TarefaTag da tag
     * @throws Exception Se houver erro na busca
     */
    public List<TarefaTag> buscarTarefasPorTag(int idTag) throws Exception {
        List<TarefaTag> resultado = new ArrayList<>();
        List<Integer> idsTarefas = indexTagTarefas.buscar(idTag);
        
        for (Integer idTarefa : idsTarefas) {
            resultado.add(new TarefaTag(idTag, idTarefa));
        }
        
        return resultado;
    }

    /**
     * Cria uma nova associação entre uma tarefa e uma tag.
     * Atualiza os índices Hash em ambas as direções.
     * 
     * @param relacionamento Objeto TarefaTag a ser incluído
     * @return true se incluído com sucesso
     * @throws Exception Se o relacionamento já existir
     */
    public boolean incluirRelacionamento(TarefaTag relacionamento) throws Exception {
        List<Integer> tagsAtuais = indexTarefaTags.buscar(relacionamento.getIdTarefa());
        if (tagsAtuais.contains(relacionamento.getIdTag())) {
            throw new Exception("Relacionamento já existe!");
        }
        
        if (arqTarefaTags.create(relacionamento) > 0) {
            // Atualiza índice Tarefa -> Tags
            tagsAtuais.add(relacionamento.getIdTag());
            indexTarefaTags.inserir(relacionamento.getIdTarefa(), tagsAtuais);
            
            // Atualiza índice Tag -> Tarefas
            List<Integer> tarefasAtuais = indexTagTarefas.buscar(relacionamento.getIdTag());
            tarefasAtuais.add(relacionamento.getIdTarefa());
            indexTagTarefas.inserir(relacionamento.getIdTag(), tarefasAtuais);
            
            return true;
        }
        return false;
    }

    /**
     * Remove a associação entre uma tag e uma tarefa.
     * Atualiza os índices Hash em ambas as direções.
     * 
     * @param idTag Identificador da tag
     * @param idTarefa Identificador da tarefa
     * @return true se removido com sucesso
     * @throws Exception Se houver erro na remoção
     */
    public boolean excluirRelacionamento(int idTag, int idTarefa) throws Exception {
        int idComposto = idTarefa * 1000000 + idTag;
        if (arqTarefaTags.delete(idComposto)) {
            // Atualiza índice Tarefa -> Tags
            List<Integer> tagsAtuais = indexTarefaTags.buscar(idTarefa);
            tagsAtuais.remove(Integer.valueOf(idTag));
            if (tagsAtuais.isEmpty()) {
                indexTarefaTags.remover(idTarefa);
            } else {
                indexTarefaTags.inserir(idTarefa, tagsAtuais);
            }
            
            // Atualiza índice Tag -> Tarefas
            List<Integer> tarefasAtuais = indexTagTarefas.buscar(idTag);
            tarefasAtuais.remove(Integer.valueOf(idTarefa));
            if (tarefasAtuais.isEmpty()) {
                indexTagTarefas.remover(idTag);
            } else {
                indexTagTarefas.inserir(idTag, tarefasAtuais);
            }
            
            return true;
        }
        return false;
    }
    
    /**
     * Remove todas as associações de uma tarefa.
     * 
     * @param idTarefa Identificador da tarefa
     * @return true se todas as associações foram removidas
     * @throws Exception Se houver erro na remoção
     */
    public boolean excluirTagsPorTarefa(int idTarefa) throws Exception {
        List<Integer> tags = indexTarefaTags.buscar(idTarefa);
        for (Integer idTag : tags) {
            excluirRelacionamento(idTag, idTarefa);
        }
        return true;
    }
    
    /**
     * Lista todos os relacionamentos tarefa-tag cadastrados.
     * 
     * @return Lista com todos os relacionamentos
     * @throws Exception Se houver erro na listagem
     */
    public List<TarefaTag> listarTodos() throws Exception {
        return arqTarefaTags.listAll();
    }
    
    /**
     * Fecha a conexão com os arquivos.
     * 
     * @throws Exception Se houver erro no fechamento
     */
    public void close() throws Exception {
        arqTarefaTags.close();
        indexTarefaTags.close();
        indexTagTarefas.close();
    }
}