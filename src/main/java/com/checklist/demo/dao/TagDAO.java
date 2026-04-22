package com.checklist.demo.dao;

import java.util.List;
import com.checklist.demo.model.Tag;
import com.checklist.demo.model.TarefaTag;
import com.checklist.demo.persistence.ArquivoIndex;

public class TagDAO {
    private final ArquivoIndex<Tag> arqTags;
    private final TarefaTagDAO tarefaTagDAO;

    public TagDAO(TarefaTagDAO tarefaTagDAO) throws Exception {
        this.arqTags = new ArquivoIndex<>("tags", Tag.class.getConstructor());
        this.tarefaTagDAO = tarefaTagDAO;
    }

    public Tag buscarTag(int id) throws Exception {
        return arqTags.read(id);
    }

    public boolean incluirTag(Tag tag) throws Exception {
        return arqTags.create(tag) > 0;
    }

    public boolean alterarTag(Tag tag) throws Exception {
        Tag existente = buscarTag(tag.getId());
        if (existente == null) {
            throw new Exception("Tag não encontrada para alteração!");
        }
        return arqTags.update(tag);
    }

    public boolean excluirTag(int id) throws Exception {
        Tag existente = buscarTag(id);
        if (existente == null) {
            throw new Exception("Tag não encontrada!");
        }
        
        // Verifica se existem tarefas usando esta tag
        List<TarefaTag> tarefas = tarefaTagDAO.buscarTarefasPorTag(id);
        
        if (!tarefas.isEmpty()) {
            throw new Exception("Não é possível excluir tag pois existem tarefas associadas a ela!");
        }
        return arqTags.delete(id);
    }
    
    public List<Tag> listarTodas() throws Exception {
        return arqTags.listAll();
    }
    
    public void close() throws Exception {
        arqTags.close();
    }
}
