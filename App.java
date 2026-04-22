import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.util.List;
import java.util.Optional;

public class App extends Application {

    private UsuarioDAO usuarioDAO;
    private TarefaDAO tarefaDAO;
    private CategoriaDAO categoriaDAO;
    private TagDAO tagDAO;
    private LogConclusaoDAO logDAO;

    @Override
    public void init() throws Exception {
        usuarioDAO = new UsuarioDAO();
        tarefaDAO = new TarefaDAO();
        categoriaDAO = new CategoriaDAO();
        tagDAO = new TagDAO();
        logDAO = new LogConclusaoDAO();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Checklist App - AEDs III");

        TabPane tabPane = new TabPane();

        tabPane.getTabs().add(createUsuariosTab());
        tabPane.getTabs().add(createTarefasTab());
        tabPane.getTabs().add(createCategoriasTab());
        tabPane.getTabs().add(createTagsTab());
        tabPane.getTabs().add(createLogsTab());

        Scene scene = new Scene(tabPane, 1100, 750);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- USUARIOS ---
    private Tab createUsuariosTab() {
        Tab tab = new Tab("Usuários");
        tab.setClosable(false);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        TableView<Usuario> table = new TableView<>();
        table.getColumns().add(createColumn("ID", "id"));
        table.getColumns().add(createColumn("Nome", "nome"));
        table.getColumns().add(createColumn("Email", "email"));

        Button btnRefresh = new Button("Atualizar");
        btnRefresh.setOnAction(e -> refreshTable(table, () -> usuarioDAO.listarTodos()));

        HBox actions = new HBox(10);
        Button btnAdd = new Button("Adicionar");
        btnAdd.setOnAction(e -> showUsuarioForm(null, table));
        
        Button btnEdit = new Button("Editar");
        btnEdit.setOnAction(e -> {
            Usuario selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showUsuarioForm(selected, table);
        });

        Button btnDelete = new Button("Excluir");
        btnDelete.setOnAction(e -> {
            Usuario selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    usuarioDAO.excluirUsuario(selected.getId());
                    refreshTable(table, () -> usuarioDAO.listarTodos());
                } catch (Exception ex) {
                    showError("Erro ao excluir", ex);
                }
            }
        });

        actions.getChildren().addAll(btnAdd, btnEdit, btnDelete, btnRefresh);
        layout.getChildren().addAll(new Label("Gerenciamento de Usuários"), table, actions);
        tab.setContent(layout);

        refreshTable(table, () -> usuarioDAO.listarTodos());
        return tab;
    }

    private void showUsuarioForm(Usuario usuario, TableView<Usuario> table) {
        Stage stage = new Stage();
        stage.setTitle(usuario == null ? "Novo Usuário" : "Editar Usuário");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        TextField txtNome = new TextField(usuario == null ? "" : usuario.getNome());
        TextField txtEmail = new TextField(usuario == null ? "" : usuario.getEmail());
        PasswordField txtSenha = new PasswordField();
        if (usuario != null) txtSenha.setText(usuario.getSenha());

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(txtNome, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(txtEmail, 1, 1);
        grid.add(new Label("Senha:"), 0, 2);
        grid.add(txtSenha, 1, 2);

        Button btnSave = new Button("Salvar");
        btnSave.setOnAction(e -> {
            try {
                if (usuario == null) {
                    usuarioDAO.incluirUsuario(new Usuario(-1, txtNome.getText(), txtEmail.getText(), txtSenha.getText()));
                } else {
                    usuario.setNome(txtNome.getText());
                    usuario.setEmail(txtEmail.getText());
                    usuario.setSenha(txtSenha.getText());
                    usuarioDAO.alterarUsuario(usuario);
                }
                refreshTable(table, () -> usuarioDAO.listarTodos());
                stage.close();
            } catch (Exception ex) {
                showError("Erro ao salvar", ex);
            }
        });

        grid.add(btnSave, 1, 3);
        stage.setScene(new Scene(grid));
        stage.show();
    }

    // --- TAREFAS ---
    private Tab createTarefasTab() {
        Tab tab = new Tab("Tarefas");
        tab.setClosable(false);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        TableView<Tarefa> table = new TableView<>();
        table.getColumns().add(createColumn("ID", "id"));
        table.getColumns().add(createColumn("Título", "titulo"));
        
        TableColumn<Tarefa, String> userCol = new TableColumn<>("Usuário");
        userCol.setCellValueFactory(cell -> {
            try {
                Usuario u = usuarioDAO.buscarUsuario(cell.getValue().getIdUser());
                return new SimpleStringProperty(u != null ? u.getNome() : "ID: " + cell.getValue().getIdUser());
            } catch (Exception e) { return new SimpleStringProperty("Erro"); }
        });
        
        TableColumn<Tarefa, String> catCol = new TableColumn<>("Categoria");
        catCol.setCellValueFactory(cell -> {
            try {
                int catId = cell.getValue().getIdCategoria();
                if (catId <= 0) return new SimpleStringProperty("Nenhuma");
                Categoria c = categoriaDAO.buscarCategoria(catId);
                return new SimpleStringProperty(c != null ? c.getNome() : "ID: " + catId);
            } catch (Exception e) { return new SimpleStringProperty("Erro"); }
        });

        table.getColumns().addAll(userCol, catCol, createColumn("Status", "status"), createColumn("Vencimento", "dataVencimento"));

        Button btnRefresh = new Button("Atualizar");
        btnRefresh.setOnAction(e -> refreshTable(table, () -> tarefaDAO.listarTodas()));

        HBox actions = new HBox(10);
        Button btnAdd = new Button("Adicionar");
        btnAdd.setOnAction(e -> showTarefaForm(null, table));
        
        Button btnEdit = new Button("Editar");
        btnEdit.setOnAction(e -> {
            Tarefa selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showTarefaForm(selected, table);
        });

        Button btnConcluir = new Button("Concluir");
        btnConcluir.setOnAction(e -> {
            Tarefa selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                TextInputDialog dialog = new TextInputDialog("");
                dialog.setTitle("Concluir Tarefa");
                dialog.setHeaderText("Concluir tarefa: " + selected.getTitulo());
                dialog.setContentText("Resumo das tags (opcional):");
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(tags -> {
                    try {
                        tarefaDAO.concluirTarefa(selected.getId(), tags);
                        refreshTable(table, () -> tarefaDAO.listarTodas());
                    } catch (Exception ex) {
                        showError("Erro ao concluir", ex);
                    }
                });
            }
        });

        Button btnDelete = new Button("Excluir");
        btnDelete.setOnAction(e -> {
            Tarefa selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    tarefaDAO.excluirTarefa(selected.getId());
                    refreshTable(table, () -> tarefaDAO.listarTodas());
                } catch (Exception ex) {
                    showError("Erro ao excluir", ex);
                }
            }
        });

        actions.getChildren().addAll(btnAdd, btnEdit, btnConcluir, btnDelete, btnRefresh);
        layout.getChildren().addAll(new Label("Gerenciamento de Tarefas"), table, actions);
        tab.setContent(layout);

        refreshTable(table, () -> tarefaDAO.listarTodas());
        return tab;
    }

    private void showTarefaForm(Tarefa tarefa, TableView<Tarefa> table) {
        Stage stage = new Stage();
        stage.setTitle(tarefa == null ? "Nova Tarefa" : "Editar Tarefa");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<Usuario> cbUser = new ComboBox<>();
        cbUser.setConverter(new StringConverter<Usuario>() {
            @Override public String toString(Usuario u) { return u == null ? "" : u.getNome(); }
            @Override public Usuario fromString(String s) { return null; }
        });

        ComboBox<Categoria> cbCat = new ComboBox<>();
        cbCat.setConverter(new StringConverter<Categoria>() {
            @Override public String toString(Categoria c) { return c == null ? "Nenhuma" : c.getNome(); }
            @Override public Categoria fromString(String s) { return null; }
        });

        try {
            cbUser.getItems().addAll(usuarioDAO.listarTodos());
            cbCat.getItems().addAll(categoriaDAO.listarTodas());
            
            if (tarefa != null) {
                for(Usuario u : cbUser.getItems()) if(u.getId() == tarefa.getIdUser()) cbUser.setValue(u);
                for(Categoria c : cbCat.getItems()) if(c.getId() == tarefa.getIdCategoria()) cbCat.setValue(c);
            }
        } catch (Exception e) {}

        TextField txtTitulo = new TextField(tarefa == null ? "" : tarefa.getTitulo());
        TextArea txtDesc = new TextArea(tarefa == null ? "" : tarefa.getDescricao());
        txtDesc.setPrefRowCount(3);
        TextField txtVenc = new TextField(tarefa == null ? "" : tarefa.getDataVencimento());
        ComboBox<String> cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("pendente", "concluida");
        cbStatus.setValue(tarefa == null ? "pendente" : tarefa.getStatus());

        grid.add(new Label("Usuário:"), 0, 0);
        grid.add(cbUser, 1, 0);
        grid.add(new Label("Categoria:"), 0, 1);
        grid.add(cbCat, 1, 1);
        grid.add(new Label("Título:"), 0, 2);
        grid.add(txtTitulo, 1, 2);
        grid.add(new Label("Descrição:"), 0, 3);
        grid.add(txtDesc, 1, 3);
        grid.add(new Label("Vencimento:"), 0, 4);
        grid.add(txtVenc, 1, 4);
        grid.add(new Label("Status:"), 0, 5);
        grid.add(cbStatus, 1, 5);

        Button btnSave = new Button("Salvar");
        btnSave.setOnAction(e -> {
            try {
                int userId = cbUser.getValue() != null ? cbUser.getValue().getId() : -1;
                int catId = cbCat.getValue() != null ? cbCat.getValue().getId() : 0;
                
                if (tarefa == null) {
                    tarefaDAO.incluirTarefa(new Tarefa(userId, catId, txtTitulo.getText(), txtDesc.getText(), 
                        "2023-01-01", cbStatus.getValue(), txtVenc.getText()));
                } else {
                    tarefa.setIdUser(userId);
                    tarefa.setIdCategoria(catId);
                    tarefa.setTitulo(txtTitulo.getText());
                    tarefa.setDescricao(txtDesc.getText());
                    tarefa.setStatus(cbStatus.getValue());
                    tarefa.setDataVencimento(txtVenc.getText());
                    tarefaDAO.alterarTarefa(tarefa);
                }
                refreshTable(table, () -> tarefaDAO.listarTodas());
                stage.close();
            } catch (Exception ex) {
                showError("Erro ao salvar", ex);
            }
        });

        grid.add(btnSave, 1, 6);
        stage.setScene(new Scene(grid));
        stage.show();
    }

    // --- CATEGORIAS ---
    private Tab createCategoriasTab() {
        Tab tab = new Tab("Categorias");
        tab.setClosable(false);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        TableView<Categoria> table = new TableView<>();
        table.getColumns().add(createColumn("ID", "id"));
        table.getColumns().add(createColumn("Nome", "nome"));
        
        TableColumn<Categoria, String> userCol = new TableColumn<>("Usuário");
        userCol.setCellValueFactory(cell -> {
            try {
                Usuario u = usuarioDAO.buscarUsuario(cell.getValue().getIdUser());
                return new SimpleStringProperty(u != null ? u.getNome() : "ID: " + cell.getValue().getIdUser());
            } catch (Exception e) { return new SimpleStringProperty("Erro"); }
        });
        table.getColumns().add(userCol);

        Button btnRefresh = new Button("Atualizar");
        btnRefresh.setOnAction(e -> refreshTable(table, () -> categoriaDAO.listarTodas()));

        HBox actions = new HBox(10);
        Button btnAdd = new Button("Adicionar");
        btnAdd.setOnAction(e -> showCategoriaForm(null, table));
        
        Button btnEdit = new Button("Editar");
        btnEdit.setOnAction(e -> {
            Categoria selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showCategoriaForm(selected, table);
        });

        Button btnDelete = new Button("Excluir");
        btnDelete.setOnAction(e -> {
            Categoria selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    categoriaDAO.excluirCategoria(selected.getId());
                    refreshTable(table, () -> categoriaDAO.listarTodas());
                } catch (Exception ex) {
                    showError("Erro ao excluir", ex);
                }
            }
        });

        actions.getChildren().addAll(btnAdd, btnEdit, btnDelete, btnRefresh);
        layout.getChildren().addAll(new Label("Gerenciamento de Categorias"), table, actions);
        tab.setContent(layout);

        refreshTable(table, () -> categoriaDAO.listarTodas());
        return tab;
    }

    private void showCategoriaForm(Categoria cat, TableView<Categoria> table) {
        Stage stage = new Stage();
        stage.setTitle(cat == null ? "Nova Categoria" : "Editar Categoria");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<Usuario> cbUser = new ComboBox<>();
        cbUser.setConverter(new StringConverter<Usuario>() {
            @Override public String toString(Usuario u) { return u == null ? "" : u.getNome(); }
            @Override public Usuario fromString(String s) { return null; }
        });
        try {
            cbUser.getItems().addAll(usuarioDAO.listarTodos());
            if (cat != null) {
                for(Usuario u : cbUser.getItems()) if(u.getId() == cat.getIdUser()) cbUser.setValue(u);
            }
        } catch (Exception e) {}

        TextField txtNome = new TextField(cat == null ? "" : cat.getNome());

        grid.add(new Label("Usuário:"), 0, 0);
        grid.add(cbUser, 1, 0);
        grid.add(new Label("Nome:"), 0, 1);
        grid.add(txtNome, 1, 1);

        Button btnSave = new Button("Salvar");
        btnSave.setOnAction(e -> {
            try {
                int userId = cbUser.getValue() != null ? cbUser.getValue().getId() : -1;
                if (cat == null) {
                    categoriaDAO.incluirCategoria(new Categoria(userId, txtNome.getText()));
                } else {
                    cat.setIdUser(userId);
                    cat.setNome(txtNome.getText());
                    categoriaDAO.alterarCategoria(cat);
                }
                refreshTable(table, () -> categoriaDAO.listarTodas());
                stage.close();
            } catch (Exception ex) {
                showError("Erro ao salvar", ex);
            }
        });

        grid.add(btnSave, 1, 2);
        stage.setScene(new Scene(grid));
        stage.show();
    }

    // --- TAGS ---
    private Tab createTagsTab() {
        Tab tab = new Tab("Tags");
        tab.setClosable(false);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        TableView<Tag> table = new TableView<>();
        table.getColumns().add(createColumn("ID", "id"));
        table.getColumns().add(createColumn("Nome", "nome"));

        Button btnRefresh = new Button("Atualizar");
        btnRefresh.setOnAction(e -> refreshTable(table, () -> tagDAO.listarTodas()));

        HBox actions = new HBox(10);
        Button btnAdd = new Button("Adicionar");
        btnAdd.setOnAction(e -> showTagForm(null, table));
        
        Button btnEdit = new Button("Editar");
        btnEdit.setOnAction(e -> {
            Tag selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showTagForm(selected, table);
        });

        Button btnDelete = new Button("Excluir");
        btnDelete.setOnAction(e -> {
            Tag selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    tagDAO.excluirTag(selected.getId());
                    refreshTable(table, () -> tagDAO.listarTodas());
                } catch (Exception ex) {
                    showError("Erro ao excluir", ex);
                }
            }
        });

        actions.getChildren().addAll(btnAdd, btnEdit, btnDelete, btnRefresh);
        layout.getChildren().addAll(new Label("Gerenciamento de Tags"), table, actions);
        tab.setContent(layout);

        refreshTable(table, () -> tagDAO.listarTodas());
        return tab;
    }

    private void showTagForm(Tag tag, TableView<Tag> table) {
        Stage stage = new Stage();
        stage.setTitle(tag == null ? "Nova Tag" : "Editar Tag");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        TextField txtNome = new TextField(tag == null ? "" : tag.getNome());

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(txtNome, 1, 0);

        Button btnSave = new Button("Salvar");
        btnSave.setOnAction(e -> {
            try {
                if (tag == null) {
                    tagDAO.incluirTag(new Tag(-1, txtNome.getText()));
                } else {
                    tag.setNome(txtNome.getText());
                    tagDAO.alterarTag(tag);
                }
                refreshTable(table, () -> tagDAO.listarTodas());
                stage.close();
            } catch (Exception ex) {
                showError("Erro ao salvar", ex);
            }
        });

        grid.add(btnSave, 1, 1);
        stage.setScene(new Scene(grid));
        stage.show();
    }

    // --- LOGS ---
    private Tab createLogsTab() {
        Tab tab = new Tab("Logs");
        tab.setClosable(false);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        TableView<LogConclusao> table = new TableView<>();
        table.getColumns().add(createColumn("ID", "id"));
        table.getColumns().add(createColumn("Tarefa ID", "idTarefa"));
        table.getColumns().add(createColumn("Data", "dataConclusao"));
        table.getColumns().add(createColumn("Resumo Tags", "resumoTags"));

        Button btnRefresh = new Button("Atualizar");
        btnRefresh.setOnAction(e -> refreshTable(table, () -> logDAO.listarTodos()));

        layout.getChildren().addAll(new Label("Logs de Conclusão"), table, btnRefresh);
        tab.setContent(layout);

        refreshTable(table, () -> logDAO.listarTodos());
        return tab;
    }

    // --- HELPERS ---
    private <T> TableColumn<T, String> createColumn(String title, String property) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>(property));
        return col;
    }

    private <T> void refreshTable(TableView<T> table, DataLoader<T> loader) {
        try {
            table.getItems().setAll(loader.load());
        } catch (Exception ex) {
            showError("Erro ao carregar dados", ex);
        }
    }

    private void showError(String title, Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(title);
        alert.setContentText(ex.getMessage());
        alert.showAndWait();
    }

    @FunctionalInterface
    interface DataLoader<T> {
        List<T> load() throws Exception;
    }

    @Override
    public void stop() throws Exception {
        if (usuarioDAO != null) usuarioDAO.close();
        if (tarefaDAO != null) tarefaDAO.close();
        if (categoriaDAO != null) categoriaDAO.close();
        if (tagDAO != null) tagDAO.close();
        if (logDAO != null) logDAO.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
