package com.checklist.ui.cli;

import com.checklist.manager.BackupManager;
import com.checklist.manager.CategoriaTarefasManager;
import com.checklist.manager.TarefaLogsManager;
import com.checklist.manager.UsuarioCategoriasManager;
import com.checklist.manager.UsuarioTarefasManager;
import java.util.Scanner;

/**
 * Classe principal do sistema de gerenciamento de tarefas.
 * Responsável por exibir o menu principal e direcionar para os submódulos.
 */
public class Principal {
    
    /**
     * Método principal que inicia a aplicação.
     * Apresenta o menu principal e gerencia o fluxo do programa.
     * 
     * @param args Argumentos da linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        Scanner console = new Scanner(System.in);
        int opcao;

        try {
            do {
                System.out.println("\n\nAEDsIII");
                System.out.println("-------");
                System.out.println("> Início");
                System.out.println("\n1 - Usuários");
                System.out.println("2 - Tarefas");
                System.out.println("3 - Categorias");
                System.out.println("4 - Tags");
                System.out.println("5 - Logs de Conclusão");
                System.out.println("6 - Backup/Restauração");
                System.out.println("7 - Buscar Padrões (KMP / Boyer-Moore)"); // NOVA OPÇÃO
                System.out.println("0 - Sair");

                System.out.print("\nOpção: ");
                try {
                    opcao = Integer.parseInt(console.nextLine());
                } catch (NumberFormatException e) {
                    opcao = -1;
                }

                switch (opcao) {
                    case 1 -> {
                        MenuUsuarios menuUsuarios = new MenuUsuarios();
                        menuUsuarios.menu();
                    }
                    case 2 -> {
                        MenuTarefas menuTarefas = new MenuTarefas();
                        menuTarefas.menu();
                    }
                    case 3 -> {
                        MenuCategorias menuCategorias = new MenuCategorias();
                        menuCategorias.menu();
                    }
                    case 4 -> {
                        MenuTags menuTags = new MenuTags();
                        menuTags.menu();
                    }
                    case 5 -> {
                        MenuLogs menuLogs = new MenuLogs();
                        menuLogs.menu();
                    }
                    case 6 -> {
                        MenuBackup menuBackup = new MenuBackup();
                        menuBackup.menu();
                    }
                    case 7 -> { // NOVA OPÇÃO
                        MenuBusca menuBusca = new MenuBusca();
                        menuBusca.menu();
                    }
                    case 0 -> System.out.println("Saindo...");
                    default -> System.out.println("Opção inválida!");
                }
            } while (opcao != 0);

        } catch (Exception e) {
            System.err.println("Erro fatal no sistema:");
            e.printStackTrace();
        } finally {
            console.close();
        }
    }
}