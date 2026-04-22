import java.io.IOException;

/**
 * Interface que define o contrato para objetos que podem ser persistidos em arquivo.
 * Todas as entidades do sistema devem implementar esta interface.
 */
public interface Registro {
    // Explicado em docs/aux/registro/setId.md
    public void setId(int i);
    
    // Explicado em docs/aux/registro/getId.md
    public int getId();
    
    /**
     * Converte o objeto em um array de bytes para persistência.
     * 
     * @return Array de bytes representando o objeto
     * @throws IOException Se houver erro na conversão
     */
    // Explicado em docs/aux/registro/toByteArray.md
    public byte[] toByteArray() throws IOException;
    
    /**
     * Reconstrói o objeto a partir de um array de bytes.
     * 
     * @param b Array de bytes contendo os dados do objeto
     * @throws IOException Se houver erro na conversão
     */
    // Explicado em docs/aux/registro/fromByteArray.md
    public void fromByteArray(byte[] b) throws IOException;
}