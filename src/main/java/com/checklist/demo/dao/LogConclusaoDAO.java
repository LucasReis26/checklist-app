package com.checklist.demo.dao;

import java.util.ArrayList;
import java.util.List;
import com.checklist.demo.model.LogConclusao;
import com.checklist.demo.persistence.ArquivoIndex;

public class LogConclusaoDAO {
    private final ArquivoIndex<LogConclusao> arqLogs;

    public LogConclusaoDAO() throws Exception {
        arqLogs = new ArquivoIndex<>("logs_conclusao", LogConclusao.class.getConstructor());
    }

    public LogConclusao buscarLog(int id) throws Exception {
        return arqLogs.read(id);
    }
    
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

    public boolean incluirLog(LogConclusao log) throws Exception {
        return arqLogs.create(log) > 0;
    }

    public boolean alterarLog(LogConclusao log) throws Exception {
        return arqLogs.update(log);
    }

    public boolean excluirLog(int id) throws Exception {
        return arqLogs.delete(id);
    }
    
    public List<LogConclusao> listarTodos() throws Exception {
        return arqLogs.listAll();
    }
    
    public void close() throws Exception {
        arqLogs.close();
    }
}
