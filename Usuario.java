import java.io.*;

public class Usuario implements Registro {
    private int id_user;
    private String nome;
    private String email;
    private String senha;

    public Usuario() {
        this(-1, "", "", "");
    }

    public Usuario(String n, String e, String s) {
        this(-1, n, e, s);
    }

    public Usuario(int i, String n, String e, String s) {
        this.id_user = i;
        this.nome = n;
        this.email = e;
        this.senha = s;
    }

    // Getters e Setters
    public int getId() { 
        return id_user; 
    }
    
    public void setId(int id) { 
        this.id_user = id; 
    }
    
    public String getNome() { 
        return nome; 
    }
    
    public void setNome(String nome) { 
        this.nome = nome; 
    }
    
    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }
    
    public String getSenha() { 
        return senha; 
    }
    
    public void setSenha(String senha) { 
        this.senha = senha; 
    }

    // Implementação do método toByteArray()
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.id_user);
        dos.writeUTF(this.nome);
        dos.writeUTF(this.email);
        dos.writeUTF(this.senha);
        return baos.toByteArray();
    }

    // Implementação do método fromByteArray()
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id_user = dis.readInt();
        this.nome = dis.readUTF();
        this.email = dis.readUTF();
        this.senha = dis.readUTF();
    }

    @Override
    public String toString() {
        return "\nID........: " + this.id_user +
               "\nNome......: " + this.nome +
               "\nEmail.....: " + this.email +
               "\nSenha.....: " + this.senha;
    }
}