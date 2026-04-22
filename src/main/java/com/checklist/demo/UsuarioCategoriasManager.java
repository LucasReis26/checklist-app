package com.checklist.demo;

import java.util.ArrayList;
import java.util.List;

/**
 * Gerenciador do relacionamento 1:N entre Usuario e Categoria.
 * Um usuário pode ter várias categorias.
 * 
 * Armazena a lista de IDs de categorias para cada usuário usando
 * um arquivo de índice com chave = idUsuario e valor = lista de ids das categorias.
 */
public class UsuarioCategoriasManager {
    // Explicado em docs/aux/usuarioCategoriasManager/usuarioCategoriasManager.md
    private ArquivoIndex<RegistroListaCategorias> arquivoRelacoes;
    
    /**
     * Construtor da classe UsuarioCategoriasManager.
     * Inicializa o arquivo que armazena as relações.
     * 
     * @throws Exception Se houver erro na inicialização
     */
    // Explicado em docs/aux/usuarioCategoriasManager/construtor.md
    public UsuarioCategoriasManager() throws Exception {
        arquivoRelacoes = new ArquivoIndex<>("usuario_categorias_rel", 
                                              RegistroListaCategorias.class.getConstructor());
    }
    
    /**
     * Adiciona uma categoria à lista de categorias de um usuário.
     * 
     * @param idUsuario Identificador do usuário
     * @param idCategoria Identificador da categoria a ser adicionada
     * @throws Exception Se houver erro na operação
     */
    // Explicado em docs/aux/usuarioCategoriasManager/adicionarCategoria.md
    public void adicionarCategoria(int idUsuario, int idCategoria) throws Exception {
        // Buscar registro existente para este usuário
        RegistroListaCategorias registro = arquivoRelacoes.read(idUsuario);
        List<Integer> categorias;
        
        if (registro == null) {
            categorias = new ArrayList<>();
        } else {
            categorias = registro.getCategorias();
        }
        
        // Evita duplicação
        if (!categorias.contains(idCategoria)) {
            categorias.add(idCategoria);
            RegistroListaCategorias novoRegistro = new RegistroListaCategorias(idUsuario, categorias);
            
            if (registro == null) {
                arquivoRelacoes.create(novoRegistro);
            } else {
                arquivoRelacoes.update(novoRegistro);
            }
        }
    }
    
    /**
     * Remove uma categoria da lista de categorias de um usuário.
     * 
     * @param idUsuario Identificador do usuário
     * @param idCategoria Identificador da categoria a ser removida
     * @throws Exception Se houver erro na operação
     */
    // Explicado em docs/aux/usuarioCategoriasManager/removerCategoria.md
    public void removerCategoria(int idUsuario, int idCategoria) throws Exception {
        RegistroListaCategorias registro = arquivoRelacoes.read(idUsuario);
        if (registro != null) {
            List<Integer> categorias = registro.getCategorias();
            categorias.remove(Integer.valueOf(idCategoria));
            
            if (categorias.isEmpty()) {
                // Se não há mais categorias, remove o registro
                arquivoRelacoes.delete(idUsuario);
            } else {
                // Atualiza a lista
                registro.setCategorias(categorias);
                arquivoRelacoes.update(registro);
            }
        }
    }
    
    /**
     * Busca todas as categorias associadas a um usuário.
     * 
     * @param idUsuario Identificador do usuário
     * @return Lista de IDs das categorias do usuário
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/usuarioCategoriasManager/buscarCategoriasDoUsuario.md
    public List<Integer> buscarCategoriasDoUsuario(int idUsuario) throws Exception {
        RegistroListaCategorias registro = arquivoRelacoes.read(idUsuario);
        if (registro != null) {
            return registro.getCategorias();
        }
        return new ArrayList<>();
    }
    
    /**
     * Conta quantas categorias estão associadas a um usuário.
     * 
     * @param idUsuario Identificador do usuário
     * @return Número de categorias do usuário
     * @throws Exception Se houver erro na contagem
     */
    // Explicado em docs/aux/usuarioCategoriasManager/contarCategoriasDoUsuario.md
    public int contarCategoriasDoUsuario(int idUsuario) throws Exception {
        return buscarCategoriasDoUsuario(idUsuario).size();
    }
    
    /**
     * Fecha a conexão com o arquivo de relações.
     * 
     * @throws Exception Se houver erro no fechamento
     */
    // Explicado em docs/aux/usuarioCategoriasManager/close.md
    public void close() throws Exception {
        arquivoRelacoes.close();
    }
}