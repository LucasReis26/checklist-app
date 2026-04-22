package com.checklist.demo;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe de acesso a dados (DAO) para a entidade LogConclusao.
 * Gerencia as operações de CRUD e os relacionamentos do log.
 */
public class LogConclusaoDAO {
    // Explicado em docs/aux/logConclusaoDAO/logConclusaoDAO.md
    private final ArquivoIndex<LogConclusao> arqLogs;
    private final TarefaDAO tarefaDAO;

    /**
     * Construtor da classe LogConclusaoDAO.
     * Inicializa o arquivo com índice e o DAO de tarefas.
     * 
     * @throws Exception Se houver erro na inicialização
     */
    // Explicado em docs/aux/logConclusaoDAO/construtor.md
    public LogConclusaoDAO() throws Exception {
        arqLogs = new ArquivoIndex<>("logs_conclusao", LogConclusao.class.getConstructor());
        tarefaDAO = new TarefaDAO();
    }

    /**
     * Busca um log pelo ID.
     * 
     * @param id Identificador do log
     * @return Objeto LogConclusao encontrado ou null
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/logConclusaoDAO/buscarLog.md
    public LogConclusao buscarLog(int id) throws Exception {
        return arqLogs.read(id);
    }
    
    /**
     * Busca todos os logs associados a uma tarefa.
     * Realiza busca sequencial em todos os logs.
     * 
     * @param idTarefa Identificador da tarefa
     * @return Lista de logs da tarefa
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/logConclusaoDAO/buscarLogsPorTarefa.md
    public List<LogConclusao> buscarLogsPorTarefa(int idTarefa) throws Exception {
        List<LogConclusao> resultado = new ArrayList<>();
        List<LogConclusao> todos = arqLogs.listAll();
        
        for (LogConclusao log : todos) {
            if (log.getIdTarefa() == idTarefa) {
                resultado.add(log);
            }
        }
        
        return resultado;
    }

    /**
     * Inclui um novo log no sistema.
     * Verifica se a tarefa existe e está concluída.
     * 
     * @param log Objeto LogConclusao a ser incluído
     * @return true se incluído com sucesso
     * @throws Exception Se a tarefa não existir ou não estiver concluída
     */
    // Explicado em docs/aux/logConclusaoDAO/incluirLog.md
    public boolean incluirLog(LogConclusao log) throws Exception {
        // Validação de integridade referencial: tarefa deve existir
        Tarefa tarefa = tarefaDAO.buscarTarefa(log.getIdTarefa());
        if (tarefa == null) {
            throw new Exception("Tarefa com ID " + log.getIdTarefa() + " não encontrada!");
        }
        
        // Validação: só pode criar log para tarefa concluída
        if (!"concluida".equals(tarefa.getStatus())) {
            throw new Exception("Não é possível criar log para tarefa não concluída!");
        }
        
        return arqLogs.create(log) > 0;
    }

    /**
     * Altera os dados de um log existente.
     * Verifica a integridade referencial se o ID da tarefa for alterado.
     * 
     * @param log Objeto com os dados atualizados
     * @return true se alterado com sucesso
     * @throws Exception Se houver erro na alteração
     */
    // Explicado em docs/aux/logConclusaoDAO/alterarLog.md
    public boolean alterarLog(LogConclusao log) throws Exception {
        LogConclusao existente = buscarLog(log.getId());
        if (existente != null && existente.getIdTarefa() != log.getIdTarefa()) {
            Tarefa tarefa = tarefaDAO.buscarTarefa(log.getIdTarefa());
            if (tarefa == null) {
                throw new Exception("Tarefa com ID " + log.getIdTarefa() + " não encontrada!");
            }
            if (!"concluida".equals(tarefa.getStatus())) {
                throw new Exception("Não é possível associar log a tarefa não concluída!");
            }
        }
        return arqLogs.update(log);
    }

    /**
     * Exclui um log do sistema.
     * 
     * @param id Identificador do log a ser excluído
     * @return true se excluído com sucesso
     * @throws Exception Se houver erro na exclusão
     */
    // Explicado em docs/aux/logConclusaoDAO/excluirLog.md
    public boolean excluirLog(int id) throws Exception {
        return arqLogs.delete(id);
    }
    
    /**
     * Lista todos os logs cadastrados.
     * 
     * @return Lista com todos os logs
     * @throws Exception Se houver erro na listagem
     */
    // Explicado em docs/aux/logConclusaoDAO/listarTodos.md
    public List<LogConclusao> listarTodos() throws Exception {
        return arqLogs.listAll();
    }
    
    /**
     * Fecha a conexão com o arquivo de logs.
     * 
     * @throws Exception Se houver erro no fechamento
     */
    // Explicado em docs/aux/logConclusaoDAO/close.md
    public void close() throws Exception {
        arqLogs.close();
    }
}