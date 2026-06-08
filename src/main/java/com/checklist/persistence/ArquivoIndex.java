package com.checklist.persistence;
import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe para gerenciamento de arquivos com índice B+ Tree.
 * Mantém sincronizados um arquivo de dados e um arquivo de índice.
 * 
 * @param <T> Tipo de registro que implementa a interface Registro
 */
public class ArquivoIndex<T extends Registro> {
    // Explicado em docs/aux/arquivoIndex/arquivoIndex.md
    private RandomAccessFile arquivoDados;
    private RandomAccessFile arquivoIndice;
    private final Constructor<T> construtor;
    
    private static final int TAM_CABECALHO_DADOS = 12;
    
    private int ordemArvore = 5;
    private long raizIndice;

    /**
     * Construtor padrão com ordem 5.
     * 
     * @param nomeArquivo Nome base do arquivo
     * @param construtor Construtor da classe T
     * @throws Exception Se houver erro na criação dos arquivos
     */
    // Explicado em docs/aux/arquivoIndex/construtor.md
    public ArquivoIndex(String nomeArquivo, Constructor<T> construtor) throws Exception {
        this(nomeArquivo, construtor, 5);
    }

    /**
     * Construtor com ordem personalizada.
     * Cria/abre os arquivos de dados e índice.
     * 
     * @param nomeArquivo Nome base do arquivo
     * @param construtor Construtor da classe T
     * @param ordem Ordem da árvore B+
     * @throws Exception Se houver erro na criação dos arquivos
     */
    // Explicado em docs/aux/arquivoIndex/construtor.md
    public ArquivoIndex(String nomeArquivo, Constructor<T> construtor, int ordem) throws Exception {
        this.construtor = construtor;
        this.ordemArvore = ordem;
        
        File diretorio = new File("./dados");
        if (!diretorio.exists()) {
            diretorio.mkdir();
        }
        
        this.arquivoDados = new RandomAccessFile("./dados/" + nomeArquivo + ".db", "rw");
        if (arquivoDados.length() == 0) {
            arquivoDados.writeInt(0);    // Último ID usado
            arquivoDados.writeLong(-1);  // Ponteiro para primeiro deletado
        }
        
        this.arquivoIndice = new RandomAccessFile("./dados/" + nomeArquivo + "_idx.db", "rw");
        if (arquivoIndice.length() == 0) {
            arquivoIndice.writeInt(ordemArvore);   // Ordem da árvore
            arquivoIndice.writeLong(-1);           // Posição da raiz
            raizIndice = -1;
        } else {
            arquivoIndice.seek(0);
            ordemArvore = arquivoIndice.readInt();
            raizIndice = arquivoIndice.readLong();
        }
    }
    
    /**
     * Insere uma chave e seu endereço no índice B+ Tree.
     * 
     * @param chave Chave a ser inserida (geralmente o ID)
     * @param endereco Posição do registro no arquivo de dados
     * @throws Exception Se houver erro na inserção
     */
    // Explicado em docs/aux/arquivoIndex/inserirIndice.md
    public void inserirIndice(int chave, long endereco) throws Exception {
        if (raizIndice == -1) {
            IndexNode novaRaiz = new IndexNode(ordemArvore, true);
            novaRaiz.inserirChave(chave, endereco);
            raizIndice = escreverNo(novaRaiz);
            atualizarCabecalhoIndice();
        } else {
            IndexNode raiz = lerNo(raizIndice);
            if (raiz.estaCheia()) {
                IndexNode novaRaiz = new IndexNode(ordemArvore, false);
                novaRaiz.setFilho(0, raizIndice);
                
                long posNovoFilho = arquivoIndice.length();
                IndexNode novoFilhoNo = novaRaiz.dividirFilho(0, raiz, posNovoFilho);
                
                escreverNo(raiz, raizIndice);
                escreverNo(novoFilhoNo, posNovoFilho);
                raizIndice = escreverNo(novaRaiz);
                atualizarCabecalhoIndice();
                
                inserirEmNo(novaRaiz, raizIndice, chave, endereco);
            } else {
                inserirEmNo(raiz, raizIndice, chave, endereco);
            }
        }
    }
    
    /**
     * Insere uma chave em um nó específico (recursivo).
     * 
     * @param no Nó atual da árvore
     * @param posNo Endereço físico do nó atual
     * @param chave Chave a ser inserida
     * @param endereco Endereço associado à chave
     * @throws Exception Se houver erro na inserção
     */
    private void inserirEmNo(IndexNode no, long posNo, int chave, long endereco) throws Exception {
        if (no.isFolha()) {
            no.inserirChave(chave, endereco);
            escreverNo(no, posNo);
        } else {
            int pos = no.encontrarPosicaoChave(chave);
            // Ajuste para B+ Tree: se a chave é igual ao separador, deve ir para o filho da direita
            if (pos < no.getNumChaves() && chave == no.getChave(pos)) {
                pos++;
            }
            
            long filhoPos = no.getFilho(pos);
            IndexNode filho = lerNo(filhoPos);
            
            if (filho.estaCheia()) {
                long posNovoFilho = arquivoIndice.length();
                IndexNode novoFilhoNo = no.dividirFilho(pos, filho, posNovoFilho);
                
                escreverNo(filho, filhoPos);
                escreverNo(novoFilhoNo, posNovoFilho);
                escreverNo(no, posNo);
                
                if (chave >= no.getChave(pos)) {
                    pos++;
                }
                filhoPos = no.getFilho(pos);
                filho = lerNo(filhoPos);
            }
            inserirEmNo(filho, filhoPos, chave, endereco);
        }
    }

    /**
     * Lista todos os registros em ordem crescente de ID utilizando a Árvore B+.
     * 
     * @return Lista de registros ordenados
     * @throws Exception Se houver erro na leitura
     */
    public List<T> listInOrder() throws Exception {
        List<T> lista = new ArrayList<>();
        if (raizIndice == -1) return lista;

        try {
            // 1. Encontrar a folha mais à esquerda
            long posAtual = raizIndice;
            IndexNode no = lerNo(posAtual);
            int seguranca = 0;
            while (!no.isFolha() && seguranca < 50) {
                posAtual = no.getFilho(0);
                no = lerNo(posAtual);
                seguranca++;
            }
            if (seguranca >= 50) throw new Exception("CORRUPTED_INDEX");

            // 2. Percorrer o encadeamento de folhas
            seguranca = 0;
            while (posAtual != -1 && seguranca < 10000) {
                for (int i = 0; i < no.getNumChaves(); i++) {
                    long enderecoDado = no.getEndereco(i);
                    T registro = readByAddress(enderecoDado);
                    if (registro != null) {
                        lista.add(registro);
                    }
                }
                posAtual = no.getFilho(ordemArvore);
                if (posAtual != -1) no = lerNo(posAtual);
                seguranca++;
            }
        } catch (Exception e) {
            if ("CORRUPTED_INDEX".equals(e.getMessage())) {
                rebuildIndex();
                return listAll(); // Fallback para listAll se falhar ordem
            }
            throw e;
        }

        return lista;
    }

    private T readByAddress(long endereco) throws Exception {
        if (endereco == -1) return null;
        arquivoDados.seek(endereco);
        byte lapide = arquivoDados.readByte();
        if (lapide != ' ') return null;
        short tamanho = arquivoDados.readShort();
        if (tamanho <= 0 || endereco + 3 + tamanho > arquivoDados.length()) return null;
        byte[] dados = new byte[tamanho];
        arquivoDados.read(dados);
        T obj = construtor.newInstance();
        obj.fromByteArray(dados);
        return obj;
    }

    private void escreverNo(IndexNode no, long posicao) throws Exception {
        arquivoIndice.seek(posicao);
        arquivoIndice.write(no.toByteArray());
    }
    
    /**
     * Busca uma chave no índice e retorna seu endereço.
     * 
     * @param chave Chave a ser buscada
     * @return Endereço do registro ou -1 se não encontrado
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/arquivoIndex/buscarIndice.md
    public long buscarIndice(int chave) throws Exception {
        if (raizIndice == -1) {
            return -1;
        }
        IndexNode no = lerNo(raizIndice);
        return buscarEmNo(no, chave);
    }
    
    /**
     * Busca recursivamente uma chave em um nó da árvore.
     * 
     * @param no Nó atual da árvore
     * @param chave Chave a ser buscada
     * @return Endereço do registro ou -1 se não encontrado
     * @throws Exception Se houver erro na busca
     */
    // Explicado em docs/aux/arquivoIndex/buscarEmNo.md
    private long buscarEmNo(IndexNode no, int chave) throws Exception {
        return buscarEmNo(no, chave, 0);
    }

    private long buscarEmNo(IndexNode no, int chave, int altura) throws Exception {
        if (altura > 50) { // Árvore B+ de ordem 5 com 50 de altura é impossível para dados normais
            throw new Exception("CORRUPTED_INDEX");
        }
        
        int pos = no.encontrarPosicaoChave(chave);
        
        if (no.isFolha()) {
            if (pos < no.getNumChaves() && no.getChave(pos) == chave) {
                return no.getEndereco(pos);
            }
            return -1;
        } else {
            // No nó interno, se chave == chaves[pos], precisamos ir para o filho da DIREITA (pos + 1)
            // porque no B+ Tree o separador é o menor valor do filho da direita.
            if (pos < no.getNumChaves() && chave == no.getChave(pos)) {
                pos++;
            }
            long filhoPos = no.getFilho(pos);
            if (filhoPos == -1) return -1;
            IndexNode filho = lerNo(filhoPos);
            return buscarEmNo(filho, chave, altura + 1);
        }
    }
    
    /**
     * Remove uma chave do índice.
     * 
     * @param chave Chave a ser removida
     * @return true se removida com sucesso, false caso contrário
     * @throws Exception Se houver erro na remoção
     */
    // Explicado em docs/aux/arquivoIndex/removerIndice.md
    public boolean removerIndice(int chave) throws Exception {
        if (raizIndice == -1) {
            return false;
        }
        
        IndexNode raiz = lerNo(raizIndice);
        boolean removido = removerEmNo(raiz, raizIndice, chave);
        
        if (!raiz.isFolha() && raiz.getNumChaves() == 0) {
            raizIndice = raiz.getFilho(0);
            atualizarCabecalhoIndice();
        }
        
        return removido;
    }
    
    /**
     * Remove recursivamente uma chave de um nó.
     * 
     * @param no Nó atual da árvore
     * @param chave Chave a ser removida
     * @return true se removida com sucesso
     * @throws Exception Se houver erro na remoção
     */
    // Explicado em docs/aux/arquivoIndex/removerEmNo.md
    private boolean removerEmNo(IndexNode no, long posNo, int chave) throws Exception {
        int pos = no.encontrarPosicaoChave(chave);
        
        if (no.isFolha()) {
            if (pos < no.getNumChaves() && no.getChave(pos) == chave) {
                no.removerChave(pos);
                escreverNo(no, posNo);
                return true;
            }
            return false;
        } else {
            if (pos < no.getNumChaves() && chave == no.getChave(pos)) {
                pos++;
            }
            long filhoPos = no.getFilho(pos);
            IndexNode filho = lerNo(filhoPos);
            boolean removido = removerEmNo(filho, filhoPos, chave);
            
            if (filho.getNumChaves() < ordemArvore / 2) {
                rebalancear(no, posNo, pos, filho, filhoPos);
            }
            
            return removido;
        }
    }
    
    /**
     * Rebalanceia a árvore após remoção, redistribuindo chaves entre irmãos.
     * 
     * @param pai Nó pai
     * @param posPai Endereço do pai
     * @param posFilhoNoPai Posição do filho no pai
     * @param filho Nó filho que ficou desbalanceado
     * @param posFilho Endereço do filho
     * @throws Exception Se houver erro no rebalanceamento
     */
    // Explicado em docs/aux/arquivoIndex/rebalancear.md
    private void rebalancear(IndexNode pai, long posPai, int posFilhoNoPai, IndexNode filho, long posFilho) throws Exception {
        if (posFilhoNoPai > 0) {
            long posIrmaoEsq = pai.getFilho(posFilhoNoPai - 1);
            IndexNode irmaoEsq = lerNo(posIrmaoEsq);
            if (irmaoEsq.getNumChaves() > ordemArvore / 2) {
                filho.rotacionarEsquerda(irmaoEsq, pai.getChave(posFilhoNoPai - 1));
                pai.setChave(posFilhoNoPai - 1, filho.getChave(0));
                escreverNo(irmaoEsq, posIrmaoEsq);
                escreverNo(filho, posFilho);
                escreverNo(pai, posPai);
                return;
            }
        }
        
        if (posFilhoNoPai < pai.getNumChaves()) {
            long posIrmaoDir = pai.getFilho(posFilhoNoPai + 1);
            IndexNode irmaoDir = lerNo(posIrmaoDir);
            if (irmaoDir.getNumChaves() > ordemArvore / 2) {
                filho.rotacionarDireita(irmaoDir, pai.getChave(posFilhoNoPai));
                pai.setChave(posFilhoNoPai, irmaoDir.getChave(0));
                escreverNo(irmaoDir, posIrmaoDir);
                escreverNo(filho, posFilho);
                escreverNo(pai, posPai);
                return;
            }
        }
        
        if (posFilhoNoPai > 0) {
            long posIrmaoEsq = pai.getFilho(posFilhoNoPai - 1);
            IndexNode irmaoEsq = lerNo(posIrmaoEsq);
            irmaoEsq.mesclar(pai.getChave(posFilhoNoPai - 1), filho);
            pai.removerChave(posFilhoNoPai - 1);
            pai.removerFilho(posFilhoNoPai);
            escreverNo(irmaoEsq, posIrmaoEsq);
            escreverNo(pai, posPai);
        } else {
            long posIrmaoDir = pai.getFilho(posFilhoNoPai + 1);
            IndexNode irmaoDir = lerNo(posIrmaoDir);
            filho.mesclar(pai.getChave(posFilhoNoPai), irmaoDir);
            pai.removerChave(posFilhoNoPai);
            pai.removerFilho(posFilhoNoPai + 1);
            escreverNo(filho, posFilho);
            escreverNo(pai, posPai);
        }
    }
    
    /**
     * Cria um novo registro no arquivo de dados e insere no índice.
     * 
     * @param obj Objeto a ser persistido
     * @return ID gerado para o novo registro
     * @throws Exception Se houver erro na criação
     */
    // Explicado em docs/aux/arquivoIndex/create.md
    public int create(T obj) throws Exception {
        int id = obj.getId();
        if (id <= 0) {
            arquivoDados.seek(0);
            id = arquivoDados.readInt() + 1;
            arquivoDados.seek(0);
            arquivoDados.writeInt(id);
            obj.setId(id);
        } else {
            // Se o ID foi definido manualmente, garantir que o cabeçalho esteja atualizado
            arquivoDados.seek(0);
            int ultimoID = arquivoDados.readInt();
            if (id > ultimoID) {
                arquivoDados.seek(0);
                arquivoDados.writeInt(id);
            }
        }
        
        byte[] dados = obj.toByteArray();
        long endereco = getDeleted(dados.length);
        
        if (endereco == -1) {
            arquivoDados.seek(arquivoDados.length());
            endereco = arquivoDados.getFilePointer();
            arquivoDados.writeByte(' ');
            arquivoDados.writeShort(dados.length);
            arquivoDados.write(dados);
        } else {
            arquivoDados.seek(endereco);
            arquivoDados.writeByte(' ');
            arquivoDados.skipBytes(2);
            arquivoDados.write(dados);
        }
        
        inserirIndice(id, endereco);
        
        return id;
    }
    
    /**
     * Busca um registro pelo ID.
     * Primeiro tenta pelo índice, se falhar faz busca sequencial e corrige o índice.
     * 
     * @param id Identificador do registro
     * @return Objeto encontrado ou null
     * @throws Exception Se houver erro na leitura
     */
    // Explicado em docs/aux/arquivoIndex/read.md
    public T read(int id) throws Exception {
        long endereco = -1;
        try {
            endereco = buscarIndice(id);
        } catch (Exception e) {
            if ("CORRUPTED_INDEX".equals(e.getMessage())) {
                rebuildIndex();
                endereco = buscarIndice(id);
            } else {
                throw e;
            }
        }
        
        if (endereco == -1) {
            // Busca sequencial como fallback
            arquivoDados.seek(TAM_CABECALHO_DADOS);
            
            while (arquivoDados.getFilePointer() < arquivoDados.length()) {
                long posAtual = arquivoDados.getFilePointer();
                byte lapide = arquivoDados.readByte();
                short tamanho = arquivoDados.readShort();
                
                if (tamanho <= 0 || arquivoDados.getFilePointer() + tamanho > arquivoDados.length()) {
                    break;
                }
                
                byte[] dados = new byte[tamanho];
                arquivoDados.read(dados);
                
                if (lapide == ' ') {
                    T obj = construtor.newInstance();
                    obj.fromByteArray(dados);
                    if (obj.getId() == id) {
                        // Corrigir o índice com o endereço correto
                        inserirIndice(id, posAtual);
                        return obj;
                    }
                }
            }
            return null;
        }
        
        arquivoDados.seek(endereco);
        byte lapide = arquivoDados.readByte();
        if (lapide != ' ') {
            return null;
        }
        
        short tamanho = arquivoDados.readShort();
        if (tamanho <= 0 || endereco + 3 + tamanho > arquivoDados.length()) return null;
        byte[] dados = new byte[tamanho];
        arquivoDados.read(dados);
        
        T obj = construtor.newInstance();
        obj.fromByteArray(dados);
        
        return obj;
    }
    
    /**
     * Remove um registro do arquivo de dados e do índice.
     * 
     * @param id Identificador do registro
     * @return true se removido com sucesso
     * @throws Exception Se houver erro na remoção
     */
    // Explicado em docs/aux/arquivoIndex/delete.md
    public boolean delete(int id) throws Exception {
        long endereco = buscarIndice(id);
        
        if (endereco == -1) {
            // Busca sequencial como fallback
            arquivoDados.seek(TAM_CABECALHO_DADOS);
            
            while (arquivoDados.getFilePointer() < arquivoDados.length()) {
                long posAtual = arquivoDados.getFilePointer();
                byte lapide = arquivoDados.readByte();
                short tamanho = arquivoDados.readShort();
                
                if (tamanho <= 0) {
                    break;
                }
                
                byte[] dados = new byte[tamanho];
                arquivoDados.read(dados);
                
                if (lapide == ' ') {
                    T obj = construtor.newInstance();
                    obj.fromByteArray(dados);
                    if (obj.getId() == id) {
                        endereco = posAtual;
                        break;
                    }
                }
            }
        }
        
        if (endereco == -1) {
            return false;
        }
        
        arquivoDados.seek(endereco);
        byte lapide = arquivoDados.readByte();
        if (lapide != ' ') {
            return false;
        }
        
        short tamanho = arquivoDados.readShort();
        
        arquivoDados.seek(endereco);
        arquivoDados.writeByte('*');
        
        addDeleted(tamanho, endereco);
        
        removerIndice(id);
        
        return true;
    }
    
    /**
     * Atualiza um registro existente.
     * Se couber no mesmo espaço, atualiza in-place.
     * Caso contrário, move para novo local e atualiza o índice.
     * 
     * @param novoObj Objeto com os dados atualizados
     * @return true se atualizado com sucesso
     * @throws Exception Se houver erro na atualização
     */
    // Explicado em docs/aux/arquivoIndex/update.md
    public boolean update(T novoObj) throws Exception {
        int id = novoObj.getId();
        
        long enderecoAntigo = buscarIndice(id);
        
        if (enderecoAntigo == -1) {
            // Busca sequencial como fallback
            arquivoDados.seek(TAM_CABECALHO_DADOS);
            
            while (arquivoDados.getFilePointer() < arquivoDados.length()) {
                long posAtual = arquivoDados.getFilePointer();
                byte lapide = arquivoDados.readByte();
                short tamanho = arquivoDados.readShort();
                
                if (tamanho <= 0) break;
                
                byte[] dados = new byte[tamanho];
                arquivoDados.read(dados);
                
                if (lapide == ' ') {
                    T obj = construtor.newInstance();
                    obj.fromByteArray(dados);
                    if (obj.getId() == id) {
                        enderecoAntigo = posAtual;
                        break;
                    }
                }
            }
        }
        
        if (enderecoAntigo == -1) {
            return false;
        }
        
        byte[] novosDados = novoObj.toByteArray();
        short novoTamanho = (short) novosDados.length;
        
        arquivoDados.seek(enderecoAntigo);
        byte lapide = arquivoDados.readByte();
        if (lapide != ' ') {
            return false;
        }
        
        short tamanhoAntigo = arquivoDados.readShort();
        
        if (novoTamanho <= tamanhoAntigo) {
            // Atualiza no mesmo lugar
            arquivoDados.seek(enderecoAntigo + 3);
            arquivoDados.write(novosDados);
        } else {
            // Marca o antigo como deletado
            arquivoDados.seek(enderecoAntigo);
            arquivoDados.writeByte('*');
            addDeleted(tamanhoAntigo, enderecoAntigo);
            
            // Remove do índice antigo
            removerIndice(id);
            
            // Cria novo registro
            long novoEndereco = getDeleted(novosDados.length);
            if (novoEndereco == -1) {
                arquivoDados.seek(arquivoDados.length());
                novoEndereco = arquivoDados.getFilePointer();
                arquivoDados.writeByte(' ');
                arquivoDados.writeShort(novoTamanho);
                arquivoDados.write(novosDados);
            } else {
                arquivoDados.seek(novoEndereco);
                arquivoDados.writeByte(' ');
                arquivoDados.skipBytes(2);
                arquivoDados.write(novosDados);
            }
            
            // Insere no índice com o novo endereço
            inserirIndice(id, novoEndereco);
        }
        
        return true;
    }
    
    /**
     * Adiciona um espaço vazio à lista encadeada de registros deletados.
     * 
     * @param tamanhoEspaco Tamanho do espaço disponível
     * @param enderecoEspaco Posição inicial do espaço vazio
     * @throws Exception Se houver erro na escrita
     */
    // Explicado em docs/aux/arquivoIndex/addDeleted.md
    private void addDeleted(int tamanhoEspaco, long enderecoEspaco) throws Exception {
        long cabecalhoLista = 4;
        arquivoDados.seek(cabecalhoLista);
        long primeiroDeletado = arquivoDados.readLong();
        
        arquivoDados.seek(enderecoEspaco + 1);
        arquivoDados.writeShort(tamanhoEspaco);
        arquivoDados.writeLong(primeiroDeletado);
        
        arquivoDados.seek(cabecalhoLista);
        arquivoDados.writeLong(enderecoEspaco);
    }
    
    /**
     * Busca um espaço vazio na lista de deletados.
     * 
     * @param tamanhoNecessario Tamanho necessário para o novo registro
     * @return Endereço do espaço encontrado ou -1
     * @throws Exception Se houver erro na leitura
     */
    // Explicado em docs/aux/arquivoIndex/getDeleted.md
    private long getDeleted(int tamanhoNecessario) throws Exception {
        long cabecalhoLista = 4;
        arquivoDados.seek(cabecalhoLista);
        long enderecoAtual = arquivoDados.readLong();
        long enderecoAnterior = -1;
        
        while (enderecoAtual != -1) {
            arquivoDados.seek(enderecoAtual + 1);
            int tamanho = arquivoDados.readShort();
            long proximo = arquivoDados.readLong();
            
            if (tamanho >= tamanhoNecessario) {
                if (enderecoAnterior == -1) {
                    arquivoDados.seek(cabecalhoLista);
                    arquivoDados.writeLong(proximo);
                } else {
                    arquivoDados.seek(enderecoAnterior + 1 + 2);
                    arquivoDados.writeLong(proximo);
                }
                return enderecoAtual;
            }
            
            enderecoAnterior = enderecoAtual;
            enderecoAtual = proximo;
        }
        
        return -1;
    }
    
    /**
     * Escreve um nó da árvore no arquivo de índice.
     * 
     * @param no Nó a ser escrito
     * @return Posição onde o nó foi escrito
     * @throws Exception Se houver erro na escrita
     */
    // Explicado em docs/aux/arquivoIndex/escreverNo.md
    private long escreverNo(IndexNode no) throws Exception {
        long posicao = arquivoIndice.length();
        arquivoIndice.seek(posicao);
        
        byte[] dados = no.toByteArray();
        arquivoIndice.write(dados);
        
        return posicao;
    }
    
    /**
     * Lê um nó da árvore do arquivo de índice.
     * 
     * @param posicao Posição do nó no arquivo
     * @return Nó lido
     * @throws Exception Se houver erro na leitura
     */
    // Explicado em docs/aux/arquivoIndex/lerNo.md
    private IndexNode lerNo(long posicao) throws Exception {
        arquivoIndice.seek(posicao);
        byte[] dados = new byte[IndexNode.getTamanhoMaximo(ordemArvore)];
        arquivoIndice.read(dados);
        
        IndexNode no = new IndexNode(ordemArvore, false);
        no.fromByteArray(dados);
        
        return no;
    }
    
    /**
     * Reconstrói o arquivo de índice do zero a partir do arquivo de dados.
     * 
     * @throws Exception Se houver erro na reconstrução
     */
    public void rebuildIndex() throws Exception {
        // Resetar arquivo de índice
        arquivoIndice.setLength(0);
        arquivoIndice.writeInt(ordemArvore);
        arquivoIndice.writeLong(-1);
        raizIndice = -1;

        // Ler todos os registros do arquivo de dados e reinserir no índice
        arquivoDados.seek(TAM_CABECALHO_DADOS);
        while (arquivoDados.getFilePointer() < arquivoDados.length()) {
            long posAtual = arquivoDados.getFilePointer();
            byte lapide = arquivoDados.readByte();
            short tamanho = arquivoDados.readShort();
            
            if (tamanho <= 0 || arquivoDados.getFilePointer() + tamanho > arquivoDados.length()) {
                break;
            }
            
            byte[] dados = new byte[tamanho];
            arquivoDados.read(dados);
            
            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                inserirIndice(obj.getId(), posAtual);
            }
        }
    }
    
    /**
     * Atualiza o cabeçalho do arquivo de índice.
     * 
     * @throws Exception Se houver erro na escrita
     */
    // Explicado em docs/aux/arquivoIndex/atualizarCabecalhoIndice.md
    private void atualizarCabecalhoIndice() throws Exception {
        arquivoIndice.seek(0);
        arquivoIndice.writeInt(ordemArvore);
        arquivoIndice.writeLong(raizIndice);
    }
    
    /**
     * Lista todos os registros ativos do arquivo de dados.
     * 
     * @return Lista com todos os registros
     * @throws Exception Se houver erro na leitura
     */
    // Explicado em docs/aux/arquivoIndex/listAll.md
    public List<T> listAll() throws Exception {
        List<T> lista = new ArrayList<>();
        arquivoDados.seek(TAM_CABECALHO_DADOS);
        
        while (arquivoDados.getFilePointer() < arquivoDados.length()) {
            long posAtual = arquivoDados.getFilePointer();
            byte lapide = arquivoDados.readByte();
            short tamanho = arquivoDados.readShort();
            
            if (tamanho <= 0 || arquivoDados.getFilePointer() + tamanho > arquivoDados.length()) {
                break;
            }
            
            byte[] dados = new byte[tamanho];
            arquivoDados.read(dados);
            
            if (lapide == ' ') {
                try {
                    T obj = construtor.newInstance();
                    obj.fromByteArray(dados);
                    lista.add(obj);
                } catch (Exception e) {
                    // Ignora registros corrompidos no listAll
                }
            }
        }
        
        return lista;
    }
    
    /**
     * Fecha os arquivos de dados e índice.
     * 
     * @throws Exception Se houver erro no fechamento
     */
    // Explicado em docs/aux/arquivoIndex/close.md
    public void close() throws Exception {
        if (arquivoDados != null) arquivoDados.close();
        if (arquivoIndice != null) arquivoIndice.close();
    }
}