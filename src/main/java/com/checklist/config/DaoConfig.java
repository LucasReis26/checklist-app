package com.checklist.config;

import com.checklist.dao.*;
import com.checklist.manager.*;
import com.checklist.persistence.HashIndexTagTarefas;
import com.checklist.persistence.HashIndexTarefaTags;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaoConfig {

    @Bean
    public UsuarioTarefasManager usuarioTarefasManager() throws Exception {
        return new UsuarioTarefasManager();
    }

    @Bean
    public UsuarioCategoriasManager usuarioCategoriasManager() throws Exception {
        return new UsuarioCategoriasManager();
    }

    @Bean
    public CategoriaTarefasManager categoriaTarefasManager() throws Exception {
        return new CategoriaTarefasManager();
    }

    @Bean
    public TarefaLogsManager tarefaLogsManager() throws Exception {
        return new TarefaLogsManager();
    }

    @Bean
    public UsuarioDAO usuarioDAO(UsuarioTarefasManager utm, UsuarioCategoriasManager ucm) throws Exception {
        return new UsuarioDAO(utm, ucm);
    }

    @Bean
    public CategoriaDAO categoriaDAO(UsuarioDAO udao, CategoriaTarefasManager ctm) throws Exception {
        return new CategoriaDAO(udao, ctm);
    }

    @Bean
    public TarefaDAO tarefaDAO(UsuarioDAO udao, CategoriaDAO cdao, UsuarioTarefasManager utm, 
                              CategoriaTarefasManager ctm, TarefaLogsManager tlm, TarefaTagDAO ttdao) throws Exception {
        TarefaDAO tdao = new TarefaDAO(udao, cdao, utm, ctm, tlm);
        tdao.setTarefaTagDAO(ttdao);
        cdao.setTarefaDAO(tdao); // Resolve circular dependency
        return tdao;
    }

    @Bean
    public TagDAO tagDAO(TarefaTagDAO ttdao) throws Exception {
        TagDAO tdao = new TagDAO();
        tdao.setTarefaTagDAO(ttdao);
        return tdao;
    }

    @Bean
    public LogConclusaoDAO logConclusaoDAO(TarefaDAO tdao) throws Exception {
        return new LogConclusaoDAO(tdao);
    }

    @Bean
    public HashIndexTarefaTags hashIndexTarefaTags() throws Exception {
        return new HashIndexTarefaTags();
    }

    @Bean
    public HashIndexTagTarefas hashIndexTagTarefas() throws Exception {
        return new HashIndexTagTarefas();
    }

    @Bean
    public TarefaTagDAO tarefaTagDAO(HashIndexTarefaTags itags, HashIndexTagTarefas itarefas) throws Exception {
        return new TarefaTagDAO(itags, itarefas);
    }
}
