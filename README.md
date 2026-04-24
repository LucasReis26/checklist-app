# Checklist App

Sistema de gerenciamento de tarefas, categorias e tags desenvolvido para a disciplina de Algoritmos e Estruturas de Dados III (AEDS-III).

## Integrantes do Grupo
- Felipe Portes Antunes
- Lucas Teixeira Reis
- Thayná Andrade Caldeira Antunes

## Entregas:

- [Fase I](./docs/fase-i.md)
- [Fase II](./docs/fase-ii.md)

## Descrição do Projeto
O **Checklist App** é um sistema projetado para o cadastro e gerenciamento de Tarefas, Categorias e Tags. O sistema permite:
- **CRUD completo**: Criação, leitura, atualização e exclusão de Tarefas, Categorias e Tags.
- **Persistência em Arquivos Binários**: Armazenamento eficiente de dados em arquivos binários.
- **Exclusão Lógica**: Implementação de controle de exclusão por "lápide", onde os registros não são removidos fisicamente de imediato.
- **Geração de Logs**: Emissão de registros de conclusão ao finalizar tarefas, contendo ID, data e resumo das tags associadas.
- **Filtros e Buscas**: Busca direta por ID e filtragem de tarefas por categoria.
- **Arquitetura**: O projeto segue o padrão **MVC + DAO** (Model, View, Controller + Data Access Object).

## Pré-requisitos
Para executar este projeto, você precisará do **Java JDK 21** e do **Maven** instalados em sua máquina.

### 1. Instalação do Maven

#### Linux (Ubuntu/Debian)
No terminal, execute:
```bash
sudo apt update
sudo apt install maven
```

#### Windows
1. Baixe o Maven em: [maven.apache.org](https://maven.apache.org/download.cgi).
2. Extraia o arquivo para uma pasta (ex: `C:\maven`).
3. Adicione o caminho da pasta `bin` (ex: `C:\maven\bin`) às **Variáveis de Ambiente** do Sistema (`PATH`).
4. Verifique a instalação abrindo o terminal e digitando:
   ```cmd
   mvn -version
   ```

### 2. Execução do Projeto

Navegue até a raiz do projeto (onde o arquivo `pom.xml` está localizado) e execute os comandos abaixo:

#### Limpar e compilar o projeto:
```bash
mvn clean compile
```

#### Executar a aplicação com JavaFX:
```bash
mvn javafx:run
```

---
