import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;

/**
 * Classe genérica para gerenciamento de arquivos de dados com controle de
 * registros deletados e reutilização de espaço.
 * 
 * @param <T> Tipo de registro que implementa a interface Registro
 */
public class Arquivo<T extends Registro> {
    // Explicado em docs/aux/arquivo/arquivo.md
    private static final int TAM_CABECALHO = 12;
    private final RandomAccessFile arquivo;
    private final String nomeArquivo;
    private final Constructor<T> construtor;

    /**
     * Construtor da classe Arquivo.
     * Cria o diretório de dados e inicializa o arquivo com cabeçalho.
     * 
     * @param nomeArquivo Nome base do arquivo a ser criado/aberto
     * @param construtor Construtor da classe T para instanciação dinâmica
     * @throws Exception Se houver erro na criação do arquivo ou diretório
     */
    // Explicado em docs/aux/arquivo/construtor.md
    public Arquivo(String nomeArquivo, Constructor<T> construtor) throws Exception {
        File diretorio = new File("./dados");
        if (!diretorio.exists()) {
            diretorio.mkdir();
        }

        this.nomeArquivo = "./dados/" + nomeArquivo + ".db";
        this.construtor = construtor;
        this.arquivo = new RandomAccessFile(this.nomeArquivo, "rw");

        if (arquivo.length() == 0) {
            arquivo.writeInt(0);    // Último ID usado (4 bytes)
            arquivo.writeLong(-1);  // Ponteiro para o primeiro registro deletado (8 bytes)
        }
    }

    /**
     * Cria um novo registro no arquivo.
     * Incrementa o último ID, escreve o registro e gerencia o espaço livre.
     * 
     * @param obj Objeto a ser persistido
     * @return ID gerado para o novo registro
     * @throws Exception Se houver erro na escrita
     */
    // Explicado em docs/aux/arquivo/create.md
    public int create(T obj) throws Exception {
        arquivo.seek(0);
        int novoID = arquivo.readInt() + 1;
        arquivo.seek(0);
        arquivo.writeInt(novoID);
        obj.setId(novoID);
        byte[] dados = obj.toByteArray();

        long endereco = getDeleted(dados.length);
        if (endereco == -1) {
            arquivo.seek(arquivo.length());
            endereco = arquivo.getFilePointer();
            arquivo.writeByte(' ');      // Lápide: ' ' = registro ativo
            arquivo.writeShort(dados.length);
            arquivo.write(dados);
        } else {
            arquivo.seek(endereco);
            arquivo.writeByte(' ');      // Remove a lápide (marca como ativo)
            arquivo.skipBytes(2);        // Pula o campo de tamanho
            arquivo.write(dados);
        }
        return obj.getId();
    }

    /**
     * Busca um registro pelo ID.
     * Percorre o arquivo sequencialmente até encontrar o registro solicitado.
     * 
     * @param id Identificador do registro a ser buscado
     * @return Objeto encontrado ou null se não existir
     * @throws Exception Se houver erro na leitura
     */
    // Explicado em docs/aux/arquivo/read.md
    public T read(int id) throws Exception {
        arquivo.seek(TAM_CABECALHO);
        while (arquivo.getFilePointer() < arquivo.length()) {
            byte lapide = arquivo.readByte();
            short tamanho = arquivo.readShort();
            if (tamanho <= 0) {
                break;
            }
            byte[] dados = new byte[tamanho];
            arquivo.read(dados);

            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                if (obj.getId() == id) {
                    return obj;
                }
            }
        }
        return null;
    }

    /**
     * Remove um registro do arquivo (exclusão lógica).
     * Marca o registro com '*' e adiciona à lista de deletados.
     * 
     * @param id Identificador do registro a ser removido
     * @return true se o registro foi removido, false caso contrário
     * @throws Exception Se houver erro na operação
     */
    // Explicado em docs/aux/arquivo/delete.md
    public boolean delete(int id) throws Exception {
        arquivo.seek(TAM_CABECALHO);
        while (arquivo.getFilePointer() < arquivo.length()) {
            long posicao = arquivo.getFilePointer();
            byte lapide = arquivo.readByte();
            short tamanho = arquivo.readShort();
            if (tamanho <= 0) {
                break;
            }
            byte[] dados = new byte[tamanho];
            arquivo.read(dados);

            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                if (obj.getId() == id) {
                    arquivo.seek(posicao);
                    arquivo.writeByte('*');  // '*' = registro deletado
                    addDeleted(tamanho, posicao);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Atualiza um registro existente.
     * Se o novo registro couber no mesmo espaço, atualiza in-place.
     * Caso contrário, marca o antigo como deletado e cria um novo.
     * 
     * @param novoObj Objeto com os dados atualizados
     * @return true se a atualização foi bem-sucedida, false caso contrário
     * @throws Exception Se houver erro na operação
     */
    // Explicado em docs/aux/arquivo/update.md
    public boolean update(T novoObj) throws Exception {
        arquivo.seek(TAM_CABECALHO);
        while (arquivo.getFilePointer() < arquivo.length()) {
            long posicao = arquivo.getFilePointer();
            byte lapide = arquivo.readByte();
            short tamanho = arquivo.readShort();
            if (tamanho <= 0) {
                break;
            }
            byte[] dados = new byte[tamanho];
            arquivo.read(dados);

            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                if (obj.getId() == novoObj.getId()) {
                    byte[] novosDados = novoObj.toByteArray();
                    short novoTam = (short) novosDados.length;

                    if (novoTam <= tamanho) {
                        // Atualiza no mesmo lugar
                        arquivo.seek(posicao + 3);
                        arquivo.write(novosDados);
                    } else {
                        // Marca o antigo como deletado
                        arquivo.seek(posicao);
                        arquivo.writeByte('*');
                        addDeleted(tamanho, posicao);

                        // Procura espaço para o novo registro
                        long novoEndereco = getDeleted(novosDados.length);
                        if (novoEndereco == -1) {
                            arquivo.seek(arquivo.length());
                            arquivo.writeByte(' ');
                            arquivo.writeShort(novoTam);
                            arquivo.write(novosDados);
                        } else {
                            arquivo.seek(novoEndereco);
                            arquivo.writeByte(' ');
                            arquivo.skipBytes(2);
                            arquivo.write(novosDados);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Adiciona um espaço vazio à lista encadeada de registros deletados.
     * Os registros deletados são organizados em uma lista ligada para reaproveitamento.
     * 
     * @param tamanhoEspaco Tamanho do espaço disponível
     * @param enderecoEspaco Posição inicial do espaço vazio
     * @throws Exception Se houver erro na escrita
     */
    // Explicado em docs/aux/arquivo/addDeleted.md
    private void addDeleted(int tamanhoEspaco, long enderecoEspaco) throws Exception {
        long cabecalhoLista = 4;
        arquivo.seek(cabecalhoLista);
        long primeiroDeletado = arquivo.readLong();
        
        arquivo.seek(enderecoEspaco + 1);
        arquivo.writeShort(tamanhoEspaco);
        arquivo.writeLong(primeiroDeletado);
        
        arquivo.seek(cabecalhoLista);
        arquivo.writeLong(enderecoEspaco);
    }

    /**
     * Busca um espaço vazio na lista de deletados que seja suficiente
     * para armazenar um novo registro do tamanho especificado.
     * 
     * @param tamanhoNecessario Tamanho necessário para o novo registro
     * @return Endereço do espaço encontrado ou -1 se não houver espaço adequado
     * @throws Exception Se houver erro na leitura
     */
    // Explicado em docs/aux/arquivo/getDeleted.md
    private long getDeleted(int tamanhoNecessario) throws Exception {
        long cabecalhoLista = 4;
        arquivo.seek(cabecalhoLista);
        long enderecoAtual = arquivo.readLong();
        long enderecoAnterior = -1;
        
        while (enderecoAtual != -1) {
            arquivo.seek(enderecoAtual + 1);
            int tamanho = arquivo.readShort();
            long proximo = arquivo.readLong();
            
            if (tamanho >= tamanhoNecessario) {
                // Remove este registro da lista de deletados
                if (enderecoAnterior == -1) {
                    arquivo.seek(cabecalhoLista);
                    arquivo.writeLong(proximo);
                } else {
                    arquivo.seek(enderecoAnterior + 1 + 2);
                    arquivo.writeLong(proximo);
                }
                return enderecoAtual;
            }
            
            enderecoAnterior = enderecoAtual;
            enderecoAtual = proximo;
        }
        
        return -1;
    }

    /**
     * Fecha o arquivo liberando os recursos do sistema.
     * 
     * @throws Exception Se houver erro no fechamento
     */
    public void close() throws Exception {
        arquivo.close();
    }
}