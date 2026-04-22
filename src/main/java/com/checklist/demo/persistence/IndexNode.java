package com.checklist.demo.persistence;

import java.io.*;

/**
 * Classe que representa um nó da árvore B+ utilizada no índice.
 * 
 * A árvore B+ possui duas características principais:
 * - Nós internos: armazenam chaves e ponteiros para filhos
 * - Nós folha: armazenam chaves e endereços dos registros
 * 
 * Os nós folha também mantêm uma lista encadeada para navegação sequencial.
 */
public class IndexNode {
    // Explicado em docs/aux/indexNode/indexNode.md
    private int ordem;           // Ordem da árvore B+ (número máximo de chaves)
    private int numChaves;       // Número atual de chaves no nó
    private int[] chaves;        // Array de chaves
    private long[] enderecos;    // Endereços dos registros (para nós folha)
    private long[] filhos;       // Ponteiros para filhos (para nós internos)
    private boolean folha;       // Indica se é nó folha
    
    /**
     * Construtor do nó.
     * 
     * @param ordem Ordem da árvore B+
     * @param folha true se for nó folha, false se for nó interno
     */
    // Explicado em docs/aux/indexNode/construtor.md
    public IndexNode(int ordem, boolean folha) {
        this.ordem = ordem;
        this.folha = folha;
        this.numChaves = 0;
        this.chaves = new int[ordem];
        this.enderecos = new long[ordem];
        this.filhos = new long[ordem + 1];
        
        for (int i = 0; i < ordem; i++) {
            chaves[i] = -1;
            enderecos[i] = -1;
            filhos[i] = -1;
        }
        filhos[ordem] = -1;
    }
    
    /**
     * Verifica se o nó é folha.
     * 
     * @return true se for nó folha
     */
    // Explicado em docs/aux/indexNode/isFolha.md
    public boolean isFolha() {
        return folha;
    }
    
    /**
     * Retorna o número de chaves no nó.
     * 
     * @return Número de chaves
     */
    // Explicado em docs/aux/indexNode/getNumChaves.md
    public int getNumChaves() {
        return numChaves;
    }
    
    /**
     * Retorna a chave em uma determinada posição.
     * 
     * @param pos Índice da chave
     * @return Chave na posição especificada
     */
    // Explicado em docs/aux/indexNode/getChave.md
    public int getChave(int pos) {
        return chaves[pos];
    }
    
    /**
     * Retorna o endereço em uma determinada posição.
     * 
     * @param pos Índice do endereço
     * @return Endereço na posição especificada
     */
    // Explicado em docs/aux/indexNode/getEndereco.md
    public long getEndereco(int pos) {
        return enderecos[pos];
    }
    
    /**
     * Retorna o ponteiro para o filho em uma determinada posição.
     * 
     * @param pos Índice do filho
     * @return Ponteiro para o filho
     */
    // Explicado em docs/aux/indexNode/getFilho.md
    public long getFilho(int pos) {
        return filhos[pos];
    }
    
    /**
     * Define o ponteiro para o filho em uma determinada posição.
     * 
     * @param pos Índice onde será armazenado o filho
     * @param filho Ponteiro para o nó filho
     */
    // Explicado em docs/aux/indexNode/setFilho.md
    public void setFilho(int pos, long filho) {
        filhos[pos] = filho;
    }
    
    /**
     * Define a chave em uma determinada posição.
     * 
     * @param pos Índice onde será armazenada a chave
     * @param chave Valor da chave
     */
    // Explicado em docs/aux/indexNode/setChave.md
    public void setChave(int pos, int chave) {
        chaves[pos] = chave;
    }
    
    /**
     * Verifica se o nó está cheio.
     * 
     * @return true se o número de chaves é igual à ordem
     */
    // Explicado em docs/aux/indexNode/estaCheia.md
    public boolean estaCheia() {
        return numChaves == ordem;
    }
    
    /**
     * Encontra a posição onde uma chave deveria estar no nó.
     * 
     * @param chave Chave a ser localizada
     * @return Posição onde a chave está ou deveria estar
     */
    // Explicado em docs/aux/indexNode/encontrarPosicaoChave.md
    public int encontrarPosicaoChave(int chave) {
        int pos = 0;
        while (pos < numChaves && chave > chaves[pos]) {
            pos++;
        }
        return pos;
    }
    
    /**
     * Insere uma chave no nó mantendo a ordem.
     * 
     * @param chave Chave a ser inserida
     * @param endereco Endereço associado à chave (para nós folha)
     */
    // Explicado em docs/aux/indexNode/inserirChave.md
    public void inserirChave(int chave, long endereco) {
        int pos = encontrarPosicaoChave(chave);
        
        // Desloca elementos para a direita
        for (int i = numChaves; i > pos; i--) {
            chaves[i] = chaves[i-1];
            enderecos[i] = enderecos[i-1];
            if (!folha) {
                filhos[i+1] = filhos[i];
            }
        }
        
        chaves[pos] = chave;
        enderecos[pos] = endereco;
        numChaves++;
    }
    
    /**
     * Remove uma chave do nó.
     * 
     * @param pos Posição da chave a ser removida
     */
    // Explicado em docs/aux/indexNode/removerChave.md
    public void removerChave(int pos) {
        for (int i = pos; i < numChaves - 1; i++) {
            chaves[i] = chaves[i+1];
            enderecos[i] = enderecos[i+1];
            if (!folha) {
                filhos[i+1] = filhos[i+2];
            }
        }
        numChaves--;
        chaves[numChaves] = -1;
        enderecos[numChaves] = -1;
    }
    
    /**
     * Remove um ponteiro para filho do nó.
     * 
     * @param pos Posição do filho a ser removido
     */
    // Explicado em docs/aux/indexNode/removerFilho.md
    public void removerFilho(int pos) {
        for (int i = pos; i < numChaves + 1; i++) {
            filhos[i] = filhos[i+1];
        }
    }
    
    /**
     * Divide um nó filho cheio em dois nós.
     * 
     * @param pos Posição do filho no pai
     * @param filho Nó filho que será dividido
     * @throws Exception Se houver erro na divisão
     */
    // Explicado em docs/aux/indexNode/dividirFilho.md
    public void dividirFilho(int pos, IndexNode filho) throws Exception {
        IndexNode novoFilho = new IndexNode(ordem, filho.isFolha());
        
        int meio = ordem / 2;
        
        // Copia a segunda metade das chaves para o novo nó
        for (int i = meio; i < ordem; i++) {
            novoFilho.chaves[i - meio] = filho.chaves[i];
            novoFilho.enderecos[i - meio] = filho.enderecos[i];
            filho.chaves[i] = -1;
            filho.enderecos[i] = -1;
            novoFilho.numChaves++;
            filho.numChaves--;
        }
        
        if (!filho.isFolha()) {
            for (int i = meio; i <= ordem; i++) {
                novoFilho.filhos[i - meio] = filho.filhos[i];
                filho.filhos[i] = -1;
            }
        }
        
        int chaveMeio = filho.chaves[meio - 1];
        
        // Insere a chave do meio no nó pai
        for (int i = numChaves; i > pos; i--) {
            chaves[i] = chaves[i-1];
            enderecos[i] = enderecos[i-1];
            filhos[i+1] = filhos[i];
        }
        
        chaves[pos] = chaveMeio;
        enderecos[pos] = -1;
        filhos[pos+1] = -1;
        numChaves++;
    }
    
    /**
     * Mescla dois nós irmãos.
     * 
     * @param chavePai Chave do pai que está entre os dois nós
     * @param irmao Nó irmão a ser mesclado
     */
    // Explicado em docs/aux/indexNode/mesclar.md
    public void mesclar(int chavePai, IndexNode irmao) {
        if (folha) {
            for (int i = 0; i < irmao.numChaves; i++) {
                inserirChave(irmao.chaves[i], irmao.enderecos[i]);
            }
        } else {
            inserirChave(chavePai, -1);
            for (int i = 0; i < irmao.numChaves; i++) {
                inserirChave(irmao.chaves[i], -1);
                setFilho(numChaves, irmao.getFilho(i));
            }
            setFilho(numChaves, irmao.getFilho(irmao.numChaves));
        }
    }
    
    /**
     * Rotaciona chaves da esquerda para a direita.
     * 
     * @param irmao Nó irmão à esquerda
     * @param chavePai Chave do pai que está entre os nós
     */
    // Explicado em docs/aux/indexNode/rotacionarEsquerda.md
    public void rotacionarEsquerda(IndexNode irmao, int chavePai) {
        if (folha) {
            inserirChave(irmao.chaves[irmao.numChaves - 1], 
                        irmao.enderecos[irmao.numChaves - 1]);
            irmao.removerChave(irmao.numChaves - 1);
        } else {
            inserirChave(chavePai, -1);
            for (int i = numChaves; i > 0; i--) {
                setFilho(i + 1, getFilho(i));
            }
            setFilho(1, getFilho(0));
            setFilho(0, irmao.getFilho(irmao.numChaves));
            irmao.removerFilho(irmao.numChaves);
            setChave(0, irmao.chaves[irmao.numChaves - 1]);
            irmao.removerChave(irmao.numChaves - 1);
        }
    }
    
    /**
     * Rotaciona chaves da direita para a esquerda.
     * 
     * @param irmao Nó irmão à direita
     * @param chavePai Chave do pai que está entre os nós
     */
    // Explicado em docs/aux/indexNode/rotacionarDireita.md
    public void rotacionarDireita(IndexNode irmao, int chavePai) {
        if (folha) {
            inserirChave(irmao.chaves[0], irmao.enderecos[0]);
            irmao.removerChave(0);
        } else {
            inserirChave(chavePai, -1);
            setChave(numChaves - 1, irmao.chaves[0]);
            setFilho(numChaves, irmao.getFilho(0));
            irmao.removerChave(0);
            irmao.removerFilho(0);
        }
    }
    
    /**
     * Converte o nó em um array de bytes para persistência.
     * 
     * @return Array de bytes representando o nó
     * @throws IOException Se houver erro na conversão
     */
    // Explicado em docs/aux/indexNode/toByteArray.md
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        dos.writeInt(ordem);
        dos.writeInt(numChaves);
        dos.writeBoolean(folha);
        
        for (int i = 0; i < ordem; i++) {
            dos.writeInt(chaves[i]);
            dos.writeLong(enderecos[i]);
            dos.writeLong(filhos[i]);
        }
        dos.writeLong(filhos[ordem]);
        
        return baos.toByteArray();
    }
    
    /**
     * Reconstrói um nó a partir de um array de bytes.
     * 
     * @param b Array de bytes contendo os dados do nó
     * @throws IOException Se houver erro na conversão
     */
    // Explicado em docs/aux/indexNode/fromByteArray.md
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        
        ordem = dis.readInt();
        numChaves = dis.readInt();
        folha = dis.readBoolean();
        
        chaves = new int[ordem];
        enderecos = new long[ordem];
        filhos = new long[ordem + 1];
        
        for (int i = 0; i < ordem; i++) {
            chaves[i] = dis.readInt();
            enderecos[i] = dis.readLong();
            filhos[i] = dis.readLong();
        }
        filhos[ordem] = dis.readLong();
    }
    
    /**
     * Calcula o tamanho máximo em bytes de um nó serializado.
     * 
     * @param ordem Ordem da árvore B+
     * @return Tamanho máximo em bytes
     */
    // Explicado em docs/aux/indexNode/getTamanhoMaximo.md
    public static int getTamanhoMaximo(int ordem) {
        // ordem (4) + numChaves (4) + folha (1) + 
        // ordem * (chave(4) + endereco(8) + filho(8)) + filho extra(8)
        return 4 + 4 + 1 + ordem * (4 + 8 + 8) + 8;
    }
}
