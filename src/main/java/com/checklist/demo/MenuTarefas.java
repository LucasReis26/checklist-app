package com.checklist.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Classe de menu para a entidade Tarefa.
 * Fornece interface interativa para operações CRUD de tarefas.
 * Inclui funcionalidades específicas como conclusão de tarefas e logs.
 */
public class MenuTarefas {
    // Explicado em docs/aux/menuTarefas/menuTarefas.md
    private final TarefaDAO tarefaDAO;
    private final Scanner console = new Scanner(System.in);

    /**
     * Construtor da classe MenuTarefas.
     * Inicializa o DAO de tarefas.
     * 
     * @throws Exception Se houver erro na inicialização do DAO
     */
    // Explicado em docs/aux/menuTarefas/construtor.md
    public MenuTarefas() throws Exception {
        tarefaDAO = new TarefaDAO();
    }

    /**
     * Menu principal de tarefas.
     * Apresenta as opções e gerencia o fluxo do submenu.
     */
    // Explicado em docs/aux/menuTarefas/menu.md
    public void menu() {
        int opcao;
        do {
            System.out.println("\n\nAEDsIII");
            System.out.println("-------");
            System.out.println("> Início > Tarefas");
            System.out.println("\n1 - Buscar por ID");
            System.out.println("2 - Buscar por Usuário");
            System.out.println("3 - Buscar por Categoria");
            System.out.println("4 - Buscar por Status");
            System.out.println("5 - Incluir");
            System.out.println("6 - Alterar");
            System.out.println("7 - Excluir");
            System.out.println("8 - Concluir Tarefa");
            System.out.println("9 - Listar Todas");
            System.out.println("0 - Voltar");

            System.out.print("\nOpção: ");
            try {
                opcao = Integer.parseInt(console.nextLine());
            } catch (NumberFormatException e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1 -> buscarPorId();
                case 2 -> buscarPorUsuario();
                case 3 -> buscarPorCategoria();
                case 4 -> buscarPorStatus();
                case 5 -> incluirTarefa();
                case 6 -> alterarTarefa();
                case 7 -> excluirTarefa();
                case 8 -> concluirTarefa();
                case 9 -> listarTodas();
                case 0 -> {}
                default -> System.out.println("Opção inválida!");
            }
        } while (opcao != 0);
        
        try {
            tarefaDAO.close();
        } catch (Exception e) {
            System.err.println("Erro ao fechar conexão: " + e.getMessage());
        }
    }

    /**
     * Busca uma tarefa pelo ID.
     * Utiliza índice B+ Tree para busca eficiente.
     * Exibe também os logs de conclusão da tarefa.
     */
    // Explicado em docs/aux/menuTarefas/buscarPorId.md
    private void buscarPorId() {
        System.out.print("\nID da tarefa: ");
        int id = console.nextInt();
        console.nextLine();
        
        long inicio = System.nanoTime();
        try {
            Tarefa tarefa = tarefaDAO.buscarTarefa(id);
            long fim = System.nanoTime();
            
            if (tarefa != null) {
                System.out.println(tarefa);
                
                List<Integer> logs = tarefaDAO.getLogsDaTarefa(id);
                if (!logs.isEmpty()) {
                    System.out.println("\nLogs de conclusão: " + logs.size());
                    LogConclusao ultimoLog = tarefaDAO.buscarUltimoLogDaTarefa(id);
                    if (ultimoLog != null) {
                        System.out.println("Última conclusão: " + ultimoLog.getDataConclusao());
                    }
                }
                
                System.out.printf("\nTempo de busca (B+ Tree): %.3f ms%n", (fim - inicio) / 1_000_000.0);
            } else {
                System.out.println("Tarefa não encontrada.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar tarefa: " + e.getMessage());
        }
    }

    /**
     * Busca tarefas por usuário.
     * Utiliza Hash Extensível para busca eficiente.
     * 
     * @see HashExtensivel
     */
    // Explicado em docs/aux/menuTarefas/buscarPorUsuario.md
    private void buscarPorUsuario() {
        System.out.print("\nID do usuário: ");
        int idUser = console.nextInt();
        console.nextLine();
        
        long inicio = System.nanoTime();
        try {
            List<Tarefa> tarefas = tarefaDAO.buscarTarefasPorUsuario(idUser);
            long fim = System.nanoTime();
            
            if (tarefas.isEmpty()) {
                System.out.println("Nenhuma tarefa encontrada para este usuário.");
            } else {
                System.out.println("\n=== Tarefas do Usuário " + idUser + " ===");
                for (Tarefa t : tarefas) {
                    System.out.println(t);
                    System.out.println("---");
                }
                System.out.printf("Total: %d tarefa(s)%n", tarefas.size());
                System.out.printf("Tempo de busca (Hash Extensível): %.3f ms%n", (fim - inicio) / 1_000_000.0);
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar tarefas: " + e.getMessage());
        }
    }

    /**
     * Busca tarefas por categoria.
     * Utiliza Hash Extensível para busca eficiente.
     */
    // Explicado em docs/aux/menuTarefas/buscarPorCategoria.md
    private void buscarPorCategoria() {
        System.out.print("\nID da categoria: ");
        int idCategoria = console.nextInt();
        console.nextLine();
        
        long inicio = System.nanoTime();
        try {
            List<Tarefa> tarefas = tarefaDAO.buscarTarefasPorCategoria(idCategoria);
            long fim = System.nanoTime();
            
            if (tarefas.isEmpty()) {
                System.out.println("Nenhuma tarefa encontrada para esta categoria.");
            } else {
                System.out.println("\n=== Tarefas da Categoria " + idCategoria + " ===");
                for (Tarefa t : tarefas) {
                    System.out.println(t);
                    System.out.println("---");
                }
                System.out.printf("Total: %d tarefa(s)%n", tarefas.size());
                System.out.printf("Tempo de busca (Hash Extensível): %.3f ms%n", (fim - inicio) / 1_000_000.0);
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar tarefas: " + e.getMessage());
        }
    }

    /**
     * Busca tarefas por status (pendente/concluida).
     * Utiliza scan sequencial (mais lento, mas necessário pois status não é indexado).
     */
    // Explicado em docs/aux/menuTarefas/buscarPorStatus.md
    private void buscarPorStatus() {
        System.out.print("\nStatus (pendente/concluida): ");
        String status = console.nextLine();
        
        long inicio = System.nanoTime();
        try {
            List<Tarefa> tarefas = tarefaDAO.buscarTarefasPorStatus(status);
            long fim = System.nanoTime();
            
            if (tarefas.isEmpty()) {
                System.out.println("Nenhuma tarefa encontrada com status: " + status);
            } else {
                System.out.println("\n=== Tarefas com Status '" + status + "' ===");
                for (Tarefa t : tarefas) {
                    System.out.println(t);
                    System.out.println("---");
                }
                System.out.printf("Total: %d tarefa(s)%n", tarefas.size());
                System.out.printf("Tempo de busca (Scan Sequencial): %.3f ms%n", (fim - inicio) / 1_000_000.0);
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar tarefas: " + e.getMessage());
        }
    }

    /**
     * Inclui uma nova tarefa.
     * Solicita todos os dados necessários e valida usuário e categoria.
     */
    // Explicado em docs/aux/menuTarefas/incluirTarefa.md
    private void incluirTarefa() {
        System.out.println("\nInclusão de tarefa");

        System.out.print("ID do usuário: ");
        int idUser = console.nextInt();
        console.nextLine();
        
        System.out.print("ID da categoria (0 para nenhuma): ");
        int idCategoria = console.nextInt();
        console.nextLine();
        
        System.out.print("Título: ");
        String titulo = console.nextLine();
        
        System.out.print("Descrição: ");
        String descricao = console.nextLine();
        
        System.out.print("Data de criação (YYYY-MM-DD): ");
        String dataCriacao = console.nextLine();
        
        System.out.print("Status (pendente/concluida): ");
        String status = console.nextLine();
        
        System.out.print("Data de vencimento (YYYY-MM-DD): ");
        String dataVencimento = console.nextLine();

        long inicio = System.nanoTime();
        try {
            Tarefa tarefa = new Tarefa(idUser, idCategoria, titulo, descricao, 
                                       dataCriacao, status, dataVencimento);
            if (tarefaDAO.incluirTarefa(tarefa)) {
                long fim = System.nanoTime();
                System.out.println("Tarefa incluída com sucesso! ID: " + tarefa.getId());
                System.out.printf("Tempo de inclusão: %.3f ms%n", (fim - inicio) / 1_000_000.0);
            } else {
                System.out.println("Erro ao incluir tarefa.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao incluir tarefa: " + e.getMessage());
        }
    }

    /**
     * Altera os dados de uma tarefa existente.
     * Permite alterar título, descrição, status, data de vencimento e categoria.
     */
    // Explicado em docs/aux/menuTarefas/alterarTarefa.md
    private void alterarTarefa() {
        System.out.print("\nID da tarefa a ser alterada: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Tarefa tarefa = tarefaDAO.buscarTarefa(id);
            if (tarefa == null) {
                System.out.println("Tarefa não encontrada.");
                return;
            }

            System.out.println("\nDados atuais:");
            System.out.println(tarefa);
            
            System.out.print("\nNovo título (vazio para manter): ");
            String titulo = console.nextLine();
            if (!titulo.isEmpty()) tarefa.setTitulo(titulo);

            System.out.print("Nova descrição (vazio para manter): ");
            String descricao = console.nextLine();
            if (!descricao.isEmpty()) tarefa.setDescricao(descricao);

            System.out.print("Novo status (vazio para manter): ");
            String status = console.nextLine();
            if (!status.isEmpty()) tarefa.setStatus(status);

            System.out.print("Nova data de vencimento (vazio para manter): ");
            String dataVencimento = console.nextLine();
            if (!dataVencimento.isEmpty()) tarefa.setDataVencimento(dataVencimento);

            System.out.print("Nova categoria (0 para nenhuma, vazio para manter): ");
            String catInput = console.nextLine();
            if (!catInput.isEmpty()) {
                int novaCategoria = Integer.parseInt(catInput);
                tarefa.setIdCategoria(novaCategoria);
            }

            long inicio = System.nanoTime();
            if (tarefaDAO.alterarTarefa(tarefa)) {
                long fim = System.nanoTime();
                System.out.println("Tarefa alterada com sucesso.");
                System.out.printf("Tempo de alteração: %.3f ms%n", (fim - inicio) / 1_000_000.0);
            } else {
                System.out.println("Erro ao alterar tarefa.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao alterar tarefa: " + e.getMessage());
        }
    }

    /**
     * Exclui uma tarefa do sistema.
     * Verifica se existem logs ou tags associados antes de excluir.
     */
    // Explicado em docs/aux/menuTarefas/excluirTarefa.md
    private void excluirTarefa() {
        System.out.print("\nID da tarefa a ser excluída: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Tarefa tarefa = tarefaDAO.buscarTarefa(id);
            if (tarefa == null) {
                System.out.println("Tarefa não encontrada.");
                return;
            }

            System.out.println("\nTarefa a ser excluída:");
            System.out.println(tarefa);
            
            List<Integer> logs = tarefaDAO.getLogsDaTarefa(id);
            if (!logs.isEmpty()) {
                System.out.println("\nATENÇÃO: Esta tarefa possui " + logs.size() + " log(s) de conclusão!");
            }
            
            System.out.print("\nConfirma exclusão? (S/N): ");
            char resp = console.next().charAt(0);
            console.nextLine();
            
            if (resp == 'S' || resp == 's') {
                long inicio = System.nanoTime();
                if (tarefaDAO.excluirTarefa(id)) {
                    long fim = System.nanoTime();
                    System.out.println("Tarefa excluída com sucesso.");
                    System.out.printf("Tempo de exclusão: %.3f ms%n", (fim - inicio) / 1_000_000.0);
                } else {
                    System.out.println("Erro ao excluir tarefa.");
                }
            } else {
                System.out.println("Exclusão cancelada.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao excluir tarefa: " + e.getMessage());
        }
    }

    /**
     * Conclui uma tarefa.
     * Atualiza o status para "concluida" e cria um log de conclusão.
     * Permite adicionar múltiplos logs para a mesma tarefa.
     */
    // Explicado em docs/aux/menuTarefas/concluirTarefa.md
    private void concluirTarefa() {
        System.out.print("\nID da tarefa a ser concluída: ");
        int id = console.nextInt();
        console.nextLine();
        
        try {
            Tarefa tarefa = tarefaDAO.buscarTarefa(id);
            if (tarefa == null) {
                System.out.println("Tarefa não encontrada.");
                return;
            }
            
            if ("concluida".equals(tarefa.getStatus())) {
                System.out.println("Esta tarefa já está concluída!");
                System.out.print("Deseja adicionar um novo log de conclusão? (S/N): ");
                char resp = console.next().charAt(0);
                console.nextLine();
                if (resp != 'S' && resp != 's') {
                    return;
                }
            }
            
            System.out.print("Resumo das tags (opcional): ");
            String resumoTags = console.nextLine();
            
            long inicio = System.nanoTime();
            if (tarefaDAO.concluirTarefa(id, resumoTags)) {
                long fim = System.nanoTime();
                System.out.println("Tarefa concluída com sucesso!");
                
                LogConclusao ultimoLog = tarefaDAO.buscarUltimoLogDaTarefa(id);
                if (ultimoLog != null) {
                    System.out.println("Log gerado:");
                    System.out.println("  Data: " + ultimoLog.getDataConclusao());
                    System.out.println("  Resumo: " + ultimoLog.getResumoTags());
                }
                
                System.out.printf("Tempo de conclusão: %.3f ms%n", (fim - inicio) / 1_000_000.0);
            } else {
                System.out.println("Erro ao concluir tarefa.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao concluir tarefa: " + e.getMessage());
        }
    }

    /**
     * Lista todas as tarefas cadastradas.
     * Exibe um resumo com quantidade de pendentes e concluídas.
     */
    // Explicado em docs/aux/menuTarefas/listarTodas.md
    private void listarTodas() {
        long inicio = System.nanoTime();
        try {
            List<Tarefa> tarefas = tarefaDAO.listarTodas();
            long fim = System.nanoTime();
            
            if (tarefas.isEmpty()) {
                System.out.println("Nenhuma tarefa cadastrada.");
            } else {
                System.out.println("\n=== Todas as Tarefas ===");
                int pendentes = 0;
                int concluidas = 0;
                
                for (Tarefa t : tarefas) {
                    System.out.println(t);
                    System.out.println("---");
                    
                    if ("concluida".equals(t.getStatus())) {
                        concluidas++;
                    } else {
                        pendentes++;
                    }
                }
                
                System.out.printf("\nResumo: %d pendente(s) | %d concluída(s) | Total: %d%n", 
                                 pendentes, concluidas, tarefas.size());
                System.out.printf("Tempo de listagem: %.3f ms%n", (fim - inicio) / 1_000_000.0);
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar tarefas: " + e.getMessage());
        }
    }
}