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
- [Fase V](./RELATORIO_FASE_V.md)

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
2. **Recuperação de Desastres**: Ferramenta de restauração que permite voltar o sistema a um estado anterior a partir de um arquivo de backup.
3. **Monitoramento**: Acesso a estatísticas de compressão (tamanho original vs. compactado, taxa de compressão e tempo de execução).

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

## Fase V - Casamento de Padrões e Criptografia

Nesta fase final, foram adicionados algoritmos de busca por padrões textuais e segurança para campos sensíveis:

1. **Casamento de Padrões**:
   - **KMP (Knuth-Morris-Pratt)**: Algoritmo de busca linear com complexidade temporal ótima $O(n + m)$ que utiliza a tabela LPS (*Longest Proper Prefix which is also Suffix*) para evitar retrocessos de busca no texto principal.
   - **Boyer-Moore**: Algoritmo que compara o padrão da direita para a esquerda a fim de realizar saltos grandes no texto, utilizando Heurística de Caractere Ruim (*Bad Character*) e de Sufixo Bom (*Good Suffix*).
   - Ambos os algoritmos foram integrados a um endpoint REST (`/api/search`) e a uma interface interativa no Frontend ("Pesquisar Padrão").

2. **Criptografia**:
   - **XOR (OU Exclusivo)**: Algoritmo simétrico aplicado ao campo `senha` do usuário para garantir privacidade dos dados armazenados no arquivo binário de usuários, codificado em Base64 antes de ser gravado.
