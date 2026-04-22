import java.util.ArrayList;
import java.util.List;

/**
 * Gerenciador do relacionamento 1:N entre Usuario e Tarefa.
 * Um usuário pode ter várias tarefas.
 * 
 * Armazena a lista de IDs de tarefas para cada usuário usando
 * um arquivo de índice com chave = idUsuario e valor = lista de ids das tarefas.
 */
public class UsuarioTarefasManager {
    // Explicado em docs/aux/usuarioTarefasManager/usuarioTarefasManager.md
    private ArquivoIndex<RegistroListaTarefas> arquivoRelacoes;
    
    /**
     * Construtor da classe UsuarioTarefasManager.
     * Inicializa o arquivo que armazena as relações.
     * 
     * @throws Exception Se houver erro na inicialização
     */
    // Explicado em docs/aux/usuarioTarefasManager/construtor.md
    public UsuarioTarefasManager() throws Exception {
        arquivoRelacoes = new ArquivoIndex<>("usuario_tarefas_rel", 
                                              RegistroListaTarefas.class.getConstructor());
    }
    
    /**
     * Adiciona uma tarefa à lista de tarefas de um usuário.
     * 
     * @param idUsuario Identificador do usuário
     * @param idTarefa Identificador da tarefa a ser adicionada
     * @throws Exception Se houver erro na operação
     */
    // Explicado em docs/aux/usuarioTarefasManager/adicionarTarefa.md
    public void adicionarTarefa(int idUsuario, int idTarefa) throws Exception {
        // Buscar registro existente para este usuário
        RegistroListaTarefas registro = arquivoRelacoes.read(idUsuario);
        List<Integer> tarefas;
        
        if (registro == null) {
            tarefas = new ArrayList<>();
        } else {
            tarefas = registro.getTarefas();
        }
        
        // Evita duplicação
        if (!tarefas.contains(idTarefa)) {
            tarefas.add(idTarefa);
            RegistroListaTarefas novoRegistro = new RegistroListaTarefas(idUsuario, tarefas);
            
            if (registro == null) {
                arquivoRelacoes.create(novoRegistro);
            } else {
                arquivoRelacoes.update(novoRegistro);
            }
        }
    }
    
    /**
     * Remove uma tarefa da lista de tarefas de um usuário.
     * 
     * @param idUsuario Identificador do usuário
     * @param idTarefa Identificador da tarefa a ser removida
     * @throws Exception Se houver erro na operação
     */
    // Explicado em docs/aux/usuarioTarefasManager/removerTarefa.md
    public void removerTarefa(int idUsuario, int idTarefa) throws Exception {
        RegistroListaTarefas registro = arquivoRelacoes.read(idUsuario);
        if (registro != null) {
            List<Integer> tarefas = registro.getTarefas();
            tarefas.remove(Integer.valueOf(idTarefa));
            
            if (tarefas.isEmpty()) {
                // Se não há mais tarefas, remove o registro
                arquivoRelacoes.delete(idUsuario);
            } else {
                // Atualiza a lista
                registro.setTarefas(tarefas);
                arquivoRelacoes.update(registro);
            }
        }
    }
    
    /**
     * Busca todas as tarefas associadas a um usuário.
     * 
     * @param idUsuario Identificador do usuário
     * @return Lista de IDs das tarefas do usuário
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/usuarioTarefasManager/buscarTarefasDoUsuario.md
    public List<Integer> buscarTarefasDoUsuario(int idUsuario) throws Exception {
        RegistroListaTarefas registro = arquivoRelacoes.read(idUsuario);
        if (registro != null) {
            return registro.getTarefas();
        }
        return new ArrayList<>();
    }
    
    /**
     * Conta quantas tarefas estão associadas a um usuário.
     * 
     * @param idUsuario Identificador do usuário
     * @return Número de tarefas do usuário
     * @throws Exception Se houver erro na contagem
     */
    // Explicado em docs/aux/usuarioTarefasManager/contarTarefasDoUsuario.md
    public int contarTarefasDoUsuario(int idUsuario) throws Exception {
        return buscarTarefasDoUsuario(idUsuario).size();
    }
    
    /**
     * Fecha a conexão com o arquivo de relações.
     * 
     * @throws Exception Se houver erro no fechamento
     */
    // Explicado em docs/aux/usuarioTarefasManager/close.md
    public void close() throws Exception {
        arquivoRelacoes.close();
    }
}