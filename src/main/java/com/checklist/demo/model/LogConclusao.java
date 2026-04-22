package com.checklist.demo.model;

import java.io.*;
import com.checklist.demo.persistence.Registro;

/**
 * Classe que representa a entidade Log de Conclusão do sistema.
 * Registra o histórico de quando uma tarefa foi concluída.
 * Implementa a interface Registro para permitir persistência em arquivo.
 */
public class LogConclusao implements Registro {
    // Explicado em docs/aux/logConclusao/logConclusao.md
    private int id_log;
    private int id_tarefa;      // Chave estrangeira para Tarefa
    private String data_conclusao;
    private String resumo_tags;

    /**
     * Construtor padrão.
     * Inicializa o log com valores padrão.
     */
    // Explicado em docs/aux/logConclusao/construtorPadrao.md
    public LogConclusao() {
        this(-1, -1, "", "");
    }

    /**
     * Construtor com dados básicos (sem ID).
     * O ID será gerado automaticamente pelo sistema.
     * 
     * @param id_tarefa ID da tarefa concluída
     * @param data_conclusao Data e hora da conclusão
     * @param resumo_tags Resumo das tags associadas
     */
    // Explicado em docs/aux/logConclusao/construtorDados.md
    public LogConclusao(int id_tarefa, String data_conclusao, String resumo_tags) {
        this(-1, id_tarefa, data_conclusao, resumo_tags);
    }

    /**
     * Construtor completo com ID.
     * Utilizado principalmente para reconstrução a partir do arquivo.
     * 
     * @param id_log ID do log
     * @param id_tarefa ID da tarefa concluída
     * @param data_conclusao Data e hora da conclusão
     * @param resumo_tags Resumo das tags associadas
     */
    // Explicado em docs/aux/logConclusao/construtorCompleto.md
    public LogConclusao(int id_log, int id_tarefa, String data_conclusao, String resumo_tags) {
        this.id_log = id_log;
        this.id_tarefa = id_tarefa;
        this.data_conclusao = data_conclusao;
        this.resumo_tags = resumo_tags;
    }

    /**
     * Retorna o ID do log.
     * 
     * @return ID do log
     */
    @Override
    // Explicado em docs/aux/logConclusao/getId.md
    public int getId() {
        return id_log;
    }

    /**
     * Define o ID do log.
     * 
     * @param id Novo ID do log
     */
    @Override
    // Explicado em docs/aux/logConclusao/setId.md
    public void setId(int id) {
        this.id_log = id;
    }

    /**
     * Retorna o ID da tarefa associada.
     * 
     * @return ID da tarefa
     */
    // Explicado em docs/aux/logConclusao/getIdTarefa.md
    public int getIdTarefa() {
        return id_tarefa;
    }

    /**
     * Define o ID da tarefa associada.
     * 
     * @param id_tarefa Novo ID da tarefa
     */
    // Explicado em docs/aux/logConclusao/setIdTarefa.md
    public void setIdTarefa(int id_tarefa) {
        this.id_tarefa = id_tarefa;
    }

    /**
     * Retorna a data de conclusão.
     * 
     * @return Data de conclusão
     */
    // Explicado em docs/aux/logConclusao/getDataConclusao.md
    public String getDataConclusao() {
        return data_conclusao;
    }

    /**
     * Define a data de conclusão.
     * 
     * @param data_conclusao Nova data de conclusão
     */
    // Explicado em docs/aux/logConclusao/setDataConclusao.md
    public void setDataConclusao(String data_conclusao) {
        this.data_conclusao = data_conclusao;
    }

    /**
     * Retorna o resumo das tags.
     * 
     * @return Resumo das tags
     */
    // Explicado em docs/aux/logConclusao/getResumoTags.md
    public String getResumoTags() {
        return resumo_tags;
    }

    /**
     * Define o resumo das tags.
     * 
     * @param resumo_tags Novo resumo das tags
     */
    // Explicado em docs/aux/logConclusao/setResumoTags.md
    public void setResumoTags(String resumo_tags) {
        this.resumo_tags = resumo_tags;
    }

    /**
     * Converte o objeto LogConclusao em um array de bytes para persistência.
     * Formato: [id_log (4 bytes)] [id_tarefa (4 bytes)] [data_conclusao (UTF)] [resumo_tags (UTF)]
     * 
     * @return Array de bytes representando o objeto
     * @throws IOException Se houver erro na conversão
     */
    @Override
    // Explicado em docs/aux/logConclusao/toByteArray.md
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.id_log);
        dos.writeInt(this.id_tarefa);
        dos.writeUTF(this.data_conclusao);
        dos.writeUTF(this.resumo_tags);
        return baos.toByteArray();
    }

    /**
     * Reconstrói um objeto LogConclusao a partir de um array de bytes.
     * 
     * @param b Array de bytes contendo os dados do log
     * @throws IOException Se houver erro na conversão
     */
    @Override
    // Explicado em docs/aux/logConclusao/fromByteArray.md
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id_log = dis.readInt();
        this.id_tarefa = dis.readInt();
        this.data_conclusao = dis.readUTF();
        this.resumo_tags = dis.readUTF();
    }

    /**
     * Retorna uma representação textual do log para exibição.
     * 
     * @return String formatada com os dados do log
     */
    @Override
    // Explicado em docs/aux/logConclusao/toString.md
    public String toString() {
        return "\nID Log........: " + this.id_log +
               "\nID Tarefa.....: " + this.id_tarefa +
               "\nData Conclusão: " + this.data_conclusao +
               "\nResumo Tags...: " + this.resumo_tags;
    }
}
