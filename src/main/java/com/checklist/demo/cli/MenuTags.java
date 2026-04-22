package com.checklist.demo.cli;

import java.util.Scanner;
import com.checklist.demo.dao.TagDAO;
import com.checklist.demo.model.Tag;

/**
 * Classe de menu para a entidade Tag.
 * Fornece interface interativa para operações CRUD de tags.
 */
public class MenuTags {
    // Explicado em docs/aux/menuTags/menuTags.md
    private final TagDAO tagDAO;
    private final Scanner console = new Scanner(System.in);

    /**
     * Construtor da classe MenuTags.
     * Inicializa o DAO de tags.
     * 
     * @throws Exception Se houver erro na inicialização do DAO
     */
    // Explicado em docs/aux/menuTags/construtor.md
    public MenuTags() throws Exception {
        tagDAO = new TagDAO();
    }

    /**
     * Menu principal de tags.
     * Apresenta as opções e gerencia o fluxo do submenu.
     */
    // Explicado em docs/aux/menuTags/menu.md
    public void menu() {
        int opcao;
        do {
            System.out.println("\n\nAEDsIII");
            System.out.println("-------");
            System.out.println("> Início > Tags");
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
                case 1 -> buscarTag();
                case 2 -> incluirTag();
                case 3 -> alterarTag();
                case 4 -> excluirTag();
                case 0 -> System.out.println("Voltando...");
                default -> System.out.println("Opção inválida!");
            }
        } while (opcao != 0);
        
        try {
            tagDAO.close();
        } catch (Exception e) {
            System.err.println("Erro ao fechar conexão: " + e.getMessage());
        }
    }

    /**
     * Busca uma tag pelo ID.
     * Solicita o ID ao usuário e exibe os dados se encontrado.
     */
    // Explicado em docs/aux/menuTags/buscarTag.md
    private void buscarTag() {
        System.out.print("\nID da tag: ");
        int id = console.nextInt();
        console.nextLine();
        try {
            Tag tag = tagDAO.buscarTag(id);
            if (tag != null) {
                System.out.println(tag);
            } else {
                System.out.println("Tag não encontrada.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar tag: " + e.getMessage());
        }
    }

    /**
     * Inclui uma nova tag.
     * Solicita o nome da tag ao usuário.
     */
    // Explicado em docs/aux/menuTags/incluirTag.md
    private void incluirTag() {
        System.out.println("\nInclusão de tag");

        System.out.print("Nome da tag: ");
        String nome = console.nextLine();

        try {
            Tag tag = new Tag(nome);
            if (tagDAO.incluirTag(tag)) {
                System.out.println("Tag incluída com sucesso. ID: " + tag.getId());
            } else {
                System.out.println("Erro ao incluir tag.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao incluir tag: " + e.getMessage());
        }
    }

    /**
     * Altera os dados de uma tag existente.
     * Solicita o ID, exibe os dados atuais e permite alterar o nome.
     */
    // Explicado em docs/aux/menuTags/alterarTag.md
    private void alterarTag() {
        System.out.print("\nID da tag a ser alterada: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Tag tag = tagDAO.buscarTag(id);
            if (tag == null) {
                System.out.println("Tag não encontrada.");
                return;
            }

            System.out.println("\nDados atuais:");
            System.out.println(tag);

            System.out.print("\nNovo nome (vazio para manter): ");
            String nome = console.nextLine();
            if (!nome.isEmpty()) tag.setNome(nome);

            if (tagDAO.alterarTag(tag)) {
                System.out.println("Tag alterada com sucesso.");
            } else {
                System.out.println("Erro ao alterar tag.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao alterar tag: " + e.getMessage());
        }
    }

    /**
     * Exclui uma tag do sistema.
     * Solicita o ID, exibe os dados, pede confirmação e remove se possível.
     * Verifica se não existem tarefas associadas antes de excluir.
     */
    // Explicado em docs/aux/menuTags/excluirTag.md
    private void excluirTag() {
        System.out.print("\nID da tag a ser excluída: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Tag tag = tagDAO.buscarTag(id);
            if (tag == null) {
                System.out.println("Tag não encontrada.");
                return;
            }

            System.out.println("\nTag a ser excluída:");
            System.out.println(tag);

            System.out.print("\nConfirma exclusão? (S/N): ");
            char resp = console.next().charAt(0);
            console.nextLine();
            
            if (resp == 'S' || resp == 's') {
                if (tagDAO.excluirTag(id)) {
                    System.out.println("Tag excluída com sucesso.");
                } else {
                    System.out.println("Erro ao excluir tag.");
                }
            } else {
                System.out.println("Exclusão cancelada.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao excluir tag: " + e.getMessage());
        }
    }
}
