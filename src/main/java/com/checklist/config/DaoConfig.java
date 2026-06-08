package com.checklist.config;

import com.checklist.dao.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaoConfig {

    @Bean
    public UsuarioDAO usuarioDAO() throws Exception {
        return new UsuarioDAO();
    }

    @Bean
    public CategoriaDAO categoriaDAO() throws Exception {
        return new CategoriaDAO();
    }

    @Bean
    public TarefaDAO tarefaDAO() throws Exception {
        return new TarefaDAO();
    }

    @Bean
    public TagDAO tagDAO() throws Exception {
        return new TagDAO();
    }

    @Bean
    public LogConclusaoDAO logConclusaoDAO() throws Exception {
        return new LogConclusaoDAO();
    }

    @Bean
    public TarefaTagDAO tarefaTagDAO() throws Exception {
        return new TarefaTagDAO();
    }
}
