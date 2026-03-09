# Documentação Fase I TP

**Alunos:**
- Felipe Portes Antunes
- Lucas Teixeira Reis
- Thayná Andrade Caldeira Antunes

**Professor:** Walisson Ferreira de Carvalho

## 1. Descrição do Problema 

O sistema deve permitir o cadastro e gerenciamento de **Tarefas**, **Categorias** e **Tags**, possibilitando a geração de Logs de Conclusão associados a tarefas finalizadas. Deve armazenar os dados em arquivo binário com cabeçalho e controle de exclusão lógica por lápide.

## 2. Objetivo do Trabalho

- Desenvolver um sistema que permita o CRUD de Tarefas, Categorias e Tags.
- Garantir persistência em arquivos binários com controle de exclusão lógica.
- Fornecer documentação contendo Diagrama de Caso de Uso (DCU), Diagrama Entidade-Relacionamento (DER) e Arquitetura Proposta.

## 3. Requisitos Funcionais

- **RF01 - Cadastro de Categorias:** O sistema deve permitir criar, listar, atualizar e excluir categorias (ex: "Faculdade", "Trabalho")
- **RF02 - Cadastro de Etiquetas:** O sistema deve permitir o gerenciamento de etiquetas independentes que serão associadas às tarefas.
- **RF03 - Cadastro de Tarefas:** O sistema deve permitir a criação de tarefas, vinculando-as a uma **Categoria** obrigatória
- **RF04 - Associação N:N (Tarefas e Etiquetas):** O sistema deve permitir que uma tarefa tenha múltiplas etiquetas e que uma etiqueta pertença a múltiplas taefas.
- **RF05 - Geração de Log de Conclusão:** Ao marcar uma tarefa como "Concluída", o sistema deve gerar um registro de log (equivalente ao cupom do pedido) contendo o ID da tarefa, data de conclusão e resumo das etiquetas utilizadas


