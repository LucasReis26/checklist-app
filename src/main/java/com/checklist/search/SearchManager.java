package com.checklist.search;

import com.checklist.dao.TarefaDAO;
import com.checklist.dao.UsuarioDAO;
import com.checklist.model.Tarefa;
import com.checklist.model.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * Gerenciador de buscas no sistema.
 * Integra os algoritmos KMP e Boyer-Moore com as entidades do sistema.
 */
public class SearchManager {
    
    private final TarefaDAO tarefaDAO;
    private final UsuarioDAO usuarioDAO;
    
    public enum SearchAlgorithm {
        KMP,
        BOYER_MOORE
    }
    
    /**
     * Construtor que recebe os DAOs já instanciados (para evitar duplicação).
     */
    public SearchManager(TarefaDAO tarefaDAO, UsuarioDAO usuarioDAO) {
        this.tarefaDAO = tarefaDAO;
        this.usuarioDAO = usuarioDAO;
    }
    
    /**
     * Busca tarefas cujo título ou descrição contenha o padrão.
     * 
     * @param pattern Padrão a ser procurado
     * @param algorithm Algoritmo a ser utilizado (KMP ou BOYER_MOORE)
     * @return Lista de tarefas que contêm o padrão
     * @throws Exception Se houver erro na busca
     */
    public List<Tarefa> searchTarefas(String pattern, SearchAlgorithm algorithm) throws Exception {
        List<Tarefa> results = new ArrayList<>();
        List<Tarefa> allTarefas = tarefaDAO.listarTodas();
        
        for (Tarefa tarefa : allTarefas) {
            // Busca no título e na descrição
            boolean foundInTitle = searchInText(tarefa.getTitulo(), pattern, algorithm);
            boolean foundInDesc = searchInText(tarefa.getDescricao(), pattern, algorithm);
            
            if (foundInTitle || foundInDesc) {
                results.add(tarefa);
            }
        }
        
        return results;
    }
    
    /**
     * Busca usuários cujo nome ou email contenha o padrão.
     * 
     * @param pattern Padrão a ser procurado
     * @param algorithm Algoritmo a ser utilizado (KMP ou BOYER_MOORE)
     * @return Lista de usuários que contêm o padrão
     * @throws Exception Se houver erro na busca
     */
    public List<Usuario> searchUsuarios(String pattern, SearchAlgorithm algorithm) throws Exception {
        List<Usuario> results = new ArrayList<>();
        List<Usuario> allUsuarios = usuarioDAO.listarTodos();
        
        for (Usuario usuario : allUsuarios) {
            boolean foundInName = searchInText(usuario.getNome(), pattern, algorithm);
            boolean foundInEmail = searchInText(usuario.getEmail(), pattern, algorithm);
            
            if (foundInName || foundInEmail) {
                results.add(usuario);
            }
        }
        
        return results;
    }
    
    /**
     * Busca um padrão em um texto usando o algoritmo especificado.
     * 
     * @param text Texto a ser pesquisado
     * @param pattern Padrão a ser procurado
     * @param algorithm Algoritmo a ser utilizado
     * @return true se o padrão foi encontrado
     */
    private boolean searchInText(String text, String pattern, SearchAlgorithm algorithm) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        if (algorithm == SearchAlgorithm.KMP) {
            return KMPSearch.contains(text, pattern);
        } else {
            return BoyerMooreSearch.contains(text, pattern);
        }
    }
    
    /**
     * Busca um padrão em um texto e retorna as posições usando KMP.
     * 
     * @param text Texto a ser pesquisado
     * @param pattern Padrão a ser procurado
     * @return Lista de posições onde o padrão foi encontrado
     */
    public List<Integer> searchWithKMP(String text, String pattern) {
        return KMPSearch.search(text, pattern);
    }
    
    /**
     * Busca um padrão em um texto e retorna as posições usando Boyer-Moore.
     * 
     * @param text Texto a ser pesquisado
     * @param pattern Padrão a ser procurado
     * @return Lista de posições onde o padrão foi encontrado
     */
    public List<Integer> searchWithBM(String text, String pattern) {
        return BoyerMooreSearch.search(text, pattern);
    }
}