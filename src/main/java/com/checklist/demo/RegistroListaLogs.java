package com.checklist.demo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe de registro para armazenar listas de logs associados a uma tarefa.
 * Utilizada pelo TarefaLogsManager para gerenciar o relacionamento 1:N entre tarefa e logs.
 * 
 * Formato de armazenamento: [id (4 bytes)] [quantidade (4 bytes)] [log1 (4 bytes)] [log2 (4 bytes)] ...
 */
public class RegistroListaLogs implements Registro {
    // Explicado em docs/aux/registroListaLogs/registroListaLogs.md
    private int id;
    private List<Integer> logs;
    
    /**
     * Construtor padrão.
     * Inicializa com lista vazia.
     */
    // Explicado em docs/aux/registroListaLogs/construtorPadrao.md
    public RegistroListaLogs() {
        this(-1, new ArrayList<>());
    }
    
    /**
     * Construtor com parâmetros.
     * 
     * @param id Chave identificadora (geralmente ID da tarefa)
     * @param logs Lista de IDs dos logs associados
     */
    // Explicado em docs/aux/registroListaLogs/construtor.md
    public RegistroListaLogs(int id, List<Integer> logs) {
        this.id = id;
        this.logs = logs;
    }
    
    /**
     * Define o ID do registro.
     * 
     * @param id Novo ID
     */
    @Override
    // Explicado em docs/aux/registroListaLogs/setId.md
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Retorna o ID do registro.
     * 
     * @return ID do registro
     */
    @Override
    // Explicado em docs/aux/registroListaLogs/getId.md
    public int getId() {
        return id;
    }
    
    /**
     * Retorna a lista de IDs dos logs.
     * 
     * @return Lista de logs
     */
    // Explicado em docs/aux/registroListaLogs/getLogs.md
    public List<Integer> getLogs() {
        return logs;
    }
    
    /**
     * Define a lista de IDs dos logs.
     * 
     * @param logs Nova lista de logs
     */
    // Explicado em docs/aux/registroListaLogs/setLogs.md
    public void setLogs(List<Integer> logs) {
        this.logs = logs;
    }
    
    /**
     * Converte o objeto em um array de bytes para persistência.
     * 
     * @return Array de bytes representando o objeto
     * @throws IOException Se houver erro na conversão
     */
    @Override
    // Explicado em docs/aux/registroListaLogs/toByteArray.md
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(id);
        dos.writeInt(logs.size());
        for (Integer logId : logs) {
            dos.writeInt(logId);
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
    // Explicado em docs/aux/registroListaLogs/fromByteArray.md
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        int tamanho = dis.readInt();
        this.logs = new ArrayList<>();
        for (int i = 0; i < tamanho; i++) {
            logs.add(dis.readInt());
        }
    }
}