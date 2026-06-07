# Roteiro: Checklist App - Evolução e Indexação (Fase III)

**Duração estimada:** 5 minutos  
**Tom:** Profissional, técnico e direto.

---

## 0:00 - 0:45 | Introdução: O Desafio da Escala
*   **Visual:** Tela inicial do Checklist App (Interface JavaFX) ou o diagrama de classes.
*   **Locução:** "Bem-vindos! Hoje vamos explorar a Fase III do Checklist App. O objetivo desta etapa foi transformar uma aplicação funcional em um sistema de alta performance, capaz de lidar com grandes volumes de dados através de indexação avançada e relacionamentos complexos."
*   **Destaque na tela:** Texto "Relacionamento N:N" e "Índices B+ e Hash".

## 0:45 - 1:45 | O Cérebro do Sistema: B+ vs. Hash
*   **Visual:** Diagrama comparativo simplificado. De um lado uma árvore (B+), do outro uma tabela de dispersão (Hash).
*   **Locução:** "Para garantir buscas instantâneas, não percorremos mais os arquivos sequencialmente. Utilizamos uma estratégia híbrida:
    1.  **Árvore B+:** Indexa todas as chaves primárias (IDs). Escolhemos a B+ porque ela permite buscas logarítmicas e a recuperação ordenada dos registros — essencial para nossas listagens.
    2.  **Hash Extensível:** Gerencia os relacionamentos. Com tempo de busca médio constante (O(1)), o Hash é perfeito para responder rapidamente: 'Quais tags pertencem a esta tarefa?' sem precisar ler o banco inteiro."

## 1:45 - 2:45 | O Relacionamento N:N (Tarefas e Tags)
*   **Visual:** Código da classe `TarefaTagDAO` ou o cálculo da chave composta.
*   **Locução:** "O grande destaque desta fase é o relacionamento N:N entre Tarefas e Tags. Implementamos isso através de uma tabela intermediária gerenciada pelo `TarefaTagDAO`.
    Para identificar unicamente um par tarefa-tag no índice, usamos uma técnica de **Chave Composta Lógica**: multiplicamos o ID da tarefa por um milhão e somamos o ID da tag. Isso gera um ID único que o nosso gerenciador de arquivos entende nativamente, mantendo a simplicidade da arquitetura."

## 2:45 - 3:45 | Integridade e Persistência de Baixo Nível
*   **Visual:** Exibição do padrão de bytes (Cabeçalho de 12 bytes / Lápide '*').
*   **Locução:** "Não basta salvar dados, é preciso manter a integridade. O sistema impede a exclusão de uma Tag se ela estiver em uso, e ao deletar uma Tarefa, todas as suas associações e logs são limpos automaticamente.
    Na camada de persistência, seguimos o padrão rigoroso de arquivos:
    - Um cabeçalho de 12 bytes para controle de IDs e espaços vazios.
    - O uso de 'lápides' para exclusão lógica.
    - E o reaproveitamento de espaço em disco através de uma lista encadeada de registros deletados. É gestão de memória em nível de arquivo."

## 3:45 - 4:30 | Integração no CRUD e Arquitetura
*   **Visual:** Navegação rápida pela estrutura de pastas (Pasta `./dados/` com os arquivos `.db` e `_idx.db`).
*   **Locução:** "Toda essa complexidade é invisível para o usuário. No `TarefaDAO`, a inclusão ou edição de uma tarefa dispara silenciosamente as atualizações nos índices Hash e B+.
    A estrutura do projeto agora está modularizada: temos o núcleo de estruturas de dados, a camada de acesso a dados (DAOs) e a interface gráfica em JavaFX, tudo operando sobre arquivos binários otimizados na pasta de dados."

## 4:30 - 5:00 | Conclusão
*   **Visual:** O app rodando, adicionando uma tag a uma tarefa.
*   **Locução:** "Com a Fase III concluída, o Checklist App deixa de ser apenas um CRUD básico e passa a ser um sistema de gerenciamento de arquivos robusto, preparado para performance e consistência de dados. Obrigado por acompanhar!"
*   **Visual:** Créditos e link do repositório.

---

### Dicas para a Gravação:
1.  **Demonstre o código:** Ao falar da chave composta (1:45), mostre exatamente a linha de código: `(id_tarefa * 1000000) + id_tag`.
2.  **Arquivos Binários:** Mostre a pasta `./dados/` e os arquivos gerados. Isso prova que o sistema de arquivos está funcionando.
3.  **Performance:** Se possível, mostre um log no terminal indicando o tempo de busca ou o uso do índice durante uma pesquisa.
