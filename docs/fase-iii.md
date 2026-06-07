# Documentação Técnica - Fase III

**Alunos:**

- Felipe Portes Antunes
- Lucas Teixeira Reis
- Thayná Andrade Caldeira Antunes

**Professor:** Walisson Ferreira de Carvalho

**Link do Projeto:** [Checklist App](https://github.com/LucasReis26/checklist-app/tree/main)

**Link do vídeo desta etapa:** [Exemplo de funcionamento do Projeto](https://www.youtube.com/watch?v=WUc6TlEErX0)

## Relatório Técnico e Formulário

Este documento descreve as implementações realizadas na Fase III do projeto Checklist App, focando no relacionamento N:N e nas estruturas de indexação.

### 1. Qual foi o relacionamento N:N escolhido e quais tabelas ele conecta?
O relacionamento N:N escolhido foi entre **Tarefas** e **Tags**. Ele conecta a tabela de tarefas (`tarefas.db`) com a tabela de tags (`tags.db`) através de uma tabela associativa intermediária gerenciada por `TarefaTagDAO`.

### 2. Qual estrutura de índice foi utilizada (B+ ou Hash Extensível)? Justifique a escolha.
Foram utilizadas ambas as estruturas em diferentes contextos para maximizar a eficiência:
*   **Árvore B+ (ArquivoIndex):** Utilizada para indexar as chaves primárias (IDs) de todas as entidades principais (Usuários, Tarefas, Categorias, Tags). **Justificativa:** Permite tanto a busca exata por ID em tempo logarítmico quanto a recuperação de todos os registros de forma ordenada (travessia das folhas), requisito essencial desta fase.
*   **Hash Extensível (HashExtensivel):** Utilizada para gerenciar os relacionamentos N:N e 1:N. **Justificativa:** Oferece busca em tempo constante (O(1) médio), ideal para recuperar rapidamente a lista de tags de uma tarefa ou a lista de tarefas de um usuário/categoria.

### 3. Como foi implementada a chave composta da tabela intermediária?
A chave composta da tabela `TarefaTag` foi implementada de forma lógica através de um cálculo numérico que garante unicidade:
`ID_Composto = (id_tarefa * 1000000) + id_tag`.
Essa implementação permite tratar a combinação de duas chaves estrangeiras como uma única chave primária para o gerenciador de arquivos e índices.

### 4. Como é feita a busca eficiente de registros por meio do índice?
A busca é realizada em duas frentes:
*   **Busca por ID:** O `ArquivoIndex` consulta a Árvore B+ para obter o endereço físico do registro no arquivo `.db` sem percorrer o arquivo sequencialmente.
*   **Busca de Relacionamentos:** O sistema utiliza índices Hash dedicados (`HashIndexTarefaTags` e `HashIndexTagTarefas`). Ao buscar as tags de uma tarefa, o índice Hash retorna diretamente a lista de IDs associados, evitando scans completos na tabela associativa.

### 5. Como o sistema trata a integridade referencial (remoção/atualização) entre as tabelas?
A integridade é tratada na camada DAO:
*   **Exclusão de Tag:** O `TagDAO` verifica no `TarefaTagDAO` se existem tarefas associadas à tag antes de permitir a exclusão. Se houver, a operação é impedida com uma exceção.
*   **Exclusão de Tarefa:** Ao excluir uma tarefa, o `TarefaDAO` remove automaticamente todas as suas associações no `TarefaTagDAO` e também exclui todos os logs de conclusão associados, garantindo a integridade referencial e evitando dados órfãos.

### 6. Como foi organizada a persistência dos dados dessa nova tabela (mesmo padrão de cabeçalho e lápide)?
A nova tabela associativa utiliza a classe `ArquivoIndex<TarefaTag>`, que segue rigorosamente o padrão de:
*   **Cabeçalho:** 12 bytes (Último ID + Ponteiro para lista de deletados).
*   **Lápide:** 1 byte no início de cada registro (' ' para ativo, '*' para excluído).
*   **Tamanho:** 2 bytes (short) indicando o tamanho do registro.
*   **Reaproveitamento:** Gerenciamento de espaços vazios via lista encadeada de registros deletados.

### 7. Descreva como o código da tabela intermediária se integra com o CRUD das tabelas principais.
O `TarefaTagDAO` é utilizado dentro do `TarefaDAO` e `TagDAO`. Por exemplo, no formulário de edição de Tarefas na interface gráfica, o usuário pode adicionar ou remover tags, o que dispara chamadas para `incluirRelacionamento` ou `excluirRelacionamento`. A exclusão de uma tarefa também limpa automaticamente suas entradas na tabela intermediária.

### 8. Descreva como está organizada a estrutura de diretórios e módulos no repositório após esta fase.
O projeto mantém uma estrutura organizada por responsabilidades:
*   **Raiz:** Classes de Modelo (`Tarefa`, `Tag`), DAOs e Gerenciadores de Relacionamento.
*   **Estruturas de Dados:** `HashExtensivel.java`, `ArquivoIndex.java` (B+ Tree), `IndexNode.java`.
*   **Interface:** `App.java` (JavaFX - Interface Principal) e pastas de `Menu...` (Console - Testes).
*   **Dados:** Pasta `./dados/` contendo arquivos `.db` e seus respectivos índices `_idx.db`.
*   **Documentação:** Pasta `./docs/` contendo os manuais das fases I, II e III.

---
