# Manual de Lógica e Estrutura — Checklist App

Este documento explica a arquitetura, as correções técnicas e o funcionamento do sistema. O projeto combina uma interface moderna (Web) com um motor de persistência de baixo nível customizado (Arquivos Indexados).

---

## 1. Organização de Pacotes (Arquitetura)

O código foi reorganizado seguindo os princípios de **Clean Architecture**:

*   **`com.checklist.demo.model`**: **Entidades** do sistema (`Usuario`, `Tarefa`, etc.). São objetos de dados puros.
*   **`com.checklist.demo.persistence`**: O **"Motor" do Banco de Dados**. Contém a implementação da **Árvore B+** e do **Hash Extensível**. É a camada que fala diretamente com os arquivos `.db`.
*   **`com.checklist.demo.dao`**: A camada de **Acesso a Dados**. Conecta as Entidades ao motor de persistência.
*   **`com.checklist.demo.controller`**: Camada **Web (REST)**. Recebe as requisições do navegador e envia respostas JSON.
*   **`com.checklist.demo.cli`**: Camada de **Terminal**. Mantém a compatibilidade com o uso via console.
*   **`com.checklist.demo.config`**: Configurações do Spring (Injeção de Dependência).

---

## 2. Fluxo de Execução do Spring Boot (Para Debug)

Para debugar o programa, é essencial entender em qual ordem o Spring Boot "acorda" os componentes:

### A. Inicialização (Startup)
1.  **`DemoApplication.main()`**: O ponto de entrada. O Spring inicia o contêiner de inversão de controle (IoC).
2.  **Scan de Componentes**: O Spring procura por classes anotadas com `@RestController` ou classes definidas em `@Configuration`.
3.  **`DataConfig.java`**: O Spring executa os métodos anotados com `@Bean`. 
    *   *Ordem Interna:* Primeiro ele cria os **Managers**, depois os **DAOs**.
    *   *Debug:* Se o programa travar ao abrir, o erro provavelmente está nos construtores dos DAOs dentro do `DataConfig`.
4.  **Tomcat**: O servidor web embutido sobe na porta 8080.

### B. Ciclo de uma Requisição (Request Lifecycle)
Quando você clica em "Criar Tarefa" no navegador:
1.  **Frontend (JS)**: Dispara um `fetch('/api/tarefas', { method: 'POST', ... })`.
2.  **DispatcherServlet**: O "maestro" do Spring recebe a rede e descobre qual Controller deve cuidar dela.
3.  **Controller (`TarefaController`)**: O método `criarTarefa` é chamado. O JSON do navegador é convertido automaticamente para um objeto `Tarefa`.
4.  **DAO (`TarefaDAO`)**: O Controller chama o DAO. Aqui a lógica de negócio (validar usuário/categoria) é executada.
5.  **Persistência (`ArquivoIndex`)**: O DAO pede para o motor salvar os bytes no disco. O `flush()` garante que o arquivo seja atualizado.
6.  **Resposta**: O Controller retorna `201 Created` e o objeto salvo volta para o JavaScript.

---

## 3. Motor de Persistência (Árvore B+)

Diferente de bancos tradicionais (SQL), este projeto usa arquivos binários (`.db`).

### Funcionamento do `ArquivoIndex<T>`:
1.  **Arquivo de Dados (`.db`)**: Armazena os registros em sequência. Cada registro possui uma "lápide" (byte de deleção), um marcador de tamanho e os dados.
2.  **Arquivo de Índice (`_idx.db`)**: Uma **Árvore B+**. Ela mapeia um `ID` para uma `Posição em Bytes` no arquivo de dados.

### Melhorias de Estabilidade:
*   **Sincronização Atômica**: O uso de `getFD().sync()` impede que o sistema operacional deixe dados no "cache de memória". Se o PC desligar, os dados estarão no disco.
*   **Leitura Resiliente**: O sistema agora usa `read()` com verificação de bytes lidos em vez de `readFully()` bruto, evitando o erro de `EOFException` se o índice estiver um pouco à frente dos dados.

---

## 4. Guia de Debugging (Onde olhar o erro?)

| Sintoma | Onde olhar? | Provável Causa |
| :--- | :--- | :--- |
| **"Erro ao carregar dados" no site** | Console do Navegador (F12) | A API retornou 500 ou 401. |
| **Erro 500 no console do navegador** | **Console do Java (IDE/Terminal)** | Ocorreu uma Exception no Java (olhe o `StackTrace`). |
| **Erro 401 no console do navegador** | `AuthController` | A sessão expirou ou o usuário não está logado. |
| **Dados salvos não aparecem no CLI** | `Principal.java` / `dados/*.db` | Conflito de arquivos se o CLI e o Spring rodarem ao mesmo tempo. |
| **Botão não clica** | `style.css` / `dashboard.html` | Algum elemento invisível está por cima (Z-Index ou Modal). |

---

## 5. Dicas de Ouro
1.  **Limpeza**: Sempre que o banco parecer "louco", apague a pasta `dados/` e reinicie. Inconsistências manuais nos arquivos quebram a Árvore B+.
2.  **Logs**: Use `System.out.println` ou `e.printStackTrace()` nos Controllers para rastrear o caminho da informação.
3.  **Jackson**: Se um campo vier como `null` no Java, verifique se o nome no JSON do JavaScript é exatamente igual ao nome do campo na classe Java.

---
*Documento atualizado em 22/04/2026 para fins educacionais.*
