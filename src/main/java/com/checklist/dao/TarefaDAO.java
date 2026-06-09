package com.checklist.dao;

import com.checklist.manager.CategoriaTarefasManager;
import com.checklist.manager.TarefaLogsManager;
import com.checklist.manager.UsuarioTarefasManager;
import com.checklist.model.Categoria;
import com.checklist.model.LogConclusao;
import com.checklist.model.Tarefa;
import com.checklist.model.TarefaTag;
import com.checklist.model.Usuario;
import com.checklist.persistence.ArquivoIndex;
import java.util.ArrayList;
import java.util.List;

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
    private TarefaTagDAO tarefaTagDAO;
    private LogConclusaoDAO logConclusaoDAO;

    public TarefaDAO() throws Exception {
        this(new UsuarioDAO(), new CategoriaDAO(), new UsuarioTarefasManager(), 
             new CategoriaTarefasManager(), new TarefaLogsManager());
    }

    public TarefaDAO(UsuarioDAO udao, CategoriaDAO cdao, UsuarioTarefasManager utm, 
                    CategoriaTarefasManager ctm, TarefaLogsManager tlm) throws Exception {
        arqTarefas = ArquivoIndex.getInstance("tarefas", Tarefa.class.getConstructor());
        this.usuarioDAO = udao;
        this.categoriaDAO = cdao;
        this.usuarioTarefasManager = utm;
        this.categoriaTarefasManager = ctm;
        this.tarefaLogsManager = tlm;
    }

    public void setTarefaTagDAO(TarefaTagDAO ttdao) {
        this.tarefaTagDAO = ttdao;
    }
    
    public void setLogConclusaoDAO(LogConclusaoDAO lcdao) {
        this.logConclusaoDAO = lcdao;
    }

    /**
     * Obtém a instância do TarefaTagDAO.
     * 
     * @return Instância do TarefaTagDAO
     * @throws Exception Se não estiver injetado
     */
    // Explicado em docs/aux/tarefaDAO/getTarefaTagDAO.md
    private synchronized TarefaTagDAO getTarefaTagDAO() throws Exception {
        if (tarefaTagDAO == null) {
            // Lazy-wire se estiver em ambiente não-Spring (CLI/GUI)
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
    public synchronized Tarefa buscarTarefa(int id) throws Exception {
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
    public synchronized List<Tarefa> buscarTarefasPorUsuario(int idUser) {
        try {
            System.out.println("DEBUG: [TarefaDAO] Iniciando busca para usuário " + idUser);
            List<Integer> idsTarefas = usuarioTarefasManager.buscarTarefasDoUsuario(idUser);
            System.out.println("DEBUG: [TarefaDAO] IDs no índice de relações: " + idsTarefas);
            
            // Forçar scan físico se o índice estiver vazio
            if (idsTarefas.isEmpty()) {
                System.out.println("DEBUG: [TarefaDAO] Índice vazio! Iniciando scan físico de tarefas.db...");
                List<Tarefa> todas = listarTodas();
                System.out.println("DEBUG: [TarefaDAO] Total de tarefas físicas lidas: " + todas.size());
                
                for (Tarefa t : todas) {
                    System.out.println("  -> Analisando Tarefa ID: " + t.getId() + " | Dono no DB: " + t.getIdUser());
                    if (t.getIdUser() == idUser) {
                        System.out.println("  [!] Encontrada! Tentando re-vincular tarefa " + t.getId() + " ao usuário " + idUser);
                        try {
                            usuarioTarefasManager.adicionarTarefa(idUser, t.getId());
                        } catch (Exception e) { 
                            System.err.println("  [X] Falha ao re-vincular tarefa " + t.getId() + ": " + e.toString());
                            e.printStackTrace();
                        }
                    }
                }
                // Recarrega após a tentativa de cura
                idsTarefas = usuarioTarefasManager.buscarTarefasDoUsuario(idUser);
                System.out.println("DEBUG: [TarefaDAO] IDs após tentativa de cura: " + idsTarefas);
            }
            
            List<Tarefa> resultado = new ArrayList<>();
            for (Integer id : idsTarefas) {
                try {
                    Tarefa tarefa = buscarTarefa(id);
                    if (tarefa != null) {
                        resultado.add(tarefa);
                    }
                } catch (Exception e) { }
            }
            return resultado;
        } catch (Exception e) {
            System.err.println("DEBUG: [TarefaDAO] Erro grave: " + e.getMessage());
            // Fallback total
            try {
                List<Tarefa> todas = listarTodas();
                List<Tarefa> filtradas = new ArrayList<>();
                for (Tarefa t : todas) {
                    if (t.getIdUser() == idUser) filtradas.add(t);
                }
                return filtradas;
            } catch (Exception e2) {
                return new ArrayList<>();
            }
        }
    }

    /**
     * Busca todas as tarefas de uma categoria.
     * Utiliza o CategoriaTarefasManager (Hash Extensível).
     * 
     * @param idCategoria Identificador da categoria
     * @return Lista de tarefas da categoria
     */
    // Explicado em docs/aux/tarefaDAO/buscarTarefasPorCategoria.md
    public synchronized List<Tarefa> buscarTarefasPorCategoria(int idCategoria) {
        try {
            List<Tarefa> resultado = new ArrayList<>();
            List<Integer> idsTarefas = categoriaTarefasManager.buscarTarefasDaCategoria(idCategoria);
            
            for (Integer id : idsTarefas) {
                try {
                    Tarefa tarefa = buscarTarefa(id);
                    if (tarefa != null) {
                        resultado.add(tarefa);
                    }
                } catch (Exception e) { }
            }
            return resultado;
        } catch (Exception e) {
            return new ArrayList<>();
        }
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
    public synchronized List<Tarefa> buscarTarefasPorStatus(String status) throws Exception {
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
    public synchronized boolean incluirTarefa(Tarefa tarefa) throws Exception {
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
    public synchronized boolean alterarTarefa(Tarefa tarefa) throws Exception {
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
    public synchronized boolean excluirTarefa(int id) throws Exception {
        Tarefa tarefa = buscarTarefa(id);
        if (tarefa == null) {
            return false;
        }
        
        // Remover logs associados automaticamente
        if (logConclusaoDAO != null) {
            List<Integer> logs = tarefaLogsManager.buscarLogsDaTarefa(id);
            for (Integer idLog : logs) {
                logConclusaoDAO.excluirLog(idLog);
            }
        }
        tarefaLogsManager.removerLogsDaTarefa(id);
        
        // Remover tags associadas automaticamente
        getTarefaTagDAO().excluirTagsPorTarefa(id);
        
        // Remover dos relacionamentos de usuário e categoria
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
    public synchronized boolean concluirTarefa(int id, String resumoTags) throws Exception {
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
        
        if (logConclusaoDAO != null) {
            boolean logIncluido = logConclusaoDAO.incluirLog(log);
            if (logIncluido) {
                tarefaLogsManager.adicionarLog(id, log.getId());
            }
        }
        
        return true;
    }
    
    /**
     * Busca o último log de conclusão de uma tarefa.
     * 
     * @param idTarefa Identificador da tarefa
     * @return Último log de conclusão ou null
     * @throws Exception Se houver erro na busca
     */
    public synchronized LogConclusao buscarUltimoLogDaTarefa(int idTarefa) throws Exception {
        int idLog = tarefaLogsManager.buscarUltimoLogId(idTarefa);
        if (idLog != -1 && logConclusaoDAO != null) {
            return logConclusaoDAO.buscarLog(idLog);
        }
        return null;
    }
    
    /**
     * Retorna a lista de IDs dos logs de uma tarefa.
     * 
     * @param idTarefa Identificador da tarefa
     * @return Lista de IDs dos logs
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/tarefaDAO/getLogsDaTarefa.md
    public synchronized List<Integer> getLogsDaTarefa(int idTarefa) throws Exception {
        return tarefaLogsManager.buscarLogsDaTarefa(idTarefa);
    }
    
    /**
     * Lista todas as tarefas cadastradas de forma ordenada pelo ID.
     * Utiliza a travessia dos nós folha da Árvore B+.
     * 
     * @return Lista de tarefas ordenadas
     * @throws Exception Se houver erro na listagem
     */
    public synchronized List<Tarefa> listarOrdenado() throws Exception {
        return arqTarefas.listInOrder();
    }
    
    /**
     * Lista todas as tarefas cadastradas.
     * 
     * @return Lista com todas as tarefas
     * @throws Exception Se houver erro na listagem
     */
    // Explicado em docs/aux/tarefaDAO/listarTodas.md
    public synchronized List<Tarefa> listarTodas() throws Exception {
        return arqTarefas.listAll();
    }
    
    /**
     * Fecha as conexões com os arquivos e gerenciadores.
     * 
     * @throws Exception Se houver erro no fechamento
     */
    // Explicado em docs/aux/tarefaDAO/close.md
    public synchronized void close() throws Exception {
        arqTarefas.close();
        usuarioTarefasManager.close();
        categoriaTarefasManager.close();
        tarefaLogsManager.close();
        if (tarefaTagDAO != null) {
            tarefaTagDAO.close();
        }
    }
}
