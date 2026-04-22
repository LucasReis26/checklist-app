package com.checklist.demo;

import java.util.Scanner;

/**
 * Classe de menu para a entidade Categoria.
 * Fornece interface interativa para operações CRUD de categorias.
 */
public class MenuCategorias {
    // Explicado em docs/aux/menuCategorias/menuCategorias.md
    private final CategoriaDAO categoriaDAO;
    private final Scanner console = new Scanner(System.in);

    /**
     * Construtor da classe MenuCategorias.
     * Inicializa o DAO de categorias.
     * 
     * @throws Exception Se houver erro na inicialização do DAO
     */
    // Explicado em docs/aux/menuCategorias/construtor.md
    public MenuCategorias() throws Exception {
        categoriaDAO = new CategoriaDAO();
    }

    /**
     * Menu principal de categorias.
     * Apresenta as opções e gerencia o fluxo do submenu.
     */
    // Explicado em docs/aux/menuCategorias/menu.md
    public void menu() {
        int opcao;
        do {
            System.out.println("\n\nAEDsIII");
            System.out.println("-------");
            System.out.println("> Início > Categorias");
            System.out.println("\n1 - Buscar");
            System.out.println("2 - Incluir");
            System.out.println("3 - Alterar");
            System.out.println("4 - Excluir");
            System.out.println("0 - Voltar");

            System.out.print("\nOpção: ");
            try {
                opcao = Integer.parseInt(console.nextLine());
            } catch (NumberFormatException e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1 -> buscarCategoria();
                case 2 -> incluirCategoria();
                case 3 -> alterarCategoria();
                case 4 -> excluirCategoria();
                case 0 -> {}
                default -> System.out.println("Opção inválida!");
            }
        } while (opcao != 0);
        
        try {
            categoriaDAO.close();
        } catch (Exception e) {
            System.err.println("Erro ao fechar conexão: " + e.getMessage());
        }
    }

    /**
     * Busca uma categoria pelo ID.
     * Solicita o ID ao usuário e exibe os dados se encontrado.
     */
    // Explicado em docs/aux/menuCategorias/buscarCategoria.md
    private void buscarCategoria() {
        System.out.print("\nID da categoria: ");
        int id = console.nextInt();
        console.nextLine();
        try {
            Categoria categoria = categoriaDAO.buscarCategoria(id);
            if (categoria != null) {
                System.out.println(categoria);
            } else {
                System.out.println("Categoria não encontrada.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar categoria: " + e.getMessage());
        }
    }

    /**
     * Inclui uma nova categoria.
     * Solicita ID do usuário e nome da categoria.
     */
    // Explicado em docs/aux/menuCategorias/incluirCategoria.md
    private void incluirCategoria() {
        System.out.println("\nInclusão de categoria");

        System.out.print("ID do usuário: ");
        int idUser = console.nextInt();
        console.nextLine();
        
        System.out.print("Nome da categoria: ");
        String nome = console.nextLine();

        try {
            Categoria categoria = new Categoria(idUser, nome);
            if (categoriaDAO.incluirCategoria(categoria)) {
                System.out.println("Categoria incluída com sucesso. ID: " + categoria.getId());
            } else {
                System.out.println("Erro ao incluir categoria.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao incluir categoria: " + e.getMessage());
        }
    }

    /**
     * Altera os dados de uma categoria existente.
     * Solicita o ID, exibe os dados atuais e permite alterar o nome.
     */
    // Explicado em docs/aux/menuCategorias/alterarCategoria.md
    private void alterarCategoria() {
        System.out.print("\nID da categoria a ser alterada: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Categoria categoria = categoriaDAO.buscarCategoria(id);
            if (categoria == null) {
                System.out.println("Categoria não encontrada.");
                return;
            }

            System.out.println("\nDados atuais:");
            System.out.println(categoria);

            System.out.print("\nNovo nome (vazio para manter): ");
            String nome = console.nextLine();
            if (!nome.isEmpty()) categoria.setNome(nome);

            if (categoriaDAO.alterarCategoria(categoria)) {
                System.out.println("Categoria alterada com sucesso.");
            } else {
                System.out.println("Erro ao alterar categoria.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao alterar categoria: " + e.getMessage());
        }
    }

    /**
     * Exclui uma categoria do sistema.
     * Solicita o ID, exibe os dados, pede confirmação e remove se possível.
     * Verifica se não existem tarefas associadas antes de excluir.
     */
    // Explicado em docs/aux/menuCategorias/excluirCategoria.md
    private void excluirCategoria() {
        System.out.print("\nID da categoria a ser excluída: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Categoria categoria = categoriaDAO.buscarCategoria(id);
            if (categoria == null) {
                System.out.println("Categoria não encontrada.");
                return;
            }

            System.out.println("\nCategoria a ser excluída:");
            System.out.println(categoria);

            System.out.print("\nConfirma exclusão? (S/N): ");
            char resp = console.next().charAt(0);
            console.nextLine();
            
            if (resp == 'S' || resp == 's') {
                if (categoriaDAO.excluirCategoria(id)) {
                    System.out.println("Categoria excluída com sucesso.");
                } else {
                    System.out.println("Erro ao excluir categoria.");
                }
            } else {
                System.out.println("Exclusão cancelada.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao excluir categoria: " + e.getMessage());
        }
    }
}