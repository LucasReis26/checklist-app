package com.checklist.demo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe de registro para armazenar listas de categorias associadas a um usuário.
 * Utilizada pelo UsuarioCategoriasManager para gerenciar o relacionamento 1:N entre usuário e categorias.
 * 
 * Formato de armazenamento: [id (4 bytes)] [quantidade (4 bytes)] [categoria1 (4 bytes)] [categoria2 (4 bytes)] ...
 */
public class RegistroListaCategorias implements Registro {
    // Explicado em docs/aux/registroListaCategorias/registroListaCategorias.md
    private int id;
    private List<Integer> categorias;
    
    /**
     * Construtor padrão.
     * Inicializa com lista vazia.
     */
    // Explicado em docs/aux/registroListaCategorias/construtorPadrao.md
    public RegistroListaCategorias() {
        this(-1, new ArrayList<>());
    }
    
    /**
     * Construtor com parâmetros.
     * 
     * @param id Chave identificadora (ID do usuário)
     * @param categorias Lista de IDs das categorias associadas
     */
    // Explicado em docs/aux/registroListaCategorias/construtor.md
    public RegistroListaCategorias(int id, List<Integer> categorias) {
        this.id = id;
        this.categorias = categorias;
    }
    
    /**
     * Define o ID do registro.
     * 
     * @param id Novo ID
     */
    @Override
    // Explicado em docs/aux/registroListaCategorias/setId.md
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Retorna o ID do registro.
     * 
     * @return ID do registro
     */
    @Override
    // Explicado em docs/aux/registroListaCategorias/getId.md
    public int getId() {
        return id;
    }
    
    /**
     * Retorna a lista de IDs das categorias.
     * 
     * @return Lista de categorias
     */
    // Explicado em docs/aux/registroListaCategorias/getCategorias.md
    public List<Integer> getCategorias() {
        return categorias;
    }
    
    /**
     * Define a lista de IDs das categorias.
     * 
     * @param categorias Nova lista de categorias
     */
    // Explicado em docs/aux/registroListaCategorias/setCategorias.md
    public void setCategorias(List<Integer> categorias) {
        this.categorias = categorias;
    }
    
    /**
     * Converte o objeto em um array de bytes para persistência.
     * 
     * @return Array de bytes representando o objeto
     * @throws IOException Se houver erro na conversão
     */
    @Override
    // Explicado em docs/aux/registroListaCategorias/toByteArray.md
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(id);
        dos.writeInt(categorias.size());
        for (Integer categoriaId : categorias) {
            dos.writeInt(categoriaId);
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
    // Explicado em docs/aux/registroListaCategorias/fromByteArray.md
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        int tamanho = dis.readInt();
        this.categorias = new ArrayList<>();
        for (int i = 0; i < tamanho; i++) {
            categorias.add(dis.readInt());
        }
    }
    
    /**
     * Retorna uma representação textual do registro para debug.
     * 
     * @return String com os dados do registro
     */
    @Override
    // Explicado em docs/aux/registroListaCategorias/toString.md
    public String toString() {
        return "RegistroListaCategorias{id=" + id + ", categorias=" + categorias + "}";
    }
}