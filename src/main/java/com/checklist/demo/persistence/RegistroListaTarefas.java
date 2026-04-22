package com.checklist.demo.persistence;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import com.checklist.demo.dao.CategoriaTarefasManager;
import com.checklist.demo.dao.UsuarioTarefasManager;

/**
 * Classe de registro para armazenar listas de tarefas associadas a uma chave.
 * Utilizada pelos gerenciadores de relacionamento (UsuarioTarefasManager, CategoriaTarefasManager).
 * 
 * Formato de armazenamento: [id (4 bytes)] [quantidade (4 bytes)] [tarefa1 (4 bytes)] [tarefa2 (4 bytes)] ...
 */
public class RegistroListaTarefas implements Registro {
    // Explicado em docs/aux/registroListaTarefas/registroListaTarefas.md
    private int id;
    private List<Integer> tarefas;
    
    /**
     * Construtor padrão.
     * Inicializa com lista vazia.
     */
    // Explicado em docs/aux/registroListaTarefas/construtorPadrao.md
    public RegistroListaTarefas() {
        this(-1, new ArrayList<>());
    }
    
    /**
     * Construtor com parâmetros.
     * 
     * @param id Chave identificadora (ID do usuário ou categoria)
     * @param tarefas Lista de IDs das tarefas associadas
     */
    // Explicado em docs/aux/registroListaTarefas/construtor.md
    public RegistroListaTarefas(int id, List<Integer> tarefas) {
        this.id = id;
        this.tarefas = tarefas;
    }
    
    /**
     * Define o ID do registro.
     * 
     * @param id Novo ID
     */
    @Override
    // Explicado em docs/aux/registroListaTarefas/setId.md
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Retorna o ID do registro.
     * 
     * @return ID do registro
     */
    @Override
    // Explicado em docs/aux/registroListaTarefas/getId.md
    public int getId() {
        return id;
    }
    
    /**
     * Retorna a lista de IDs das tarefas.
     * 
     * @return Lista de tarefas
     */
    // Explicado em docs/aux/registroListaTarefas/getTarefas.md
    public List<Integer> getTarefas() {
        return tarefas;
    }
    
    /**
     * Define a lista de IDs das tarefas.
     * 
     * @param tarefas Nova lista de tarefas
     */
    // Explicado em docs/aux/registroListaTarefas/setTarefas.md
    public void setTarefas(List<Integer> tarefas) {
        this.tarefas = tarefas;
    }
    
    /**
     * Converte o objeto em um array de bytes para persistência.
     * 
     * @return Array de bytes representando o objeto
     * @throws IOException Se houver erro na conversão
     */
    @Override
    // Explicado em docs/aux/registroListaTarefas/toByteArray.md
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(id);
        dos.writeInt(tarefas.size());
        for (Integer tarefaId : tarefas) {
            dos.writeInt(tarefaId);
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
    // Explicado em docs/aux/registroListaTarefas/fromByteArray.md
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        int tamanho = dis.readInt();
        this.tarefas = new ArrayList<>();
        for (int i = 0; i < tamanho; i++) {
            tarefas.add(dis.readInt());
        }
    }
}
