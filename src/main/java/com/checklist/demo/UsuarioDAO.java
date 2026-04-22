package com.checklist.demo;

import java.util.List;

/**
 * Classe de acesso a dados (DAO) para a entidade Usuario.
 * Gerencia as operações de CRUD e os relacionamentos do usuário.
 */
public class UsuarioDAO {
    // Explicado em docs/aux/usuarioDAO/usuarioDAO.md
    private final ArquivoIndex<Usuario> arqUsuarios;
    private final UsuarioTarefasManager usuarioTarefasManager;
    private final UsuarioCategoriasManager usuarioCategoriasManager;

    /**
     * Construtor da classe UsuarioDAO.
     * Inicializa o arquivo com índice e os gerenciadores de relacionamento.
     * 
     * @throws Exception Se houver erro na inicialização
     */
    // Explicado em docs/aux/usuarioDAO/construtor.md
    public UsuarioDAO() throws Exception {
        arqUsuarios = new ArquivoIndex<>("usuarios", Usuario.class.getConstructor());
        usuarioTarefasManager = new UsuarioTarefasManager();
        usuarioCategoriasManager = new UsuarioCategoriasManager();
    }

    /**
     * Busca um usuário pelo ID.
     * 
     * @param id Identificador do usuário
     * @return Objeto Usuario encontrado ou null
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/usuarioDAO/buscarUsuario.md
    public Usuario buscarUsuario(int id) throws Exception {
        return arqUsuarios.read(id);
    }

    /**
     * Inclui um novo usuário no sistema.
     * 
     * @param usuario Objeto Usuario a ser incluído
     * @return true se incluído com sucesso
     * @throws Exception Se houver erro na inclusão
     */
    // Explicado em docs/aux/usuarioDAO/incluirUsuario.md
    public boolean incluirUsuario(Usuario usuario) throws Exception {
        return arqUsuarios.create(usuario) > 0;
    }

    /**
     * Altera os dados de um usuário existente.
     * 
     * @param usuario Objeto com os dados atualizados
     * @return true se alterado com sucesso
     * @throws Exception Se houver erro na alteração
     */
    // Explicado em docs/aux/usuarioDAO/alterarUsuario.md
    public boolean alterarUsuario(Usuario usuario) throws Exception {
        return arqUsuarios.update(usuario);
    }

    /**
     * Exclui um usuário do sistema.
     * Verifica se o usuário possui tarefas ou categorias antes de excluir.
     * 
     * @param id Identificador do usuário a ser excluído
     * @return true se excluído com sucesso
     * @throws Exception Se o usuário não existir ou tiver dependências
     */
    // Explicado em docs/aux/usuarioDAO/excluirUsuario.md
    public boolean excluirUsuario(int id) throws Exception {
        // Verificar se o usuário existe
        Usuario usuario = buscarUsuario(id);
        if (usuario == null) {
            throw new Exception("Usuário não encontrado!");
        }
        
        // Verificar se o usuário tem tarefas
        int numTarefas = usuarioTarefasManager.contarTarefasDoUsuario(id);
        if (numTarefas > 0) {
            throw new Exception("Não é possível excluir usuário pois ele possui " + 
                               numTarefas + " tarefa(s)!");
        }
        
        // Verificar se o usuário tem categorias
        int numCategorias = usuarioCategoriasManager.contarCategoriasDoUsuario(id);
        if (numCategorias > 0) {
            throw new Exception("Não é possível excluir usuário pois ele possui " + 
                               numCategorias + " categoria(s)!");
        }
        
        // Se chegou aqui, pode excluir
        return arqUsuarios.delete(id);
    }
    
    /**
     * Retorna a lista de IDs das tarefas de um usuário.
     * 
     * @param idUsuario Identificador do usuário
     * @return Lista de IDs das tarefas
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/usuarioDAO/getTarefasDoUsuario.md
    public List<Integer> getTarefasDoUsuario(int idUsuario) throws Exception {
        return usuarioTarefasManager.buscarTarefasDoUsuario(idUsuario);
    }
    
    /**
     * Retorna a lista de IDs das categorias de um usuário.
     * 
     * @param idUsuario Identificador do usuário
     * @return Lista de IDs das categorias
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/usuarioDAO/getCategoriasDoUsuario.md
    public List<Integer> getCategoriasDoUsuario(int idUsuario) throws Exception {
        return usuarioCategoriasManager.buscarCategoriasDoUsuario(idUsuario);
    }
    
    /**
     * Conta quantas tarefas um usuário possui.
     * 
     * @param idUsuario Identificador do usuário
     * @return Número de tarefas do usuário
     * @throws Exception Se houver erro na contagem
     */
    // Explicado em docs/aux/usuarioDAO/contarTarefasDoUsuario.md
    public int contarTarefasDoUsuario(int idUsuario) throws Exception {
        return usuarioTarefasManager.contarTarefasDoUsuario(idUsuario);
    }
    
    /**
     * Conta quantas categorias um usuário possui.
     * 
     * @param idUsuario Identificador do usuário
     * @return Número de categorias do usuário
     * @throws Exception Se houver erro na contagem
     */
    // Explicado em docs/aux/usuarioDAO/contarCategoriasDoUsuario.md
    public int contarCategoriasDoUsuario(int idUsuario) throws Exception {
        return usuarioCategoriasManager.contarCategoriasDoUsuario(idUsuario);
    }
    
    /**
     * Lista todos os usuários cadastrados.
     * 
     * @return Lista com todos os usuários
     * @throws Exception Se houver erro na listagem
     */
    // Explicado em docs/aux/usuarioDAO/listarTodos.md
    public List<Usuario> listarTodos() throws Exception {
        return arqUsuarios.listAll();
    }
    
    /**
     * Fecha as conexões com os arquivos e gerenciadores.
     * 
     * @throws Exception Se houver erro no fechamento
     */
    // Explicado em docs/aux/usuarioDAO/close.md
    public void close() throws Exception {
        arqUsuarios.close();
        usuarioTarefasManager.close();
        usuarioCategoriasManager.close();
    }
}