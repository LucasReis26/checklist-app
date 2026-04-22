import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe de registro para armazenar listas de endereços associados a uma chave.
 * Utilizada pelo HashExtensivel para gerenciar relacionamentos que armazenam posições de arquivo.
 * 
 * Formato de armazenamento: [id (4 bytes)] [quantidade (4 bytes)] [endereco1 (8 bytes)] [endereco2 (8 bytes)] ...
 */
public class RegistroListaEnderecos implements Registro {
    // Explicado em docs/aux/registroListaEnderecos/registroListaEnderecos.md
    private int id;
    private List<Long> enderecos;
    
    /**
     * Construtor padrão.
     * Inicializa com lista vazia.
     */
    // Explicado em docs/aux/registroListaEnderecos/construtorPadrao.md
    public RegistroListaEnderecos() {
        this(-1, new ArrayList<>());
    }
    
    /**
     * Construtor com parâmetros.
     * 
     * @param id Chave identificadora
     * @param enderecos Lista de endereços associados
     */
    // Explicado em docs/aux/registroListaEnderecos/construtor.md
    public RegistroListaEnderecos(int id, List<Long> enderecos) {
        this.id = id;
        this.enderecos = enderecos;
    }
    
    /**
     * Define o ID do registro.
     * 
     * @param id Novo ID
     */
    @Override
    // Explicado em docs/aux/registroListaEnderecos/setId.md
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Retorna o ID do registro.
     * 
     * @return ID do registro
     */
    @Override
    // Explicado em docs/aux/registroListaEnderecos/getId.md
    public int getId() {
        return id;
    }
    
    /**
     * Retorna a lista de endereços.
     * 
     * @return Lista de endereços
     */
    // Explicado em docs/aux/registroListaEnderecos/getEnderecos.md
    public List<Long> getEnderecos() {
        return enderecos;
    }
    
    /**
     * Define a lista de endereços.
     * 
     * @param enderecos Nova lista de endereços
     */
    // Explicado em docs/aux/registroListaEnderecos/setEnderecos.md
    public void setEnderecos(List<Long> enderecos) {
        this.enderecos = enderecos;
    }
    
    /**
     * Converte o objeto em um array de bytes para persistência.
     * 
     * @return Array de bytes representando o objeto
     * @throws IOException Se houver erro na conversão
     */
    @Override
    // Explicado em docs/aux/registroListaEnderecos/toByteArray.md
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(id);
        dos.writeInt(enderecos.size());
        for (Long endereco : enderecos) {
            dos.writeLong(endereco);
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
    // Explicado em docs/aux/registroListaEnderecos/fromByteArray.md
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        int tamanho = dis.readInt();
        this.enderecos = new ArrayList<>();
        for (int i = 0; i < tamanho; i++) {
            enderecos.add(dis.readLong());
        }
    }
}