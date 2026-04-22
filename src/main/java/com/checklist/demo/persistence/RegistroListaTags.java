package com.checklist.demo.persistence;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe de registro para armazenar listas de tags associadas a uma chave.
 * Utilizada pelo HashExtensivel para gerenciar relacionamentos N:N entre tarefas e tags.
 * 
 * Formato de armazenamento: [id (4 bytes)] [quantidade (4 bytes)] [tag1 (4 bytes)] [tag2 (4 bytes)] ...
 */
public class RegistroListaTags implements Registro {
    // Explicado em docs/aux/registroListaTags/registroListaTags.md
    private int id;
    private List<Integer> tags;
    
    /**
     * Construtor padrão.
     * Inicializa com lista vazia.
     */
    // Explicado em docs/aux/registroListaTags/construtorPadrao.md
    public RegistroListaTags() {
        this(-1, new ArrayList<>());
    }
    
    /**
     * Construtor com parâmetros.
     * 
     * @param id Chave identificadora (geralmente ID da tarefa)
     * @param tags Lista de IDs das tags associadas
     */
    // Explicado em docs/aux/registroListaTags/construtor.md
    public RegistroListaTags(int id, List<Integer> tags) {
        this.id = id;
        this.tags = tags;
    }
    
    /**
     * Define o ID do registro.
     * 
     * @param id Novo ID
     */
    @Override
    // Explicado em docs/aux/registroListaTags/setId.md
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Retorna o ID do registro.
     * 
     * @return ID do registro
     */
    @Override
    // Explicado em docs/aux/registroListaTags/getId.md
    public int getId() {
        return id;
    }
    
    /**
     * Retorna a lista de IDs das tags.
     * 
     * @return Lista de tags
     */
    // Explicado em docs/aux/registroListaTags/getTags.md
    public List<Integer> getTags() {
        return tags;
    }
    
    /**
     * Define a lista de IDs das tags.
     * 
     * @param tags Nova lista de tags
     */
    // Explicado em docs/aux/registroListaTags/setTags.md
    public void setTags(List<Integer> tags) {
        this.tags = tags;
    }
    
    /**
     * Converte o objeto em um array de bytes para persistência.
     * 
     * @return Array de bytes representando o objeto
     * @throws IOException Se houver erro na conversão
     */
    @Override
    // Explicado em docs/aux/registroListaTags/toByteArray.md
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(id);
        dos.writeInt(tags.size());
        for (Integer tagId : tags) {
            dos.writeInt(tagId);
        }
        return baos.toByteArray();
    }
    
    /**
     * Reconstrói um objeto a partir de um array de bytes.
     * 
     * @param b Array de bytes contendo os dados
     * @throws IOException Se houver erro na conversão
     */
    @Override
    // Explicado em docs/aux/registroListaTags/fromByteArray.md
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        int tamanho = dis.readInt();
        this.tags = new ArrayList<>();
        for (int i = 0; i < tamanho; i++) {
            tags.add(dis.readInt());
        }
    }
}
