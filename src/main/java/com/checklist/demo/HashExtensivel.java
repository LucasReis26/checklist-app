import java.io.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação de Hash Extensível (Dynamic Hashing) para armazenamento persistente.
 * 
 * Características:
 * - Crescimento dinâmico do diretório
 * - Buckets com profundidade local
 * - Suporte a colisões por encadeamento interno
 * - Persistência em arquivo
 * 
 * @param <T> Tipo de registro que implementa a interface Registro
 */
public class HashExtensivel<T extends Registro> {
    // Explicado em docs/aux/hashExtensivel/hashExtensivel.md
    private RandomAccessFile arquivo;
    private int profundidadeGlobal;
    private int tamanhoBucket;
    private final Constructor<T> construtor;
    
    private static final int TAM_CABECALHO = 12;  // 4 bytes profGlobal + 4 bytes tamBucket + 4 bytes tamDiretorio
    
    /**
     * Construtor com tamanho de bucket padrão (4).
     * 
     * @param nomeArquivo Nome base do arquivo de hash
     * @param construtor Construtor da classe T
     * @throws Exception Se houver erro na inicialização
     */
    // Explicado em docs/aux/hashExtensivel/construtor.md
    public HashExtensivel(String nomeArquivo, Constructor<T> construtor) throws Exception {
        this(nomeArquivo, construtor, 4);
    }
    
    /**
     * Construtor com tamanho de bucket personalizado.
     * 
     * @param nomeArquivo Nome base do arquivo de hash
     * @param construtor Construtor da classe T
     * @param tamanhoBucket Capacidade máxima de registros por bucket
     * @throws Exception Se houver erro na inicialização
     */
    // Explicado em docs/aux/hashExtensivel/construtorPersonalizado.md
    public HashExtensivel(String nomeArquivo, Constructor<T> construtor, int tamanhoBucket) throws Exception {
        this.construtor = construtor;
        this.tamanhoBucket = tamanhoBucket;
        
        File diretorio = new File("./dados");
        if (!diretorio.exists()) {
            diretorio.mkdir();
        }
        
        this.arquivo = new RandomAccessFile("./dados/" + nomeArquivo + "_hash.db", "rw");
        
        if (arquivo.length() == 0) {
            this.profundidadeGlobal = 1;
            inicializarArquivo();
        } else {
            carregarCabecalho();
        }
    }
    
    /**
     * Inicializa um novo arquivo de hash com diretório de tamanho 2.
     * 
     * @throws Exception Se houver erro na escrita
     */
    // Explicado em docs/aux/hashExtensivel/inicializarArquivo.md
    private void inicializarArquivo() throws Exception {
        arquivo.seek(0);
        arquivo.writeInt(profundidadeGlobal);
        arquivo.writeInt(tamanhoBucket);
        arquivo.writeInt(1);  // Tamanho do diretório (2^1 = 2)
        
        for (int i = 0; i < 2; i++) {
            long posicaoBucket = criarBucket();
            arquivo.writeLong(posicaoBucket);
        }
    }
    
    /**
     * Carrega o cabeçalho do arquivo de hash existente.
     * 
     * @throws Exception Se houver erro na leitura
     */
    // Explicado em docs/aux/hashExtensivel/carregarCabecalho.md
    private void carregarCabecalho() throws Exception {
        arquivo.seek(0);
        profundidadeGlobal = arquivo.readInt();
        tamanhoBucket = arquivo.readInt();
        int tamanhoDiretorio = arquivo.readInt();  // Não utilizado diretamente
    }
    
    /**
     * Cria um novo bucket vazio no final do arquivo.
     * 
     * @return Posição do bucket criado
     * @throws Exception Se houver erro na escrita
     */
    // Explicado em docs/aux/hashExtensivel/criarBucket.md
    private long criarBucket() throws Exception {
        long posicao = arquivo.length();
        arquivo.seek(posicao);
        
        arquivo.writeInt(1);     // Profundidade local inicial = 1
        arquivo.writeInt(0);     // Quantidade inicial de registros = 0
        arquivo.write(new byte[tamanhoBucket * 1024]);  // Espaço reservado para registros
        
        return posicao;
    }
    
    /**
     * Lê um bucket do arquivo a partir de sua posição.
     * 
     * @param posicao Posição do bucket no arquivo
     * @return Objeto Bucket com os dados lidos
     * @throws Exception Se houver erro na leitura
     */
    // Explicado em docs/aux/hashExtensivel/lerBucket.md
    private Bucket lerBucket(long posicao) throws Exception {
        arquivo.seek(posicao);
        int profundidadeLocal = arquivo.readInt();
        int quantidade = arquivo.readInt();
        
        List<ParChaveEndereco> registros = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            int chave = arquivo.readInt();
            long endereco = arquivo.readLong();
            registros.add(new ParChaveEndereco(chave, endereco));
        }
        
        return new Bucket(posicao, profundidadeLocal, quantidade, registros);
    }
    
    /**
     * Escreve um bucket no arquivo.
     * 
     * @param bucket Bucket a ser escrito
     * @throws Exception Se houver erro na escrita
     */
    // Explicado em docs/aux/hashExtensivel/escreverBucket.md
    private void escreverBucket(Bucket bucket) throws Exception {
        arquivo.seek(bucket.getPosicao());
        arquivo.writeInt(bucket.getProfundidadeLocal());
        arquivo.writeInt(bucket.getQuantidade());
        
        for (ParChaveEndereco par : bucket.getRegistros()) {
            arquivo.writeInt(par.getChave());
            arquivo.writeLong(par.getEndereco());
        }
    }
    
    /**
     * Insere um registro no hash.
     * Primeiro escreve o registro no final do arquivo, depois insere no bucket.
     * 
     * @param chave Chave para indexação
     * @param registro Registro a ser armazenado
     * @throws Exception Se houver erro na inserção
     */
    // Explicado em docs/aux/hashExtensivel/inserir.md
    public void inserir(int chave, T registro) throws Exception {
        // Primeiro, escreve o registro no final do arquivo
        arquivo.seek(arquivo.length());
        long enderecoRegistro = arquivo.getFilePointer();
        byte[] dados = registro.toByteArray();
        arquivo.writeInt(dados.length);
        arquivo.write(dados);
        
        // Depois, insere no bucket apropriado
        int hash = hash(chave);
        int indice = hash % (1 << profundidadeGlobal);
        
        long posicaoBucket = lerPosicaoDiretorio(indice);
        Bucket bucket = lerBucket(posicaoBucket);
        
        if (bucket.getQuantidade() < tamanhoBucket) {
            bucket.inserir(chave, enderecoRegistro);
            escreverBucket(bucket);
        } else {
            dividirBucket(bucket, indice, chave, enderecoRegistro);
        }
    }
    
    /**
     * Busca um registro pela chave.
     * 
     * @param chave Chave do registro a ser buscado
     * @return Registro encontrado ou null
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/hashExtensivel/buscar.md
    public T buscar(int chave) throws Exception {
        int hash = hash(chave);
        int indice = hash % (1 << profundidadeGlobal);
        
        long posicaoBucket = lerPosicaoDiretorio(indice);
        Bucket bucket = lerBucket(posicaoBucket);
        
        long enderecoRegistro = bucket.buscar(chave);
        if (enderecoRegistro != -1) {
            return lerRegistro(enderecoRegistro);
        }
        
        return null;
    }
    
    /**
     * Lê um registro do arquivo a partir de seu endereço.
     * 
     * @param endereco Posição do registro no arquivo
     * @return Registro lido
     * @throws Exception Se houver erro na leitura
     */
    // Explicado em docs/aux/hashExtensivel/lerRegistro.md
    private T lerRegistro(long endereco) throws Exception {
        arquivo.seek(endereco);
        int tamanho = arquivo.readInt();
        byte[] dados = new byte[tamanho];
        arquivo.read(dados);
        
        T registro = construtor.newInstance();
        registro.fromByteArray(dados);
        return registro;
    }
    
    /**
     * Remove um registro do hash pela chave.
     * 
     * @param chave Chave do registro a ser removido
     * @return true se removido com sucesso
     * @throws Exception Se houver erro na remoção
     */
    // Explicado em docs/aux/hashExtensivel/remover.md
    public boolean remover(int chave) throws Exception {
        int hash = hash(chave);
        int indice = hash % (1 << profundidadeGlobal);
        
        long posicaoBucket = lerPosicaoDiretorio(indice);
        Bucket bucket = lerBucket(posicaoBucket);
        
        boolean removido = bucket.remover(chave);
        if (removido) {
            escreverBucket(bucket);
        }
        
        return removido;
    }
    
    /**
     * Divide um bucket que ficou cheio.
     * Cria dois novos buckets e redistribui os registros.
     * 
     * @param bucketAntigo Bucket que ficou cheio
     * @param indice Índice do bucket no diretório
     * @param novaChave Chave do novo registro
     * @param novoEndereco Endereço do novo registro
     * @throws Exception Se houver erro na divisão
     */
    // Explicado em docs/aux/hashExtensivel/dividirBucket.md
    private void dividirBucket(Bucket bucketAntigo, int indice, int novaChave, long novoEndereco) throws Exception {
        int novaProfundidadeLocal = bucketAntigo.getProfundidadeLocal() + 1;
        
        // Se necessário, expande o diretório
        if (novaProfundidadeLocal > profundidadeGlobal) {
            expandirDiretorio();
        }
        
        // Cria dois novos buckets
        long posicaoBucket0 = criarBucket();
        long posicaoBucket1 = criarBucket();
        
        Bucket bucket0 = new Bucket(posicaoBucket0, novaProfundidadeLocal, 0, new ArrayList<>());
        Bucket bucket1 = new Bucket(posicaoBucket1, novaProfundidadeLocal, 0, new ArrayList<>());
        
        // Redistribui registros do bucket antigo
        for (ParChaveEndereco par : bucketAntigo.getRegistros()) {
            int novoHash = hash(par.getChave());
            int novoIndice = novoHash % (1 << novaProfundidadeLocal);
            
            if ((novoIndice & (1 << (novaProfundidadeLocal - 1))) == 0) {
                bucket0.inserir(par.getChave(), par.getEndereco());
            } else {
                bucket1.inserir(par.getChave(), par.getEndereco());
            }
        }
        
        // Insere o novo registro
        int novoHash = hash(novaChave);
        int novoIndice = novoHash % (1 << novaProfundidadeLocal);
        if ((novoIndice & (1 << (novaProfundidadeLocal - 1))) == 0) {
            bucket0.inserir(novaChave, novoEndereco);
        } else {
            bucket1.inserir(novaChave, novoEndereco);
        }
        
        escreverBucket(bucket0);
        escreverBucket(bucket1);
        
        // Atualiza o diretório
        int fator = 1 << (novaProfundidadeLocal - 1);
        for (int i = 0; i < fator; i++) {
            int indiceAtual = indice + i;
            if (hash(indiceAtual) % (1 << novaProfundidadeLocal) < fator) {
                atualizarPosicaoDiretorio(indiceAtual, posicaoBucket0);
            } else {
                atualizarPosicaoDiretorio(indiceAtual, posicaoBucket1);
            }
        }
    }
    
    /**
     * Expande o diretório, dobrando seu tamanho.
     * 
     * @throws Exception Se houver erro na expansão
     */
    // Explicado em docs/aux/hashExtensivel/expandirDiretorio.md
    private void expandirDiretorio() throws Exception {
        profundidadeGlobal++;
        
        int novoTamanho = 1 << profundidadeGlobal;
        
        // Salva diretório antigo
        List<Long> diretorioAntigo = new ArrayList<>();
        for (int i = 0; i < (1 << (profundidadeGlobal - 1)); i++) {
            diretorioAntigo.add(lerPosicaoDiretorio(i));
        }
        
        // Atualiza cabeçalho
        arquivo.seek(0);
        arquivo.writeInt(profundidadeGlobal);
        arquivo.writeInt(tamanhoBucket);
        arquivo.writeInt(novoTamanho);
        
        // Expande diretório (cada entrada duplica a anterior)
        for (int i = 0; i < novoTamanho; i++) {
            int indiceAntigo = i % (1 << (profundidadeGlobal - 1));
            long posicao = diretorioAntigo.get(indiceAntigo);
            arquivo.writeLong(posicao);
        }
    }
    
    /**
     * Função hash simples (identidade).
     * 
     * @param chave Chave a ser hasheada
     * @return Valor hash
     */
    // Explicado em docs/aux/hashExtensivel/hash.md
    private int hash(int chave) {
        return chave;
    }
    
    /**
     * Lê a posição do bucket no diretório.
     * 
     * @param indice Índice no diretório
     * @return Posição do bucket
     * @throws Exception Se houver erro na leitura
     */
    // Explicado em docs/aux/hashExtensivel/lerPosicaoDiretorio.md
    private long lerPosicaoDiretorio(int indice) throws Exception {
        long posicao = TAM_CABECALHO + (indice * 8);
        arquivo.seek(posicao);
        return arquivo.readLong();
    }
    
    /**
     * Atualiza a posição do bucket no diretório.
     * 
     * @param indice Índice no diretório
     * @param posicao Nova posição do bucket
     * @throws Exception Se houver erro na escrita
     */
    // Explicado em docs/aux/hashExtensivel/atualizarPosicaoDiretorio.md
    private void atualizarPosicaoDiretorio(int indice, long posicao) throws Exception {
        long posicaoArquivo = TAM_CABECALHO + (indice * 8);
        arquivo.seek(posicaoArquivo);
        arquivo.writeLong(posicao);
    }
    
    /**
     * Fecha o arquivo de hash.
     * 
     * @throws Exception Se houver erro no fechamento
     */
    // Explicado em docs/aux/hashExtensivel/close.md
    public void close() throws Exception {
        if (arquivo != null) {
            arquivo.close();
        }
    }
    
    /**
     * Classe interna que representa um par chave-endereço no bucket.
     */
    // Explicado em docs/aux/hashExtensivel/ParChaveEndereco.md
    private class ParChaveEndereco {
        private final int chave;
        private final long endereco;
        
        public ParChaveEndereco(int chave, long endereco) {
            this.chave = chave;
            this.endereco = endereco;
        }
        
        public int getChave() { return chave; }
        public long getEndereco() { return endereco; }
    }
    
    /**
     * Classe interna que representa um bucket do hash extensível.
     * Cada bucket contém uma lista de pares chave-endereço.
     */
    // Explicado em docs/aux/hashExtensivel/Bucket.md
    private class Bucket {
        private final long posicao;
        private final int profundidadeLocal;
        private int quantidade;
        private final List<ParChaveEndereco> registros;
        
        public Bucket(long posicao, int profundidadeLocal, int quantidade, List<ParChaveEndereco> registros) {
            this.posicao = posicao;
            this.profundidadeLocal = profundidadeLocal;
            this.quantidade = quantidade;
            this.registros = registros;
        }
        
        public long getPosicao() { return posicao; }
        public int getProfundidadeLocal() { return profundidadeLocal; }
        public int getQuantidade() { return quantidade; }
        public List<ParChaveEndereco> getRegistros() { return registros; }
        
        public void inserir(int chave, long endereco) {
            registros.add(new ParChaveEndereco(chave, endereco));
            quantidade++;
        }
        
        public long buscar(int chave) {
            for (ParChaveEndereco par : registros) {
                if (par.getChave() == chave) {
                    return par.getEndereco();
                }
            }
            return -1;
        }
        
        public boolean remover(int chave) {
            for (int i = 0; i < registros.size(); i++) {
                if (registros.get(i).getChave() == chave) {
                    registros.remove(i);
                    quantidade--;
                    return true;
                }
            }
            return false;
        }
    }
}