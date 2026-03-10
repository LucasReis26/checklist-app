import java.util.Scanner;

public class MenuUsuarios {
    private UsuarioDAO usuarioDAO;
    private Scanner console = new Scanner(System.in);

    public MenuUsuarios() throws Exception {
        usuarioDAO = new UsuarioDAO();
    }

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
                opcao = Integer.valueOf(console.nextLine());
            } catch(NumberFormatException e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1:
                    buscarUsuario();
                    break;
                case 2:
                    incluirUsuario();
                    break;
                case 3:
                    alterarUsuario();
                    break;
                case 4:
                    excluirUsuario();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        } while (opcao != 0);
    }

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
            System.out.println("Erro ao buscar usuário.");
        }
    }

    private void incluirUsuario() {
        System.out.println("\nInclusão de usuário");

        System.out.print("\nNome: ");
        String nome = console.nextLine();
        System.out.print("Email: ");
        String email = console.nextLine();
        System.out.print("Senha: ");
        String senha = console.nextLine();

        try {
            Usuario usuario = new Usuario(nome, email, senha);
            if (usuarioDAO.incluirUsuario(usuario)) {
                System.out.println("Usuário incluído com sucesso.");
            } else {
                System.out.println("Erro ao incluir usuário.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao incluir usuário.");
        }
    }

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
            System.out.println("Erro ao alterar usuário.");
        }
    }

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

            System.out.print("Confirma exclusão? (S/N): ");
            char resp = console.next().charAt(0);
            if (resp == 'S' || resp == 's') {
                if (usuarioDAO.excluirUsuario(id)) {
                    System.out.println("Usuário excluído com sucesso.");
                } else {
                    System.out.println("Erro ao excluir usuário.");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao excluir usuário.");
        }
    }
}