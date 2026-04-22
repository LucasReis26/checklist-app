import java.util.List;

/**
 * Classe de acesso a dados (DAO) para a entidade Tag.
 * Gerencia as operações de CRUD e verifica integridade referencial com tarefas.
 */
public class TagDAO {
    // Explicado em docs/aux/tagDAO/tagDAO.md
    private final ArquivoIndex<Tag> arqTags;

    /**
     * Construtor da classe TagDAO.
     * Inicializa o arquivo com índice para tags.
     * 
     * @throws Exception Se houver erro na inicialização
     */
    // Explicado em docs/aux/tagDAO/construtor.md
    public TagDAO() throws Exception {
        arqTags = new ArquivoIndex<>("tags", Tag.class.getConstructor());
    }

    /**
     * Busca uma tag pelo ID.
     * 
     * @param id Identificador da tag
     * @return Objeto Tag encontrado ou null
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/tagDAO/buscarTag.md
    public Tag buscarTag(int id) throws Exception {
        return arqTags.read(id);
    }

    /**
     * Inclui uma nova tag no sistema.
     * 
     * @param tag Objeto Tag a ser incluído
     * @return true se incluída com sucesso
     * @throws Exception Se houver erro na inclusão
     */
    // Explicado em docs/aux/tagDAO/incluirTag.md
    public boolean incluirTag(Tag tag) throws Exception {
        return arqTags.create(tag) > 0;
    }

    /**
     * Altera os dados de uma tag existente.
     * Verifica se a tag existe antes de alterar.
     * 
     * @param tag Objeto com os dados atualizados
     * @return true se alterada com sucesso
     * @throws Exception Se a tag não existir ou houver erro na alteração
     */
    // Explicado em docs/aux/tagDAO/alterarTag.md
    public boolean alterarTag(Tag tag) throws Exception {
        // Primeiro verificar se a tag existe
        Tag existente = buscarTag(tag.getId());
        if (existente == null) {
            throw new Exception("Tag não encontrada para alteração!");
        }
        
        // Atualizar usando o ArquivoIndex
        boolean resultado = arqTags.update(tag);
        
        if (!resultado) {
            throw new Exception("Falha ao atualizar a tag no arquivo!");
        }
        
        return true;
    }

    /**
     * Exclui uma tag do sistema.
     * Verifica se existem tarefas associadas antes de excluir.
     * 
     * @param id Identificador da tag a ser excluída
     * @return true se excluída com sucesso
     * @throws Exception Se a tag não existir ou tiver tarefas associadas
     */
    // Explicado em docs/aux/tagDAO/excluirTag.md
    public boolean excluirTag(int id) throws Exception {
        Tag existente = buscarTag(id);
        if (existente == null) {
            throw new Exception("Tag não encontrada!");
        }
        
        // Verificar se existem tarefas usando esta tag (integridade referencial)
        TarefaTagDAO tarefaTagDAO = new TarefaTagDAO();
        List<TarefaTag> tarefas = tarefaTagDAO.buscarTarefasPorTag(id);
        tarefaTagDAO.close();
        
        if (!tarefas.isEmpty()) {
            throw new Exception("Não é possível excluir tag pois existem " + 
                               tarefas.size() + " tarefas associadas a ela!");
        }
        return arqTags.delete(id);
    }
    
    /**
     * Lista todas as tags cadastradas.
     * 
     * @return Lista com todas as tags
     * @throws Exception Se houver erro na listagem
     */
    // Explicado em docs/aux/tagDAO/listarTodas.md
    public List<Tag> listarTodas() throws Exception {
        return arqTags.listAll();
    }
    
    /**
     * Fecha a conexão com o arquivo de tags.
     * 
     * @throws Exception Se houver erro no fechamento
     */
    // Explicado em docs/aux/tagDAO/close.md
    public void close() throws Exception {
        arqTags.close();
    }
}