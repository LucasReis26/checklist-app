public class UsuarioDAO {
    private Arquivo<Usuario> arqUsuarios;

    public UsuarioDAO() throws Exception {
        arqUsuarios = new Arquivo<>("usuarios", Usuario.class.getConstructor());
    }

    public Usuario buscarUsuario(int id) throws Exception {
        return arqUsuarios.read(id);
    }

    public boolean incluirUsuario(Usuario usuario) throws Exception {
        return arqUsuarios.create(usuario) > 0;
    }

    public boolean alterarUsuario(Usuario usuario) throws Exception {
        return arqUsuarios.update(usuario);
    }

    public boolean excluirUsuario(int id) throws Exception {
        return arqUsuarios.delete(id);
    }
}