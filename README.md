# Checklist App

Sistema de gerenciamento de tarefas, categorias e tags desenvolvido para a disciplina de Algoritmos e Estruturas de Dados III (AEDS-III).

## Integrantes do Grupo
- Felipe Portes Antunes
- Lucas Teixeira Reis
- Thayná Andrade Caldeira Antunes

## Entregas:

- [Fase I](./docs/fase-i.md)
- [Fase II](./docs/fase-ii.md)
- [Fase III](./docs/fase-iii.md)
- [Fase IV](./docs/fase-iv.md)
- [Fase V](./docs/fase-v.md)

## Descrição do Projeto
O **Checklist App** é um sistema projetado para o cadastro e gerenciamento de Tarefas, Categorias e Tags. O sistema permite:
- **Interface Web Moderna**: Frontend responsivo e interativo integrado ao backend Spring Boot.
- **CRUD completo**: Criação, leitura, atualização e exclusão de Tarefas, Categorias e Tags.
- **Persistência em Arquivos Binários**: Armazenamento eficiente de dados em arquivos binários customizados (Árvores B+, Hash Extensível e Listas Invertidas), sem o uso de bancos de dados relacionais.
- **Exclusão Lógica**: Implementação de controle de exclusão por "lápide", onde os registros não são removidos fisicamente de imediato.
- **Geração de Logs**: Emissão de registros de conclusão ao finalizar tarefas, contendo ID, data e resumo das tags associadas.
- **Arquitetura**: O projeto segue o padrão **MVC + DAO** (Model, View, Controller + Data Access Object).

## Como a Aplicação Funciona
A aplicação utiliza uma arquitetura cliente-servidor. O backend é desenvolvido em **Java com Spring Boot**, servindo tanto a API REST quanto os arquivos estáticos do frontend.

- **Persistência de Baixo Nível**: Toda a persistência é feita diretamente em disco, utilizando implementações próprias de estruturas de indexação para garantir performance.
- **Autenticação e Sessão**: O sistema gerencia múltiplos usuários, permitindo que cada um tenha seu próprio conjunto privado de tarefas, categorias e tags.
- **Ciclo de Vida da Tarefa**: As tarefas podem ser criadas, associadas a categorias e tags, e finalmente concluídas, o que gera um registro histórico (log).

## Funcionalidades de Administrador
O sistema possui um controle de acesso baseado em papéis (Roles). Usuários com a role `ADMIN` possuem acesso a ferramentas de manutenção do sistema.

### Como Acessar o Perfil de Administrador
Para testar todas as funcionalidades, inclusive as de baixo nível (backups e compressão), utilize as credenciais padrão:

- **Email:** `admin@checklist.com`
- **Senha:** `admin`

**Como navegar:**
1. Realize o login com as credenciais acima.
2. No **Dashboard**, clique no botão **"Admin"** localizado no cabeçalho (ao lado do nome do usuário).
3. Alternativamente, acesse diretamente via URL: `http://localhost:8080/admin`

As funcionalidades exclusivas incluem:

1. **Gestão de Backups**:
   - **Compressão Huffman**: Backup otimizado utilizando frequências de caracteres.
   - **Compressão LZW**: Backup utilizando dicionário dinâmico para compressão.
2. **Recuperação de Desastres**: Ferramenta de restauração que permite voltar o sistema a um estado anterior a partir de um arquivo de backup diretamente pela interface web.
3. **Monitoramento de Compressão**: Acesso a estatísticas detalhadas de compressão (tamanho original vs. compactado, taxa de compressão e tempo de execução).
4. **Gestão de Contas de Usuários**:
   - Visualização de todas as contas registradas no sistema.
   - Painel com contagem de total de usuários, administradores e contas comuns.
   - Exibição de detalhes da conta de cada usuário (Nome, ID, E-mail e Papel/Role).
   - **Visualização de Senhas Criptografadas**: Exibição da senha criptografada utilizando o algoritmo **XOR (codificada em Base64)** diretamente a partir do arquivo de persistência do backend.
5. **Busca de Padrões Global**:
   - Administradores têm permissão para realizar buscas de padrões (utilizando KMP ou Boyer-Moore) em todas as tarefas do sistema (busca global), enquanto usuários comuns buscam apenas em suas próprias tarefas privadas.

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

#### Executar a aplicação (Spring Boot):
```bash
mvn spring-boot:run
```

Após iniciar, a aplicação estará disponível em:
**[http://localhost:8080](http://localhost:8080)**

---

