package com.checklist.demo.config;

import com.checklist.demo.dao.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataConfig {

    // --- Managers (Arquivos de Relacionamento) ---

    @Bean(destroyMethod = "close")
    public UsuarioTarefasManager usuarioTarefasManager() throws Exception {
        return new UsuarioTarefasManager();
    }

    @Bean(destroyMethod = "close")
    public UsuarioCategoriasManager usuarioCategoriasManager() throws Exception {
        return new UsuarioCategoriasManager();
    }

    @Bean(destroyMethod = "close")
    public CategoriaTarefasManager categoriaTarefasManager() throws Exception {
        return new CategoriaTarefasManager();
    }

    @Bean(destroyMethod = "close")
    public TarefaLogsManager tarefaLogsManager() throws Exception {
        return new TarefaLogsManager();
    }

    // --- DAOs ---

    @Bean(destroyMethod = "close")
    public LogConclusaoDAO logConclusaoDAO() throws Exception {
        return new LogConclusaoDAO();
    }

    @Bean(destroyMethod = "close")
    public TarefaTagDAO tarefaTagDAO() throws Exception {
        return new TarefaTagDAO();
    }

    @Bean(destroyMethod = "close")
    public UsuarioDAO usuarioDAO(UsuarioTarefasManager utm, UsuarioCategoriasManager ucm) throws Exception {
        return new UsuarioDAO(utm, ucm);
    }

    @Bean(destroyMethod = "close")
    public TagDAO tagDAO(TarefaTagDAO ttdao) throws Exception {
        return new TagDAO(ttdao);
    }

    @Bean(destroyMethod = "close")
    public CategoriaDAO categoriaDAO(UsuarioDAO udao, CategoriaTarefasManager ctm) throws Exception {
        return new CategoriaDAO(udao, ctm);
    }

    @Bean(destroyMethod = "close")
    public TarefaDAO tarefaDAO(UsuarioDAO udao, CategoriaDAO cdao, 
                               UsuarioTarefasManager utm, CategoriaTarefasManager ctm,
                               TarefaLogsManager tlm, TarefaTagDAO ttdao,
                               LogConclusaoDAO lcdao) throws Exception {
        return new TarefaDAO(udao, cdao, utm, ctm, tlm, ttdao, lcdao);
    }
}
