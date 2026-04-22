package com.checklist.demo;

import java.util.Scanner;

/**
 * Classe de menu para a entidade Usuario.
 * Fornece interface interativa para operações CRUD de usuários.
 */
public class MenuUsuarios {
    // Explicado em docs/aux/menuUsuarios/menuUsuarios.md
    private final UsuarioDAO usuarioDAO;
    private final Scanner console = new Scanner(System.in);

    /**
     * Construtor da classe MenuUsuarios.
     * Inicializa o DAO de usuários.
     * 
     * @throws Exception Se houver erro na inicialização do DAO
     */
    // Explicado em docs/aux/menuUsuarios/construtor.md
    public MenuUsuarios() throws Exception {
        usuarioDAO = new UsuarioDAO();
    }

    /**
     * Menu principal de usuários.
     * Apresenta as opções e gerencia o fluxo do submenu.
     */
    // Explicado em docs/aux/menuUsuarios/menu.md
    public void menu() {
        int opcao;
        do {
            System.out.println("\n\nAEDsIII");
            System.out.println("-------");
            System.out.println("> Início > Usuários");
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
                case 1 -> buscarUsuario();
                case 2 -> incluirUsuario();
                case 3 -> alterarUsuario();
                case 4 -> excluirUsuario();
                case 0 -> {}
                default -> System.out.println("Opção inválida!");
            }
        } while (opcao != 0);
        
        try {
            usuarioDAO.close();
        } catch (Exception e) {
            System.err.println("Erro ao fechar conexão: " + e.getMessage());
        }
    }

    /**
     * Busca um usuário pelo ID.
     * Solicita o ID ao usuário e exibe os dados se encontrado.
     */
    // Explicado em docs/aux/menuUsuarios/buscarUsuario.md
    private void buscarUsuario() {
        System.out.print("\nID do usuário: ");
        int id = console.nextInt();
        console.nextLine();
        try {
            Usuario usuario = usuarioDAO.buscarUsuario(id);
            if (usuario != null) {
                System.out.println(usuario);
            } else {
                System.out.println("Usuário não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar usuário: " + e.getMessage());
        }
    }

    /**
     * Inclui um novo usuário.
     * Solicita nome, email e senha ao usuário e cria o registro.
     */
    // Explicado em docs/aux/menuUsuarios/incluirUsuario.md
    private void incluirUsuario() {
        System.out.println("\nInclusão de usuário");

        System.out.print("Nome: ");
        String nome = console.nextLine();
        System.out.print("Email: ");
        String email = console.nextLine();
        System.out.print("Senha: ");
        String senha = console.nextLine();

        try {
            Usuario usuario = new Usuario(nome, email, senha);
            if (usuarioDAO.incluirUsuario(usuario)) {
                System.out.println("Usuário incluído com sucesso. ID: " + usuario.getId());
            } else {
                System.out.println("Erro ao incluir usuário.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao incluir usuário: " + e.getMessage());
        }
    }

    /**
     * Altera os dados de um usuário existente.
     * Solicita o ID, exibe os dados atuais e permite alterar campos.
     */
    // Explicado em docs/aux/menuUsuarios/alterarUsuario.md
    private void alterarUsuario() {
        System.out.print("\nID do usuário a ser alterado: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Usuario usuario = usuarioDAO.buscarUsuario(id);
            if (usuario == null) {
                System.out.println("Usuário não encontrado.");
                return;
            }

            System.out.println("\nDados atuais:");
            System.out.println(usuario);

            System.out.print("\nNovo nome (vazio para manter): ");
            String nome = console.nextLine();
            if (!nome.isEmpty()) usuario.setNome(nome);

            System.out.print("Novo email (vazio para manter): ");
            String email = console.nextLine();
            if (!email.isEmpty()) usuario.setEmail(email);

            System.out.print("Nova senha (vazio para manter): ");
            String senha = console.nextLine();
            if (!senha.isEmpty()) usuario.setSenha(senha);

            if (usuarioDAO.alterarUsuario(usuario)) {
                System.out.println("Usuário alterado com sucesso.");
            } else {
                System.out.println("Erro ao alterar usuário.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao alterar usuário: " + e.getMessage());
        }
    }

    /**
     * Exclui um usuário do sistema.
     * Solicita o ID, exibe os dados, pede confirmação e remove se possível.
     * Verifica dependências antes de excluir (tarefas e categorias).
     */
    // Explicado em docs/aux/menuUsuarios/excluirUsuario.md
    private void excluirUsuario() {
        System.out.print("\nID do usuário a ser excluído: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Usuario usuario = usuarioDAO.buscarUsuario(id);
            if (usuario == null) {
                System.out.println("Usuário não encontrado.");
                return;
            }

            System.out.println("\nUsuário a ser excluído:");
            System.out.println(usuario);

            System.out.print("\nConfirma exclusão? (S/N): ");
            char resp = console.next().charAt(0);
            console.nextLine();
            
            if (resp == 'S' || resp == 's') {
                if (usuarioDAO.excluirUsuario(id)) {
                    System.out.println("Usuário excluído com sucesso.");
                } else {
                    System.out.println("Erro ao excluir usuário.");
                }
            } else {
                System.out.println("Exclusão cancelada.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao excluir usuário: " + e.getMessage());
        }
    }
}