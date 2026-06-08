package com.checklist.ui.gui;

import com.checklist.dao.CategoriaDAO;
import com.checklist.dao.LogConclusaoDAO;
import com.checklist.dao.TagDAO;
import com.checklist.dao.TarefaDAO;
import com.checklist.dao.TarefaTagDAO;
import com.checklist.dao.UsuarioDAO;
import com.checklist.manager.BackupManager;
import com.checklist.model.Categoria;
import com.checklist.model.LogConclusao;
import com.checklist.model.Tag;
import com.checklist.model.Tarefa;
import com.checklist.model.TarefaTag;
import com.checklist.model.Usuario;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.io.File;
import java.util.List;
import java.util.Optional;

public class App extends Application {

    private UsuarioDAO usuarioDAO;
    private TarefaDAO tarefaDAO;
    private CategoriaDAO categoriaDAO;
    private TagDAO tagDAO;
    private LogConclusaoDAO logDAO;
    private TarefaTagDAO tarefaTagDAO;

    @Override
    public void init() throws Exception {
        usuarioDAO = new UsuarioDAO();
        tarefaDAO = new TarefaDAO();
        categoriaDAO = new CategoriaDAO();
        tagDAO = new TagDAO();
        logDAO = new LogConclusaoDAO();
        tarefaTagDAO = new TarefaTagDAO();
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
        tabPane.getTabs().add(createBackupTab());

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

        TableColumn<Tarefa, String> tagsCol = new TableColumn<>("Tags");
        tagsCol.setCellValueFactory(cell -> {
            try {
                List<TarefaTag> tts = tarefaTagDAO.buscarTagsPorTarefa(cell.getValue().getId());
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < tts.size(); i++) {
                    Tag t = tagDAO.buscarTag(tts.get(i).getIdTag());
                    if (t != null) {
                        sb.append(t.getNome());
                        if (i < tts.size() - 1) sb.append(", ");
                    }
                }
                return new SimpleStringProperty(sb.toString());
            } catch (Exception e) { return new SimpleStringProperty("Erro"); }
        });

        table.getColumns().addAll(userCol, catCol, tagsCol, createColumn("Status", "status"), createColumn("Vencimento", "dataVencimento"));

        Button btnRefresh = new Button("Atualizar");
        btnRefresh.setOnAction(e -> refreshTable(table, () -> tarefaDAO.listarTodas()));

        Button btnOrder = new Button("Listar por ID (Ordenado)");
        btnOrder.setOnAction(e -> refreshTable(table, () -> {
            try { return tarefaDAO.listarOrdenado(); } 
            catch(Exception ex) { showError("Erro na Árvore B+", ex); return null; }
        }));

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
                try {
                    tarefaDAO.concluirTarefa(selected.getId(), "");
                    refreshTable(table, () -> tarefaDAO.listarTodas());
                } catch (Exception ex) {
                    showError("Erro ao concluir", ex);
                }
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

        actions.getChildren().addAll(btnAdd, btnEdit, btnConcluir, btnDelete, btnRefresh, btnOrder);
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

        ListView<Tag> listTags = new ListView<>();
        listTags.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listTags.setPrefHeight(120);
        listTags.setCellFactory(lv -> new ListCell<Tag>() {
            @Override
            protected void updateItem(Tag item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getNome());
            }
        });

        try {
            cbUser.getItems().addAll(usuarioDAO.listarTodos());
            cbCat.getItems().addAll(categoriaDAO.listarTodas());
            listTags.getItems().addAll(tagDAO.listarTodas());
            
            if (tarefa != null) {
                for(Usuario u : cbUser.getItems()) if(u.getId() == tarefa.getIdUser()) cbUser.setValue(u);
                for(Categoria c : cbCat.getItems()) if(c.getId() == tarefa.getIdCategoria()) cbCat.setValue(c);
                
                List<TarefaTag> currentRels = tarefaTagDAO.buscarTagsPorTarefa(tarefa.getId());
                for (Tag t : listTags.getItems()) {
                    for (TarefaTag r : currentRels) {
                        if (r.getIdTag() == t.getId()) {
                            listTags.getSelectionModel().select(t);
                        }
                    }
                }
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
        grid.add(new Label("Tags:"), 0, 6);
        grid.add(listTags, 1, 6);

        Button btnSave = new Button("Salvar");
        btnSave.setOnAction(e -> {
            try {
                int userId = cbUser.getValue() != null ? cbUser.getValue().getId() : -1;
                int catId = cbCat.getValue() != null ? cbCat.getValue().getId() : 0;
                
                if (tarefa == null) {
                    Tarefa novaTarefa = new Tarefa(userId, catId, txtTitulo.getText(), txtDesc.getText(), 
                        new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()), 
                        cbStatus.getValue(), txtVenc.getText());
                    if (tarefaDAO.incluirTarefa(novaTarefa)) {
                        for (Tag tag : listTags.getSelectionModel().getSelectedItems()) {
                            tarefaTagDAO.incluirRelacionamento(new TarefaTag(tag.getId(), novaTarefa.getId()));
                        }
                    }
                } else {
                    tarefa.setIdUser(userId);
                    tarefa.setIdCategoria(catId);
                    tarefa.setTitulo(txtTitulo.getText());
                    tarefa.setDescricao(txtDesc.getText());
                    tarefa.setStatus(cbStatus.getValue());
                    tarefa.setDataVencimento(txtVenc.getText());
                    if (tarefaDAO.alterarTarefa(tarefa)) {
                        tarefaTagDAO.excluirTagsPorTarefa(tarefa.getId());
                        for (Tag tag : listTags.getSelectionModel().getSelectedItems()) {
                            tarefaTagDAO.incluirRelacionamento(new TarefaTag(tag.getId(), tarefa.getId()));
                        }
                    }
                }
                refreshTable(table, () -> tarefaDAO.listarTodas());
                stage.close();
            } catch (Exception ex) {
                showError("Erro ao salvar", ex);
            }
        });

        grid.add(btnSave, 1, 7);
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

        Button btnOrder = new Button("Listar por ID (Ordenado)");
        btnOrder.setOnAction(e -> refreshTable(table, () -> {
            try { return tagDAO.listarOrdenado(); } 
            catch(Exception ex) { showError("Erro na Árvore B+", ex); return null; }
        }));

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

        Button btnVerTarefas = new Button("Ver Tarefas");
        btnVerTarefas.setOnAction(e -> {
            Tag selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showTagTarefasList(selected);
        });

        actions.getChildren().addAll(btnAdd, btnEdit, btnDelete, btnVerTarefas, btnRefresh, btnOrder);
        layout.getChildren().addAll(new Label("Gerenciamento de Tags"), table, actions);
        tab.setContent(layout);

        refreshTable(table, () -> tagDAO.listarTodas());
        return tab;
    }

    private void showTagTarefasList(Tag tag) {
        Stage stage = new Stage();
        stage.setTitle("Tarefas com a Tag: " + tag.getNome());

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        ListView<String> listTarefas = new ListView<>();
        try {
            List<TarefaTag> rels = tarefaTagDAO.buscarTarefasPorTag(tag.getId());
            for (TarefaTag r : rels) {
                Tarefa t = tarefaDAO.buscarTarefa(r.getIdTarefa());
                if (t != null) listTarefas.getItems().add(t.getTitulo() + " (ID: " + t.getId() + ")");
            }
        } catch (Exception e) { showError("Erro ao carregar tarefas", e); }

        layout.getChildren().addAll(new Label("Tarefas associadas:"), listTarefas);
        stage.setScene(new Scene(layout, 400, 400));
        stage.show();
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

    // --- BACKUP ---
    private Tab createBackupTab() {
        Tab tab = new Tab("Backup");
        tab.setClosable(false);
        
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        
        Label title = new Label("Gerenciamento de Backup");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label infoLabel = new Label();
        infoLabel.setWrapText(true);
        
        // Botão Backup Huffman
        Button btnHuffman = new Button("Backup com Huffman");
        btnHuffman.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnHuffman.setPrefWidth(250);
        btnHuffman.setOnAction(e -> {
            infoLabel.setText("Executando backup Huffman...");
            new Thread(() -> {
                try {
                    String result = BackupManager.backupHuffman();
                    javafx.application.Platform.runLater(() -> 
                        infoLabel.setText("✓ Backup Huffman concluído: " + result));
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> 
                        infoLabel.setText("✗ Erro no backup Huffman: " + ex.getMessage()));
                }
            }).start();
        });
        
        // Botão Backup LZW
        Button btnLZW = new Button("Backup com LZW");
        btnLZW.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        btnLZW.setPrefWidth(250);
        btnLZW.setOnAction(e -> {
            infoLabel.setText("Executando backup LZW...");
            new Thread(() -> {
                try {
                    String result = BackupManager.backupLZW();
                    javafx.application.Platform.runLater(() -> 
                        infoLabel.setText("✓ Backup LZW concluído: " + result));
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> 
                        infoLabel.setText("✗ Erro no backup LZW: " + ex.getMessage()));
                }
            }).start();
        });
        
        // Botão Listar Backups
        Button btnList = new Button("Listar Backups");
        btnList.setOnAction(e -> {
            try {
                File backupDir = new File("./backups");
                if (!backupDir.exists()) {
                    infoLabel.setText("Nenhum backup encontrado.");
                    return;
                }
                StringBuilder sb = new StringBuilder("Backups disponíveis:\n");
                for (File f : backupDir.listFiles()) {
                    if (f.getName().endsWith(".huf") || f.getName().endsWith(".lzw")) {
                        sb.append("  • ").append(f.getName()).append(" (")
                          .append(String.format("%,d", f.length())).append(" bytes)\n");
                    }
                }
                infoLabel.setText(sb.toString());
            } catch (Exception ex) {
                infoLabel.setText("Erro: " + ex.getMessage());
            }
        });
        
        HBox buttonBox = new HBox(15, btnHuffman, btnLZW, btnList);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        layout.getChildren().addAll(title, buttonBox, infoLabel);
        layout.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        
        tab.setContent(layout);
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
        if (tarefaTagDAO != null) tarefaTagDAO.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
