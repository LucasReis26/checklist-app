import java.io.*;

/**
 * Classe que representa a entidade Usuário do sistema.
 * Implementa a interface Registro para permitir persistência em arquivo.
 */
public class Usuario implements Registro {
    // Explicado em docs/aux/usuario/usuario.md
    private int id_user;
    private String nome;
    private String email;
    private String senha;

    /**
     * Construtor padrão.
     * Inicializa o usuário com valores padrão.
     */
    // Explicado em docs/aux/usuario/construtorPadrao.md
    public Usuario() {
        this(-1, "", "", "");
    }

    /**
     * Construtor com dados básicos (sem ID).
     * O ID será gerado automaticamente pelo sistema.
     * 
     * @param n Nome do usuário
     * @param e Email do usuário
     * @param s Senha do usuário
     */
    // Explicado em docs/aux/usuario/construtorDados.md
    public Usuario(String n, String e, String s) {
        this(-1, n, e, s);
    }

    /**
     * Construtor completo com ID.
     * Utilizado principalmente para reconstrução a partir do arquivo.
     * 
     * @param i ID do usuário
     * @param n Nome do usuário
     * @param e Email do usuário
     * @param s Senha do usuário
     */
    // Explicado em docs/aux/usuario/construtorCompleto.md
    public Usuario(int i, String n, String e, String s) {
        this.id_user = i;
        this.nome = n;
        this.email = e;
        this.senha = s;
    }

    /**
     * Retorna o ID do usuário.
     * 
     * @return ID do usuário
     */
    @Override
    // Explicado em docs/aux/usuario/getId.md
    public int getId() { 
        return id_user; 
    }
    
    /**
     * Define o ID do usuário.
     * 
     * @param id Novo ID do usuário
     */
    @Override
    // Explicado em docs/aux/usuario/setId.md
    public void setId(int id) { 
        this.id_user = id; 
    }
    
    /**
     * Retorna o nome do usuário.
     * 
     * @return Nome do usuário
     */
    // Explicado em docs/aux/usuario/getNome.md
    public String getNome() { 
        return nome; 
    }
    
    /**
     * Define o nome do usuário.
     * 
     * @param nome Novo nome do usuário
     */
    // Explicado em docs/aux/usuario/setNome.md
    public void setNome(String nome) { 
        this.nome = nome; 
    }
    
    /**
     * Retorna o email do usuário.
     * 
     * @return Email do usuário
     */
    // Explicado em docs/aux/usuario/getEmail.md
    public String getEmail() { 
        return email; 
    }
    
    /**
     * Define o email do usuário.
     * 
     * @param email Novo email do usuário
     */
    // Explicado em docs/aux/usuario/setEmail.md
    public void setEmail(String email) { 
        this.email = email; 
    }
    
    /**
     * Retorna a senha do usuário.
     * 
     * @return Senha do usuário
     */
    // Explicado em docs/aux/usuario/getSenha.md
    public String getSenha() { 
        return senha; 
    }
    
    /**
     * Define a senha do usuário.
     * 
     * @param senha Nova senha do usuário
     */
    // Explicado em docs/aux/usuario/setSenha.md
    public void setSenha(String senha) { 
        this.senha = senha; 
    }

    /**
     * Converte o objeto Usuario em um array de bytes para persistência.
     * Formato: [id (4 bytes)] [nome (UTF)] [email (UTF)] [senha (UTF)]
     * 
     * @return Array de bytes representando o objeto
     * @throws IOException Se houver erro na conversão
     */
    @Override
    // Explicado em docs/aux/usuario/toByteArray.md
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.id_user);
        dos.writeUTF(this.nome);
        dos.writeUTF(this.email);
        dos.writeUTF(this.senha);
        return baos.toByteArray();
    }

    /**
     * Reconstrói um objeto Usuario a partir de um array de bytes.
     * 
     * @param b Array de bytes contendo os dados do usuário
     * @throws IOException Se houver erro na conversão
     */
    @Override
    // Explicado em docs/aux/usuario/fromByteArray.md
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id_user = dis.readInt();
        this.nome = dis.readUTF();
        this.email = dis.readUTF();
        this.senha = dis.readUTF();
    }

    /**
     * Retorna uma representação textual do usuário para exibição.
     * 
     * @return String formatada com os dados do usuário
     */
    @Override
    // Explicado em docs/aux/usuario/toString.md
    public String toString() {
        return "\nID........: " + this.id_user +
               "\nNome......: " + this.nome +
               "\nEmail.....: " + this.email +
               "\nSenha.....: " + this.senha;
    }
}