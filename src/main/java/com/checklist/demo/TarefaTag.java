import java.io.*;

/**
 * Classe que representa a entidade de relacionamento Tarefa-Tag.
 * Implementa uma tabela de ligação N:N entre Tarefa e Tag.
 * 
 * Cada instância representa uma associação entre uma tarefa específica e uma tag específica.
 * Implementa a interface Registro para permitir persistência em arquivo.
 */
public class TarefaTag implements Registro {
    // Explicado em docs/aux/tarefaTag/tarefaTag.md
    private int id_tag;      // Chave estrangeira para Tag
    private int id_tarefa;   // Chave estrangeira para Tarefa

    /**
     * Construtor padrão.
     * Inicializa o relacionamento com valores padrão.
     */
    // Explicado em docs/aux/tarefaTag/construtorPadrao.md
    public TarefaTag() {
        this(-1, -1);
    }

    /**
     * Construtor com parâmetros.
     * 
     * @param id_tag ID da tag
     * @param id_tarefa ID da tarefa
     */
    // Explicado em docs/aux/tarefaTag/construtor.md
    public TarefaTag(int id_tag, int id_tarefa) {
        this.id_tag = id_tag;
        this.id_tarefa = id_tarefa;
    }

    /**
     * Retorna um ID composto para o relacionamento.
     * O ID é calculado como: id_tarefa * 1000000 + id_tag
     * Isso garante unicidade para a chave primária.
     * 
     * @return ID composto do relacionamento
     */
    @Override
    // Explicado em docs/aux/tarefaTag/getId.md
    public int getId() {
        return (id_tarefa * 1000000 + id_tag);
    }

    /**
     * Define o ID do relacionamento.
     * Método vazio pois o ID é calculado automaticamente.
     * 
     * @param id Novo ID (não utilizado)
     */
    @Override
    // Explicado em docs/aux/tarefaTag/setId.md
    public void setId(int id) {
        // Não aplicável para relacionamento - ID é composto
    }

    /**
     * Retorna o ID da tag associada.
     * 
     * @return ID da tag
     */
    // Explicado em docs/aux/tarefaTag/getIdTag.md
    public int getIdTag() {
        return id_tag;
    }

    /**
     * Define o ID da tag associada.
     * 
     * @param id_tag Novo ID da tag
     */
    // Explicado em docs/aux/tarefaTag/setIdTag.md
    public void setIdTag(int id_tag) {
        this.id_tag = id_tag;
    }

    /**
     * Retorna o ID da tarefa associada.
     * 
     * @return ID da tarefa
     */
    // Explicado em docs/aux/tarefaTag/getIdTarefa.md
    public int getIdTarefa() {
        return id_tarefa;
    }

    /**
     * Define o ID da tarefa associada.
     * 
     * @param id_tarefa Novo ID da tarefa
     */
    // Explicado em docs/aux/tarefaTag/setIdTarefa.md
    public void setIdTarefa(int id_tarefa) {
        this.id_tarefa = id_tarefa;
    }

    /**
     * Converte o objeto TarefaTag em um array de bytes para persistência.
     * Formato: [id_tag (4 bytes)] [id_tarefa (4 bytes)]
     * 
     * @return Array de bytes representando o objeto
     * @throws IOException Se houver erro na conversão
     */
    @Override
    // Explicado em docs/aux/tarefaTag/toByteArray.md
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.id_tag);
        dos.writeInt(this.id_tarefa);
        return baos.toByteArray();
    }

    /**
     * Reconstrói um objeto TarefaTag a partir de um array de bytes.
     * 
     * @param b Array de bytes contendo os dados do relacionamento
     * @throws IOException Se houver erro na conversão
     */
    @Override
    // Explicado em docs/aux/tarefaTag/fromByteArray.md
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id_tag = dis.readInt();
        this.id_tarefa = dis.readInt();
    }

    /**
     * Retorna uma representação textual do relacionamento para exibição.
     * 
     * @return String formatada com os dados do relacionamento
     */
    @Override
    // Explicado em docs/aux/tarefaTag/toString.md
    public String toString() {
        return "\nID Tag..: " + this.id_tag +
               "\nID Tarefa: " + this.id_tarefa;
    }
}