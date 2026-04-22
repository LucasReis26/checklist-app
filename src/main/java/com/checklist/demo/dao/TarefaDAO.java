package com.checklist.demo.dao;

import java.util.ArrayList;
import java.util.List;
import com.checklist.demo.model.Categoria;
import com.checklist.demo.model.LogConclusao;
import com.checklist.demo.model.Tarefa;
import com.checklist.demo.model.TarefaTag;
import com.checklist.demo.model.Usuario;
import com.checklist.demo.persistence.ArquivoIndex;

/**
 * Classe de acesso a dados (DAO) para a entidade Tarefa.
 * Gerencia as operações de CRUD e os relacionamentos da tarefa com:
 * - Usuário (N:1)
 * - Categoria (N:1)
 * - Tags (N:N via TarefaTag)
 * - Logs de conclusão (1:N)
 */
public class TarefaDAO {
    // Explicado em docs/aux/tarefaDAO/tarefaDAO.md
    private final ArquivoIndex<Tarefa> arqTarefas;
    private final UsuarioDAO usuarioDAO;
    private final CategoriaDAO categoriaDAO;
    private final UsuarioTarefasManager usuarioTarefasManager;
    private final CategoriaTarefasManager categoriaTarefasManager;
    private final TarefaLogsManager tarefaLogsManager;
    private TarefaTagDAO tarefaTagDAO;  // Inicializado sob demanda para evitar recursão

    /**
     * Construtor da classe TarefaDAO.
     * Inicializa o arquivo com índice e os gerenciadores de relacionamento.
     * 
     * @throws Exception Se houver erro na inicialização
     */
    // Explicado em docs/aux/tarefaDAO/construtor.md
    public TarefaDAO() throws Exception {
        arqTarefas = new ArquivoIndex<>("tarefas", Tarefa.class.getConstructor());
        usuarioDAO = new UsuarioDAO();
        categoriaDAO = new CategoriaDAO();
        usuarioTarefasManager = new UsuarioTarefasManager();
        categoriaTarefasManager = new CategoriaTarefasManager();
        tarefaLogsManager = new TarefaLogsManager();
        // NÃO instanciar TarefaTagDAO aqui - será instanciado quando necessário
    }
    
    /**
     * Obtém a instância do TarefaTagDAO (lazy initialization).
     * 
     * @return Instância do TarefaTagDAO
     * @throws Exception Se houver erro na criação
     */
    // Explicado em docs/aux/tarefaDAO/getTarefaTagDAO.md
    private TarefaTagDAO getTarefaTagDAO() throws Exception {
        if (tarefaTagDAO == null) {
            tarefaTagDAO = new TarefaTagDAO();
        }
        return tarefaTagDAO;
    }

    /**
     * Busca uma tarefa pelo ID.
     * 
     * @param id Identificador da tarefa
     * @return Objeto Tarefa encontrado ou null
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/tarefaDAO/buscarTarefa.md
    public Tarefa buscarTarefa(int id) throws Exception {
        return arqTarefas.read(id);
    }

    /**
     * Busca todas as tarefas de um usuário.
     * Utiliza o UsuarioTarefasManager (Hash Extensível).
     * 
     * @param idUser Identificador do usuário
     * @return Lista de tarefas do usuário
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/tarefaDAO/buscarTarefasPorUsuario.md
    public List<Tarefa> buscarTarefasPorUsuario(int idUser) throws Exception {
        List<Tarefa> resultado = new ArrayList<>();
        List<Integer> idsTarefas = usuarioTarefasManager.buscarTarefasDoUsuario(idUser);
        
        for (Integer id : idsTarefas) {
            Tarefa tarefa = buscarTarefa(id);
            if (tarefa != null) {
                resultado.add(tarefa);
            }
        }
        
        return resultado;
    }

    /**
     * Busca todas as tarefas de uma categoria.
     * Utiliza o CategoriaTarefasManager (Hash Extensível).
     * 
     * @param idCategoria Identificador da categoria
     * @return Lista de tarefas da categoria
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/tarefaDAO/buscarTarefasPorCategoria.md
    public List<Tarefa> buscarTarefasPorCategoria(int idCategoria) throws Exception {
        List<Tarefa> resultado = new ArrayList<>();
        List<Integer> idsTarefas = categoriaTarefasManager.buscarTarefasDaCategoria(idCategoria);
        
        for (Integer id : idsTarefas) {
            Tarefa tarefa = buscarTarefa(id);
            if (tarefa != null) {
                resultado.add(tarefa);
            }
        }
        
        return resultado;
    }

    /**
     * Busca tarefas por status (pendente/concluida).
     * Utiliza scan sequencial (não indexado).
     * 
     * @param status Status desejado ("pendente" ou "concluida")
     * @return Lista de tarefas com o status especificado
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/tarefaDAO/buscarTarefasPorStatus.md
    public List<Tarefa> buscarTarefasPorStatus(String status) throws Exception {
        List<Tarefa> resultado = new ArrayList<>();
        List<Tarefa> todas = listarTodas();
        
        for (Tarefa tarefa : todas) {
            if (tarefa.getStatus().equalsIgnoreCase(status)) {
                resultado.add(tarefa);
            }
        }
        
        return resultado;
    }

    /**
     * Inclui uma nova tarefa no sistema.
     * Valida usuário e categoria antes de criar.
     * 
     * @param tarefa Objeto Tarefa a ser incluído
     * @return true se incluída com sucesso
     * @throws Exception Se usuário ou categoria não existirem
     */
    // Explicado em docs/aux/tarefaDAO/incluirTarefa.md
    public boolean incluirTarefa(Tarefa tarefa) throws Exception {
        // Validação de integridade referencial: usuário deve existir
        Usuario usuario = usuarioDAO.buscarUsuario(tarefa.getIdUser());
        if (usuario == null) {
            throw new Exception("Usuário com ID " + tarefa.getIdUser() + " não encontrado!");
        }
        
        // Validação: categoria deve existir e pertencer ao usuário
        if (tarefa.getIdCategoria() > 0) {
            Categoria categoria = categoriaDAO.buscarCategoria(tarefa.getIdCategoria());
            if (categoria == null) {
                throw new Exception("Categoria com ID " + tarefa.getIdCategoria() + " não encontrada!");
            }
            if (categoria.getIdUser() != tarefa.getIdUser()) {
                throw new Exception("A categoria informada não pertence ao usuário!");
            }
        }
        
        int id = arqTarefas.create(tarefa);
        if (id <= 0) {
            return false;
        }
        
        // Atualiza relacionamentos
        usuarioTarefasManager.adicionarTarefa(tarefa.getIdUser(), id);
        if (tarefa.getIdCategoria() > 0) {
            categoriaTarefasManager.adicionarTarefa(tarefa.getIdCategoria(), id);
        }
        
        return true;
    }

    /**
     * Altera os dados de uma tarefa existente.
     * Atualiza os relacionamentos se usuário ou categoria forem alterados.
     * 
     * @param tarefa Objeto com os dados atualizados
     * @return true se alterada com sucesso
     * @throws Exception Se houver erro na alteração
     */
    // Explicado em docs/aux/tarefaDAO/alterarTarefa.md
    public boolean alterarTarefa(Tarefa tarefa) throws Exception {
        Tarefa existente = buscarTarefa(tarefa.getId());
        if (existente == null) {
            throw new Exception("Tarefa não encontrada!");
        }
        
        // Se alterou o usuário, atualizar relacionamentos
        if (existente.getIdUser() != tarefa.getIdUser()) {
            Usuario usuario = usuarioDAO.buscarUsuario(tarefa.getIdUser());
            if (usuario == null) {
                throw new Exception("Usuário com ID " + tarefa.getIdUser() + " não encontrado!");
            }
            
            usuarioTarefasManager.removerTarefa(existente.getIdUser(), tarefa.getId());
            usuarioTarefasManager.adicionarTarefa(tarefa.getIdUser(), tarefa.getId());
        }
        
        // Se alterou a categoria, atualizar relacionamentos
        if (existente.getIdCategoria() != tarefa.getIdCategoria()) {
            if (existente.getIdCategoria() > 0) {
                categoriaTarefasManager.removerTarefa(existente.getIdCategoria(), tarefa.getId());
            }
            if (tarefa.getIdCategoria() > 0) {
                Categoria categoria = categoriaDAO.buscarCategoria(tarefa.getIdCategoria());
                if (categoria == null) {
                    throw new Exception("Categoria com ID " + tarefa.getIdCategoria() + " não encontrada!");
                }
                if (categoria.getIdUser() != tarefa.getIdUser()) {
                    throw new Exception("A categoria informada não pertence ao usuário!");
                }
                categoriaTarefasManager.adicionarTarefa(tarefa.getIdCategoria(), tarefa.getId());
            }
        }
        
        return arqTarefas.update(tarefa);
    }

    /**
     * Exclui uma tarefa do sistema.
     * Verifica se existem logs ou tags associados antes de excluir.
     * 
     * @param id Identificador da tarefa a ser excluída
     * @return true se excluída com sucesso
     * @throws Exception Se houver logs ou tags associados
     */
    // Explicado em docs/aux/tarefaDAO/excluirTarefa.md
    public boolean excluirTarefa(int id) throws Exception {
        Tarefa tarefa = buscarTarefa(id);
        if (tarefa == null) {
            return false;
        }
        
        // Verificar logs associados
        int numLogs = tarefaLogsManager.contarLogsDaTarefa(id);
        if (numLogs > 0) {
            throw new Exception("Não é possível excluir tarefa pois existem " + 
                               numLogs + " logs de conclusão associados!");
        }
        
        // Verificar tags associadas
        List<TarefaTag> tags = getTarefaTagDAO().buscarTagsPorTarefa(id);
        if (!tags.isEmpty()) {
            throw new Exception("Não é possível excluir tarefa pois existem " + 
                               tags.size() + " tags associadas!");
        }
        
        // Remover dos relacionamentos
        usuarioTarefasManager.removerTarefa(tarefa.getIdUser(), id);
        if (tarefa.getIdCategoria() > 0) {
            categoriaTarefasManager.removerTarefa(tarefa.getIdCategoria(), id);
        }
        
        return arqTarefas.delete(id);
    }
    
    /**
     * Conclui uma tarefa.
     * Atualiza o status para "concluida" e cria um log de conclusão.
     * 
     * @param id Identificador da tarefa
     * @param resumoTags Resumo das tags associadas (opcional)
     * @return true se concluída com sucesso
     * @throws Exception Se a tarefa não existir ou já estiver concluída
     */
    // Explicado em docs/aux/tarefaDAO/concluirTarefa.md
    public boolean concluirTarefa(int id, String resumoTags) throws Exception {
        Tarefa tarefa = buscarTarefa(id);
        if (tarefa == null) {
            throw new Exception("Tarefa não encontrada!");
        }
        
        if ("concluida".equals(tarefa.getStatus())) {
            throw new Exception("Tarefa já está concluída!");
        }
        
        tarefa.setStatus("concluida");
        if (!alterarTarefa(tarefa)) {
            return false;
        }
        
        LogConclusao log = new LogConclusao(
            id,
            new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()),
            resumoTags
        );
        
        LogConclusaoDAO logDAO = new LogConclusaoDAO();
        boolean logIncluido = logDAO.incluirLog(log);
        
        if (logIncluido) {
            tarefaLogsManager.adicionarLog(id, log.getId());
        }
        
        return logIncluido;
    }
    
    /**
     * Busca o último log de conclusão de uma tarefa.
     * 
     * @param idTarefa Identificador da tarefa
     * @return Último log de conclusão ou null
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/tarefaDAO/buscarUltimoLogDaTarefa.md
    public LogConclusao buscarUltimoLogDaTarefa(int idTarefa) throws Exception {
        return tarefaLogsManager.buscarUltimoLog(idTarefa);
    }
    
    /**
     * Retorna a lista de IDs dos logs de uma tarefa.
     * 
     * @param idTarefa Identificador da tarefa
     * @return Lista de IDs dos logs
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/tarefaDAO/getLogsDaTarefa.md
    public List<Integer> getLogsDaTarefa(int idTarefa) throws Exception {
        return tarefaLogsManager.buscarLogsDaTarefa(idTarefa);
    }
    
    /**
     * Lista todas as tarefas cadastradas.
     * 
     * @return Lista com todas as tarefas
     * @throws Exception Se houver erro na listagem
     */
    // Explicado em docs/aux/tarefaDAO/listarTodas.md
    public List<Tarefa> listarTodas() throws Exception {
        return arqTarefas.listAll();
    }
    
    /**
     * Fecha as conexões com os arquivos e gerenciadores.
     * 
     * @throws Exception Se houver erro no fechamento
     */
    // Explicado em docs/aux/tarefaDAO/close.md
    public void close() throws Exception {
        arqTarefas.close();
        usuarioTarefasManager.close();
        categoriaTarefasManager.close();
        tarefaLogsManager.close();
        if (tarefaTagDAO != null) {
            tarefaTagDAO.close();
        }
    }
}
