package com.checklist.model;

import com.checklist.persistence.Registro;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.*;

/**
 * Classe que representa a entidade Tag do sistema.
 * Tags são palavras-chave que podem ser associadas a tarefas (relacionamento N:N).
 * Implementa a interface Registro para permitir persistência em arquivo.
 */
public class Tag implements Registro {
    // Explicado em docs/aux/tag/tag.md
    @JsonProperty("id_tag")
    private int id_tag;
    @JsonProperty("id_user")
    private int id_user;
    private String nome;

    /**
     * Construtor padrão.
     * Inicializa a tag com valores padrão.
     */
    // Explicado em docs/aux/tag/construtorPadrao.md
    public Tag() {
        this(-1, -1, "");
    }

    /**
     * Construtor com nome (sem ID).
     * O ID será gerado automaticamente pelo sistema.
     * 
     * @param id_user ID do usuário dono da tag
     * @param nome Nome da tag
     */
    // Explicado em docs/aux/tag/construtorNome.md
    public Tag(int id_user, String nome) {
        this(-1, id_user, nome);
    }

    /**
     * Construtor completo com ID.
     * Utilizado principalmente para reconstrução a partir do arquivo.
     * 
     * @param id_tag ID da tag
     * @param id_user ID do usuário dono da tag
     * @param nome Nome da tag
     */
    // Explicado em docs/aux/tag/construtorCompleto.md
    public Tag(int id_tag, int id_user, String nome) {
        this.id_tag = id_tag;
        this.id_user = id_user;
        this.nome = nome;
    }

    /**
     * Retorna o ID da tag.
     * 
     * @return ID da tag
     */
    @Override
    // Explicado em docs/aux/tag/getId.md
    public int getId() {
        return id_tag;
    }

    /**
     * Define o ID da tag.
     * 
     * @param id Novo ID da tag
     */
    @Override
    // Explicado em docs/aux/tag/setId.md
    public void setId(int id) {
        this.id_tag = id;
    }

    /**
     * Retorna o ID do usuário dono da tag.
     * 
     * @return ID do usuário
     */
    public int getIdUser() {
        return id_user;
    }

    /**
     * Define o ID do usuário dono da tag.
     * 
     * @param id_user Novo ID do usuário
     */
    public void setIdUser(int id_user) {
        this.id_user = id_user;
    }

    /**
     * Retorna o nome da tag.
     * 
     * @return Nome da tag
     */
    // Explicado em docs/aux/tag/getNome.md
    public String getNome() {
        return nome;
    }

    /**
     * Define o nome da tag.
     * 
     * @param nome Novo nome da tag
     */
    // Explicado em docs/aux/tag/setNome.md
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Converte o objeto Tag em um array de bytes para persistência.
     * Formato: [id_tag (4 bytes)] [id_user (4 bytes)] [nome (UTF)]
     * 
     * @return Array de bytes representando o objeto
     * @throws IOException Se houver erro na conversion
     */
    @Override
    // Explicado em docs/aux/tag/toByteArray.md
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.id_tag);
        dos.writeInt(this.id_user);
        dos.writeUTF(this.nome);
        return baos.toByteArray();
    }

    /**
     * Reconstrói um objeto Tag a partir de um array de bytes.
     * 
     * @param b Array de bytes contendo os dados da tag
     * @throws IOException Se houver erro na conversão
     */
    @Override
    // Explicado em docs/aux/tag/fromByteArray.md
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id_tag = dis.readInt();
        this.id_user = dis.readInt();
        this.nome = dis.readUTF();
    }

    /**
     * Retorna uma representação textual da tag para exibição.
     * 
     * @return String formatada com os dados da tag
     */
    @Override
    // Explicado em docs/aux/tag/toString.md
    public String toString() {
        return "\nID Tag..: " + this.id_tag +
               "\nID Usuário: " + this.id_user +
               "\nNome....: " + this.nome;
    }
}
