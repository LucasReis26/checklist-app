package com.checklist.demo;

import java.io.*;

/**
 * Classe de registro auxiliar para armazenar pares chave-endereço no índice hash.
 * Implementa a interface Registro para permitir persistência em arquivo.
 * 
 * Utilizado principalmente pelo HashExtensivel para gerenciar relacionamentos.
 */
public class RegistroIndex implements Registro {
    // Explicado em docs/aux/registroIndex/registroIndex.md
    private int id;
    private long endereco;
    
    /**
     * Construtor padrão.
     * Inicializa o registro com valores padrão.
     */
    // Explicado em docs/aux/registroIndex/construtorPadrao.md
    public RegistroIndex() {
        this(-1, -1);
    }
    
    /**
     * Construtor com parâmetros.
     * 
     * @param id Chave do registro
     * @param endereco Endereço associado à chave
     */
    // Explicado em docs/aux/registroIndex/construtor.md
    public RegistroIndex(int id, long endereco) {
        this.id = id;
        this.endereco = endereco;
    }
    
    /**
     * Define o ID do registro.
     * 
     * @param id Novo ID
     */
    @Override
    // Explicado em docs/aux/registroIndex/setId.md
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Retorna o ID do registro.
     * 
     * @return ID do registro
     */
    @Override
    // Explicado em docs/aux/registroIndex/getId.md
    public int getId() {
        return id;
    }
    
    /**
     * Retorna o endereço armazenado.
     * 
     * @return Endereço do registro
     */
    // Explicado em docs/aux/registroIndex/getEndereco.md
    public long getEndereco() {
        return endereco;
    }
    
    /**
     * Define o endereço do registro.
     * 
     * @param endereco Novo endereço
     */
    // Explicado em docs/aux/registroIndex/setEndereco.md
    public void setEndereco(long endereco) {
        this.endereco = endereco;
    }
    
    /**
     * Converte o objeto em um array de bytes para persistência.
     * Formato: [id (4 bytes)] [endereco (8 bytes)]
     * 
     * @return Array de bytes representando o objeto
     * @throws IOException Se houver erro na conversão
     */
    @Override
    // Explicado em docs/aux/registroIndex/toByteArray.md
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(id);
        dos.writeLong(endereco);
        return baos.toByteArray();
    }
    
    /**
     * Reconstrói um objeto a partir de um array de bytes.
     * 
     * @param b Array de bytes contendo os dados
     * @throws IOException Se houver erro na conversão
     */
    @Override
    // Explicado em docs/aux/registroIndex/fromByteArray.md
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        this.endereco = dis.readLong();
    }
}