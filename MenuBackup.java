import java.util.Scanner;

/**
 * Menu para operações de backup e restauração.
 * Adicionar este menu ao Principal.java.
 */
public class MenuBackup {
    
    private final Scanner scanner;
    
    public MenuBackup() {
        this.scanner = new Scanner(System.in);
    }
    
    public void menu() {
        int opcao;
        do {
            System.out.println("\n\nAEDsIII");
            System.out.println("-------");
            System.out.println("> Início > Backup");
            System.out.println("\n1 - Backup com Huffman");
            System.out.println("2 - Backup com LZW");
            System.out.println("3 - Listar Backups");
            System.out.println("4 - Restaurar Backup Huffman");
            System.out.println("5 - Restaurar Backup LZW");
            System.out.println("0 - Voltar");
            
            System.out.print("\nOpção: ");
            try {
                opcao = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                opcao = -1;
            }
            
            switch (opcao) {
                case 1 -> {
                    try {
                        BackupManager.backupHuffman();
                    } catch (Exception e) {
                        System.out.println("Erro no backup Huffman: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                case 2 -> {
                    try {
                        BackupManager.backupLZW();
                    } catch (Exception e) {
                        System.out.println("Erro no backup LZW: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                case 3 -> BackupManager.listBackups();
                case 4 -> {
                    System.out.print("\nNome do arquivo de backup Huffman: ");
                    String filename = scanner.nextLine();
                    try {
                        BackupManager.restoreHuffman("./backups/" + filename);
                    } catch (Exception e) {
                        System.out.println("Erro na restauração: " + e.getMessage());
                    }
                }
                case 5 -> {
                    System.out.print("\nNome do arquivo de backup LZW: ");
                    String filename = scanner.nextLine();
                    try {
                        BackupManager.restoreLZW("./backups/" + filename);
                    } catch (Exception e) {
                        System.out.println("Erro na restauração: " + e.getMessage());
                    }
                }
                case 0 -> System.out.println("Voltando...");
                default -> System.out.println("Opção inválida!");
            }
        } while (opcao != 0);
    }
}