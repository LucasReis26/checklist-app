import java.util.Scanner;

/**
 * Classe de menu para a entidade LogConclusao.
 * Fornece interface interativa para operações CRUD de logs de conclusão.
 */
public class MenuLogs {
    // Explicado em docs/aux/menuLogs/menuLogs.md
    private final LogConclusaoDAO logDAO;
    private final Scanner console = new Scanner(System.in);

    /**
     * Construtor da classe MenuLogs.
     * Inicializa o DAO de logs.
     * 
     * @throws Exception Se houver erro na inicialização do DAO
     */
    // Explicado em docs/aux/menuLogs/construtor.md
    public MenuLogs() throws Exception {
        logDAO = new LogConclusaoDAO();
    }

    /**
     * Menu principal de logs.
     * Apresenta as opções e gerencia o fluxo do submenu.
     */
    // Explicado em docs/aux/menuLogs/menu.md
    public void menu() {
        int opcao;
        do {
            System.out.println("\n\nAEDsIII");
            System.out.println("-------");
            System.out.println("> Início > Logs de Conclusão");
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
                case 1 -> buscarLog();
                case 2 -> incluirLog();
                case 3 -> alterarLog();
                case 4 -> excluirLog();
                case 0 -> {}
                default -> System.out.println("Opção inválida!");
            }
        } while (opcao != 0);
        
        try {
            logDAO.close();
        } catch (Exception e) {
            System.err.println("Erro ao fechar conexão: " + e.getMessage());
        }
    }

    /**
     * Busca um log pelo ID.
     * Solicita o ID ao usuário e exibe os dados se encontrado.
     */
    // Explicado em docs/aux/menuLogs/buscarLog.md
    private void buscarLog() {
        System.out.print("\nID do log: ");
        int id = console.nextInt();
        console.nextLine();
        try {
            LogConclusao log = logDAO.buscarLog(id);
            if (log != null) {
                System.out.println(log);
            } else {
                System.out.println("Log não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar log: " + e.getMessage());
        }
    }

    /**
     * Inclui um novo log de conclusão.
     * Solicita ID da tarefa, data de conclusão e resumo das tags.
     */
    // Explicado em docs/aux/menuLogs/incluirLog.md
    private void incluirLog() {
        System.out.println("\nInclusão de log de conclusão");

        System.out.print("ID da tarefa: ");
        int idTarefa = console.nextInt();
        console.nextLine();
        
        System.out.print("Data de conclusão (YYYY-MM-DD HH:MM:SS): ");
        String dataConclusao = console.nextLine();
        
        System.out.print("Resumo das tags: ");
        String resumoTags = console.nextLine();

        try {
            LogConclusao log = new LogConclusao(idTarefa, dataConclusao, resumoTags);
            if (logDAO.incluirLog(log)) {
                System.out.println("Log incluído com sucesso. ID: " + log.getId());
            } else {
                System.out.println("Erro ao incluir log.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao incluir log: " + e.getMessage());
        }
    }

    /**
     * Altera os dados de um log existente.
     * Solicita o ID, exibe os dados atuais e permite alterar campos.
     */
    // Explicado em docs/aux/menuLogs/alterarLog.md
    private void alterarLog() {
        System.out.print("\nID do log a ser alterado: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            LogConclusao log = logDAO.buscarLog(id);
            if (log == null) {
                System.out.println("Log não encontrado.");
                return;
            }

            System.out.println("\nDados atuais:");
            System.out.println(log);

            System.out.print("\nNova data de conclusão (vazio para manter): ");
            String dataConclusao = console.nextLine();
            if (!dataConclusao.isEmpty()) log.setDataConclusao(dataConclusao);

            System.out.print("Novo resumo de tags (vazio para manter): ");
            String resumoTags = console.nextLine();
            if (!resumoTags.isEmpty()) log.setResumoTags(resumoTags);

            if (logDAO.alterarLog(log)) {
                System.out.println("Log alterado com sucesso.");
            } else {
                System.out.println("Erro ao alterar log.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao alterar log: " + e.getMessage());
        }
    }

    /**
     * Exclui um log do sistema.
     * Solicita o ID, exibe os dados, pede confirmação e remove.
     */
    // Explicado em docs/aux/menuLogs/excluirLog.md
    private void excluirLog() {
        System.out.print("\nID do log a ser excluído: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            LogConclusao log = logDAO.buscarLog(id);
            if (log == null) {
                System.out.println("Log não encontrado.");
                return;
            }

            System.out.println("\nLog a ser excluído:");
            System.out.println(log);

            System.out.print("\nConfirma exclusão? (S/N): ");
            char resp = console.next().charAt(0);
            console.nextLine();
            
            if (resp == 'S' || resp == 's') {
                if (logDAO.excluirLog(id)) {
                    System.out.println("Log excluído com sucesso.");
                } else {
                    System.out.println("Erro ao excluir log.");
                }
            } else {
                System.out.println("Exclusão cancelada.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao excluir log: " + e.getMessage());
        }
    }
}