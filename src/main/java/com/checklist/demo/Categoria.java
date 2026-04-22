package com.checklist.demo;

import java.io.*;

/**
 * Classe que representa a entidade Categoria do sistema.
 * Uma categoria pertence a um usuário e pode conter várias tarefas.
 * Implementa a interface Registro para permitir persistência em arquivo.
 */
public class Categoria implements Registro {
    // Explicado em docs/aux/categoria/categoria.md
    private int id_categoria;
    private int id_user;  // Chave estrangeira para Usuario
    private String nome;

    /**
     * Construtor padrão.
     * Inicializa a categoria com valores padrão.
     */
    // Explicado em docs/aux/categoria/construtorPadrao.md
    public Categoria() {
        this(-1, -1, "");
    }

    /**
     * Construtor com dados básicos (sem ID).
     * O ID será gerado automaticamente pelo sistema.
     * 
     * @param id_user ID do usuário dono da categoria
     * @param nome Nome da categoria
     */
    // Explicado em docs/aux/categoria/construtorDados.md
    public Categoria(int id_user, String nome) {
        this(-1, id_user, nome);
    }

    /**
     * Construtor completo com ID.
     * Utilizado principalmente para reconstrução a partir do arquivo.
     * 
     * @param id_categoria ID da categoria
     * @param id_user ID do usuário dono da categoria
     * @param nome Nome da categoria
     */
    // Explicado em docs/aux/categoria/construtorCompleto.md
    public Categoria(int id_categoria, int id_user, String nome) {
        this.id_categoria = id_categoria;
        this.id_user = id_user;
        this.nome = nome;
    }

    /**
     * Retorna o ID da categoria.
     * 
     * @return ID da categoria
     */
    @Override
    // Explicado em docs/aux/categoria/getId.md
    public int getId() {
        return id_categoria;
    }

    /**
     * Define o ID da categoria.
     * 
     * @param id Novo ID da categoria
     */
    @Override
    // Explicado em docs/aux/categoria/setId.md
    public void setId(int id) {
        this.id_categoria = id;
    }

    /**
     * Retorna o ID do usuário dono da categoria.
     * 
     * @return ID do usuário
     */
    // Explicado em docs/aux/categoria/getIdUser.md
    public int getIdUser() {
        return id_user;
    }

    /**
     * Define o ID do usuário dono da categoria.
     * 
     * @param id_user Novo ID do usuário
     */
    // Explicado em docs/aux/categoria/setIdUser.md
    public void setIdUser(int id_user) {
        this.id_user = id_user;
    }

    /**
     * Retorna o nome da categoria.
     * 
     * @return Nome da categoria
     */
    // Explicado em docs/aux/categoria/getNome.md
    public String getNome() {
        return nome;
    }

    /**
     * Define o nome da categoria.
     * 
     * @param nome Novo nome da categoria
     */
    // Explicado em docs/aux/categoria/setNome.md
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Converte o objeto Categoria em um array de bytes para persistência.
     * Formato: [id_categoria (4 bytes)] [id_user (4 bytes)] [nome (UTF)]
     * 
     * @return Array de bytes representando o objeto
     * @throws IOException Se houver erro na conversão
     */
    @Override
    // Explicado em docs/aux/categoria/toByteArray.md
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.id_categoria);
        dos.writeInt(this.id_user);
        dos.writeUTF(this.nome);
        return baos.toByteArray();
    }

    /**
     * Reconstrói um objeto Categoria a partir de um array de bytes.
     * 
     * @param b Array de bytes contendo os dados da categoria
     * @throws IOException Se houver erro na conversão
     */
    @Override
    // Explicado em docs/aux/categoria/fromByteArray.md
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id_categoria = dis.readInt();
        this.id_user = dis.readInt();
        this.nome = dis.readUTF();
    }

    /**
     * Retorna uma representação textual da categoria para exibição.
     * 
     * @return String formatada com os dados da categoria
     */
    @Override
    // Explicado em docs/aux/categoria/toString.md
    public String toString() {
        return "\nID Categoria.: " + this.id_categoria +
               "\nID Usuário..: " + this.id_user +
               "\nNome........: " + this.nome;
    }
}