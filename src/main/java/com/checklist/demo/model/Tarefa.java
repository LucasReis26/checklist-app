package com.checklist.demo.model;

import java.io.*;
import com.checklist.demo.persistence.Registro;

/**
 * Classe que representa a entidade Tarefa do sistema.
 * Uma tarefa pertence a um usuário e pode estar associada a uma categoria,
 * além de poder ter várias tags e logs de conclusão.
 * 
 * Implementa a interface Registro para permitir persistência em arquivo.
 */
public class Tarefa implements Registro {
    // Explicado em docs/aux/tarefa/tarefa.md
    private int id_tarefa;
    private int id_user;         // Chave estrangeira para Usuario
    private int id_categoria;    // Chave estrangeira para Categoria (0 = nenhuma)
    private String titulo;
    private String descricao;
    private String data_criacao;
    private String status;       // "pendente" ou "concluida"
    private String data_vencimento;

    /**
     * Construtor padrão.
     * Inicializa a tarefa com valores padrão.
     */
    // Explicado em docs/aux/tarefa/construtorPadrao.md
    public Tarefa() {
        this(-1, -1, -1, "", "", "", "", "");
    }

    /**
     * Construtor com dados básicos (sem ID).
     * O ID será gerado automaticamente pelo sistema.
     * 
     * @param id_user ID do usuário responsável pela tarefa
     * @param id_categoria ID da categoria da tarefa (0 para nenhuma)
     * @param titulo Título da tarefa
     * @param descricao Descrição detalhada da tarefa
     * @param data_criacao Data de criação da tarefa
     * @param status Status atual ("pendente" ou "concluida")
     * @param data_vencimento Data limite para conclusão
     */
    // Explicado em docs/aux/tarefa/construtorDados.md
    public Tarefa(int id_user, int id_categoria, String titulo, String descricao, 
                  String data_criacao, String status, String data_vencimento) {
        this(-1, id_user, id_categoria, titulo, descricao, data_criacao, status, data_vencimento);
    }

    /**
     * Construtor completo com ID.
     * Utilizado principalmente para reconstrução a partir do arquivo.
     * 
     * @param id_tarefa ID da tarefa
     * @param id_user ID do usuário responsável
     * @param id_categoria ID da categoria associada
     * @param titulo Título da tarefa
     * @param descricao Descrição detalhada
     * @param data_criacao Data de criação
     * @param status Status atual
     * @param data_vencimento Data de vencimento
     */
    // Explicado em docs/aux/tarefa/construtorCompleto.md
    public Tarefa(int id_tarefa, int id_user, int id_categoria, String titulo, String descricao,
                  String data_criacao, String status, String data_vencimento) {
        this.id_tarefa = id_tarefa;
        this.id_user = id_user;
        this.id_categoria = id_categoria;
        this.titulo = titulo;
        this.descricao = descricao;
        this.data_criacao = data_criacao;
        this.status = status;
        this.data_vencimento = data_vencimento;
    }

    /**
     * Retorna o ID da tarefa.
     * 
     * @return ID da tarefa
     */
    @Override
    // Explicado em docs/aux/tarefa/getId.md
    public int getId() {
        return id_tarefa;
    }

    /**
     * Define o ID da tarefa.
     * 
     * @param id Novo ID da tarefa
     */
    @Override
    // Explicado em docs/aux/tarefa/setId.md
    public void setId(int id) {
        this.id_tarefa = id;
    }

    /**
     * Retorna o ID do usuário responsável pela tarefa.
     * 
     * @return ID do usuário
     */
    // Explicado em docs/aux/tarefa/getIdUser.md
    public int getIdUser() {
        return id_user;
    }

    /**
     * Define o ID do usuário responsável pela tarefa.
     * 
     * @param id_user Novo ID do usuário
     */
    // Explicado em docs/aux/tarefa/setIdUser.md
    public void setIdUser(int id_user) {
        this.id_user = id_user;
    }

    /**
     * Retorna o ID da categoria associada à tarefa.
     * 
     * @return ID da categoria (0 se nenhuma)
     */
    // Explicado em docs/aux/tarefa/getIdCategoria.md
    public int getIdCategoria() {
        return id_categoria;
    }

    /**
     * Define o ID da categoria associada à tarefa.
     * 
     * @param id_categoria Novo ID da categoria (0 para nenhuma)
     */
    // Explicado em docs/aux/tarefa/setIdCategoria.md
    public void setIdCategoria(int id_categoria) {
        this.id_categoria = id_categoria;
    }

    /**
     * Retorna o título da tarefa.
     * 
     * @return Título da tarefa
     */
    // Explicado em docs/aux/tarefa/getTitulo.md
    public String getTitulo() {
        return titulo;
    }

    /**
     * Define o título da tarefa.
     * 
     * @param titulo Novo título da tarefa
     */
    // Explicado em docs/aux/tarefa/setTitulo.md
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Retorna a descrição da tarefa.
     * 
     * @return Descrição da tarefa
     */
    // Explicado em docs/aux/tarefa/getDescricao.md
    public String getDescricao() {
        return descricao;
    }

    /**
     * Define a descrição da tarefa.
     * 
     * @param descricao Nova descrição da tarefa
     */
    // Explicado em docs/aux/tarefa/setDescricao.md
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna a data de criação da tarefa.
     * 
     * @return Data de criação
     */
    // Explicado em docs/aux/tarefa/getDataCriacao.md
    public String getDataCriacao() {
        return data_criacao;
    }

    /**
     * Define a data de criação da tarefa.
     * 
     * @param data_criacao Nova data de criação
     */
    // Explicado em docs/aux/tarefa/setDataCriacao.md
    public void setDataCriacao(String data_criacao) {
        this.data_criacao = data_criacao;
    }

    /**
     * Retorna o status da tarefa.
     * 
     * @return Status ("pendente" ou "concluida")
     */
    // Explicado em docs/aux/tarefa/getStatus.md
    public String getStatus() {
        return status;
    }

    /**
     * Define o status da tarefa.
     * 
     * @param status Novo status ("pendente" ou "concluida")
     */
    // Explicado em docs/aux/tarefa/setStatus.md
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Retorna a data de vencimento da tarefa.
     * 
     * @return Data de vencimento
     */
    // Explicado em docs/aux/tarefa/getDataVencimento.md
    public String getDataVencimento() {
        return data_vencimento;
    }

    /**
     * Define a data de vencimento da tarefa.
     * 
     * @param data_vencimento Nova data de vencimento
     */
    // Explicado em docs/aux/tarefa/setDataVencimento.md
    public void setDataVencimento(String data_vencimento) {
        this.data_vencimento = data_vencimento;
    }

    /**
     * Converte o objeto Tarefa em um array de bytes para persistência.
     * Formato: [id_tarefa (4 bytes)] [id_user (4 bytes)] [id_categoria (4 bytes)]
     *          [titulo (UTF)] [descricao (UTF)] [data_criacao (UTF)]
     *          [status (UTF)] [data_vencimento (UTF)]
     * 
     * @return Array de bytes representando o objeto
     * @throws IOException Se houver erro na conversão
     */
    @Override
    // Explicado em docs/aux/tarefa/toByteArray.md
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.id_tarefa);
        dos.writeInt(this.id_user);
        dos.writeInt(this.id_categoria);
        dos.writeUTF(this.titulo);
        dos.writeUTF(this.descricao);
        dos.writeUTF(this.data_criacao);
        dos.writeUTF(this.status);
        dos.writeUTF(this.data_vencimento);
        return baos.toByteArray();
    }

    /**
     * Reconstrói um objeto Tarefa a partir de um array de bytes.
     * 
     * @param b Array de bytes contendo os dados da tarefa
     * @throws IOException Se houver erro na conversão
     */
    @Override
    // Explicado em docs/aux/tarefa/fromByteArray.md
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id_tarefa = dis.readInt();
        this.id_user = dis.readInt();
        this.id_categoria = dis.readInt();
        this.titulo = dis.readUTF();
        this.descricao = dis.readUTF();
        this.data_criacao = dis.readUTF();
        this.status = dis.readUTF();
        this.data_vencimento = dis.readUTF();
    }

    /**
     * Retorna uma representação textual da tarefa para exibição.
     * 
     * @return String formatada com os dados da tarefa
     */
    @Override
    // Explicado em docs/aux/tarefa/toString.md
    public String toString() {
        return "\nID Tarefa....: " + this.id_tarefa +
               "\nID Usuário...: " + this.id_user +
               "\nID Categoria.: " + this.id_categoria +
               "\nTítulo.......: " + this.titulo +
               "\nDescrição....: " + this.descricao +
               "\nData Criação.: " + this.data_criacao +
               "\nStatus.......: " + this.status +
               "\nData Vencimento: " + this.data_vencimento;
    }
}
