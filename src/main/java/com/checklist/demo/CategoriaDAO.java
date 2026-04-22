import java.util.ArrayList;
import java.util.List;

/**
 * Classe de acesso a dados (DAO) para a entidade Categoria.
 * Gerencia as operações de CRUD e os relacionamentos da categoria.
 */
public class CategoriaDAO {
    // Explicado em docs/aux/categoriaDAO/categoriaDAO.md
    private final ArquivoIndex<Categoria> arqCategorias;
    private final UsuarioDAO usuarioDAO;

    /**
     * Construtor da classe CategoriaDAO.
     * Inicializa o arquivo com índice e o DAO de usuários.
     * 
     * @throws Exception Se houver erro na inicialização
     */
    // Explicado em docs/aux/categoriaDAO/construtor.md
    public CategoriaDAO() throws Exception {
        arqCategorias = new ArquivoIndex<>("categorias", Categoria.class.getConstructor());
        usuarioDAO = new UsuarioDAO();
    }

    /**
     * Busca uma categoria pelo ID.
     * 
     * @param id Identificador da categoria
     * @return Objeto Categoria encontrado ou null
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/categoriaDAO/buscarCategoria.md
    public Categoria buscarCategoria(int id) throws Exception {
        return arqCategorias.read(id);
    }

    /**
     * Busca todas as categorias de um usuário específico.
     * Realiza uma busca sequencial em todas as categorias.
     * 
     * @param idUser Identificador do usuário
     * @return Lista de categorias do usuário
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/categoriaDAO/buscarCategoriasPorUsuario.md
    public List<Categoria> buscarCategoriasPorUsuario(int idUser) throws Exception {
        List<Categoria> resultado = new ArrayList<>();
        List<Categoria> todas = arqCategorias.listAll();
        
        for (Categoria categoria : todas) {
            if (categoria.getIdUser() == idUser) {
                resultado.add(categoria);
            }
        }
        
        return resultado;
    }

    /**
     * Inclui uma nova categoria no sistema.
     * Verifica se o usuário informado existe antes de criar.
     * 
     * @param categoria Objeto Categoria a ser incluído
     * @return true se incluída com sucesso
     * @throws Exception Se o usuário não existir
     */
    // Explicado em docs/aux/categoriaDAO/incluirCategoria.md
    public boolean incluirCategoria(Categoria categoria) throws Exception {
        // Validação de integridade referencial: usuário deve existir
        Usuario usuario = usuarioDAO.buscarUsuario(categoria.getIdUser());
        if (usuario == null) {
            throw new Exception("Usuário com ID " + categoria.getIdUser() + " não encontrado!");
        }
        
        int id = arqCategorias.create(categoria);
        if (id > 0) {
            categoria.setId(id);
            return true;
        }
        
        return false;
    }

    /**
     * Altera os dados de uma categoria existente.
     * 
     * @param categoria Objeto com os dados atualizados
     * @return true se alterada com sucesso
     * @throws Exception Se houver erro na alteração
     */
    // Explicado em docs/aux/categoriaDAO/alterarCategoria.md
    public boolean alterarCategoria(Categoria categoria) throws Exception {
        return arqCategorias.update(categoria);
    }

    /**
     * Exclui uma categoria do sistema.
     * Verifica se existem tarefas associadas antes de excluir.
     * 
     * @param id Identificador da categoria a ser excluída
     * @return true se excluída com sucesso
     * @throws Exception Se houver tarefas associadas
     */
    // Explicado em docs/aux/categoriaDAO/excluirCategoria.md
    public boolean excluirCategoria(int id) throws Exception {
        Categoria categoria = buscarCategoria(id);
        if (categoria == null) {
            return false;
        }
        
        // Verificar se existem tarefas usando esta categoria
        TarefaDAO tarefaDAO = new TarefaDAO();
        List<Tarefa> tarefas = tarefaDAO.buscarTarefasPorCategoria(id);
        if (!tarefas.isEmpty()) {
            throw new Exception("Não é possível excluir categoria pois existem " + 
                               tarefas.size() + " tarefas associadas!");
        }
        
        return arqCategorias.delete(id);
    }
    
    /**
     * Lista todas as categorias cadastradas.
     * 
     * @return Lista com todas as categorias
     * @throws Exception Se houver erro na listagem
     */
    // Explicado em docs/aux/categoriaDAO/listarTodas.md
    public List<Categoria> listarTodas() throws Exception {
        return arqCategorias.listAll();
    }
    
    /**
     * Fecha a conexão com o arquivo de categorias.
     * 
     * @throws Exception Se houver erro no fechamento
     */
    // Explicado em docs/aux/categoriaDAO/close.md
    public void close() throws Exception {
        arqCategorias.close();
    }
}