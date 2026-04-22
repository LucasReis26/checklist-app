package com.checklist.demo.dao;

import java.util.ArrayList;
import java.util.List;
import com.checklist.demo.model.LogConclusao;
import com.checklist.demo.model.Tarefa;
import com.checklist.demo.persistence.ArquivoIndex;
import com.checklist.demo.persistence.RegistroListaLogs;

/**
 * Gerenciador do relacionamento 1:N entre Tarefa e LogConclusao.
 * Uma tarefa pode ter vários logs de conclusão (histórico).
 * 
 * Armazena a lista de IDs de logs para cada tarefa usando
 * um arquivo de índice com chave = idTarefa e valor = lista de ids dos logs.
 */
public class TarefaLogsManager {
    // Explicado em docs/aux/tarefaLogsManager/tarefaLogsManager.md
    private ArquivoIndex<RegistroListaLogs> arquivoRelacoes;
    
    /**
     * Construtor da classe TarefaLogsManager.
     * Inicializa o arquivo que armazena as relações.
     * 
     * @throws Exception Se houver erro na inicialização
     */
    // Explicado em docs/aux/tarefaLogsManager/construtor.md
    public TarefaLogsManager() throws Exception {
        arquivoRelacoes = new ArquivoIndex<>("tarefa_logs_rel", 
                                              RegistroListaLogs.class.getConstructor());
    }
    
    /**
     * Adiciona um log à lista de logs de uma tarefa.
     * 
     * @param idTarefa Identificador da tarefa
     * @param idLog Identificador do log a ser adicionado
     * @throws Exception Se houver erro na operação
     */
    // Explicado em docs/aux/tarefaLogsManager/adicionarLog.md
    public void adicionarLog(int idTarefa, int idLog) throws Exception {
        // Busca o registro existente para esta tarefa
        RegistroListaLogs registro = arquivoRelacoes.read(idTarefa);
        List<Integer> logs;
        
        if (registro == null) {
            logs = new ArrayList<>();
        } else {
            logs = registro.getLogs();
        }
        
        // Evita duplicação
        if (!logs.contains(idLog)) {
            logs.add(idLog);
            RegistroListaLogs novoRegistro = new RegistroListaLogs(idTarefa, logs);
            arquivoRelacoes.save(novoRegistro);
        }
    }
    
    /**
     * Remove um log da lista de logs de uma tarefa.
     * 
     * @param idTarefa Identificador da tarefa
     * @param idLog Identificador do log a ser removido
     * @throws Exception Se houver erro na operação
     */
    // Explicado em docs/aux/tarefaLogsManager/removerLog.md
    public void removerLog(int idTarefa, int idLog) throws Exception {
        RegistroListaLogs registro = arquivoRelacoes.read(idTarefa);
        if (registro != null) {
            List<Integer> logs = registro.getLogs();
            logs.remove(Integer.valueOf(idLog));
            
            if (logs.isEmpty()) {
                // Se não há mais logs, remove o registro
                arquivoRelacoes.delete(idTarefa);
            } else {
                // Atualiza a lista
                registro.setLogs(logs);
                arquivoRelacoes.update(registro);
            }
        }
    }
    
    /**
     * Busca todos os logs associados a uma tarefa.
     * 
     * @param idTarefa Identificador da tarefa
     * @return Lista de IDs dos logs da tarefa
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/tarefaLogsManager/buscarLogsDaTarefa.md
    public List<Integer> buscarLogsDaTarefa(int idTarefa) throws Exception {
        RegistroListaLogs registro = arquivoRelacoes.read(idTarefa);
        if (registro != null) {
            return registro.getLogs();
        }
        return new ArrayList<>();
    }
    
    /**
     * Busca o último log de conclusão de uma tarefa.
     * 
     * @param idTarefa Identificador da tarefa
     * @return Objeto LogConclusao do último log ou null
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/tarefaLogsManager/buscarUltimoLog.md
    public LogConclusao buscarUltimoLog(int idTarefa) throws Exception {
        List<Integer> logs = buscarLogsDaTarefa(idTarefa);
        if (!logs.isEmpty()) {
            int ultimoLogId = logs.get(logs.size() - 1);
            LogConclusaoDAO logDAO = new LogConclusaoDAO();
            return logDAO.buscarLog(ultimoLogId);
        }
        return null;
    }
    
    /**
     * Conta quantos logs estão associados a uma tarefa.
     * 
     * @param idTarefa Identificador da tarefa
     * @return Número de logs da tarefa
     * @throws Exception Se houver erro na contagem
     */
    // Explicado em docs/aux/tarefaLogsManager/contarLogsDaTarefa.md
    public int contarLogsDaTarefa(int idTarefa) throws Exception {
        return buscarLogsDaTarefa(idTarefa).size();
    }
    
    /**
     * Fecha a conexão com o arquivo de relações.
     * 
     * @throws Exception Se houver erro no fechamento
     */
    // Explicado em docs/aux/tarefaLogsManager/close.md
    public void close() throws Exception {
        arquivoRelacoes.close();
    }
}
