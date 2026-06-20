package com.checklist.ui.cli;

import com.checklist.dao.CategoriaDAO;
import com.checklist.dao.TarefaDAO;
import com.checklist.dao.UsuarioDAO;
import com.checklist.manager.CategoriaTarefasManager;
import com.checklist.manager.TarefaLogsManager;
import com.checklist.manager.UsuarioCategoriasManager;
import com.checklist.manager.UsuarioTarefasManager;
import com.checklist.model.Tarefa;
import com.checklist.model.Usuario;
import com.checklist.search.SearchManager;
import com.checklist.search.SearchManager.SearchAlgorithm;

import java.util.List;
import java.util.Scanner;

/**
 * Menu para busca de padrões usando KMP e Boyer-Moore.
 */
public class MenuBusca {
    
    private final SearchManager searchManager;
    private final Scanner scanner;
    
    public MenuBusca() throws Exception {
        // Inicializa os gerenciadores necessários
        UsuarioTarefasManager utm = new UsuarioTarefasManager();
        UsuarioCategoriasManager ucm = new UsuarioCategoriasManager();
        CategoriaTarefasManager ctm = new CategoriaTarefasManager();
        TarefaLogsManager tlm = new TarefaLogsManager();
        
        UsuarioDAO usuarioDAO = new UsuarioDAO(utm, ucm);
        CategoriaDAO categoriaDAO = new CategoriaDAO(usuarioDAO, ctm);
        TarefaDAO tarefaDAO = new TarefaDAO(usuarioDAO, categoriaDAO, utm, ctm, tlm);
        
        this.searchManager = new SearchManager(tarefaDAO, usuarioDAO);
        this.scanner = new Scanner(System.in);
    }
    
    public void menu() {
        int opcao;
        do {
            System.out.println("\n\nAEDsIII");
            System.out.println("-------");
            System.out.println("> Início > Busca de Padrões");
            System.out.println("\n1 - Buscar em Tarefas (KMP)");
            System.out.println("2 - Buscar em Tarefas (Boyer-Moore)");
            System.out.println("3 - Buscar em Usuários (KMP)");
            System.out.println("4 - Buscar em Usuários (Boyer-Moore)");
            System.out.println("0 - Voltar");
            
            System.out.print("\nOpção: ");
            try {
                opcao = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                opcao = -1;
            }
            
            switch (opcao) {
                case 1 -> buscarTarefas("KMP", SearchAlgorithm.KMP);
                case 2 -> buscarTarefas("Boyer-Moore", SearchAlgorithm.BOYER_MOORE);
                case 3 -> buscarUsuarios("KMP", SearchAlgorithm.KMP);
                case 4 -> buscarUsuarios("Boyer-Moore", SearchAlgorithm.BOYER_MOORE);
                case 0 -> System.out.println("Voltando...");
                default -> System.out.println("Opção inválida!");
            }
        } while (opcao != 0);
    }
    
    private void buscarTarefas(String algoritmoNome, SearchAlgorithm algoritmo) {
        System.out.print("\nDigite o padrão a ser buscado: ");
        String pattern = scanner.nextLine();
        
        if (pattern == null || pattern.isEmpty()) {
            System.out.println("Padrão não pode ser vazio!");
            return;
        }
        
        long inicio = System.nanoTime();
        try {
            List<Tarefa> results = searchManager.searchTarefas(pattern, algoritmo);
            long fim = System.nanoTime();
            
            System.out.println("\n=== Resultados da Busca (" + algoritmoNome + ") ===");
            System.out.println("Padrão: \"" + pattern + "\"");
            System.out.println("Encontrados: " + results.size() + " tarefa(s)");
            System.out.printf("Tempo de busca: %.3f ms\n", (fim - inicio) / 1_000_000.0);
            
            if (!results.isEmpty()) {
                System.out.println("\n--- Detalhes ---");
                for (Tarefa t : results) {
                    System.out.println(t);
                    System.out.println("---");
                }
            }
            
        } catch (Exception e) {
            System.out.println("Erro na busca: " + e.getMessage());
        }
    }
    
    private void buscarUsuarios(String algoritmoNome, SearchAlgorithm algoritmo) {
        System.out.print("\nDigite o padrão a ser buscado: ");
        String pattern = scanner.nextLine();
        
        if (pattern == null || pattern.isEmpty()) {
            System.out.println("Padrão não pode ser vazio!");
            return;
        }
        
        long inicio = System.nanoTime();
        try {
            List<Usuario> results = searchManager.searchUsuarios(pattern, algoritmo);
            long fim = System.nanoTime();
            
            System.out.println("\n=== Resultados da Busca (" + algoritmoNome + ") ===");
            System.out.println("Padrão: \"" + pattern + "\"");
            System.out.println("Encontrados: " + results.size() + " usuário(s)");
            System.out.printf("Tempo de busca: %.3f ms\n", (fim - inicio) / 1_000_000.0);
            
            if (!results.isEmpty()) {
                System.out.println("\n--- Detalhes ---");
                for (Usuario u : results) {
                    System.out.println(u);
                    System.out.println("---");
                }
            }
            
        } catch (Exception e) {
            System.out.println("Erro na busca: " + e.getMessage());
        }
    }
}