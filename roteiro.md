# Roteiro de Apresentação - Checklist App (Fase V)

Este roteiro foi elaborado para guiar a gravação de um vídeo de **no máximo 5 minutos**, apresentando de forma clara, técnica e objetiva as novidades e implementações da **Fase V** do projeto Checklist App.

---

## 📌 Visão Geral do Vídeo
- **Duração Estimada:** ~4 minutos e 30 segundos (margem de segurança de 30 segundos).
- **Tom:** Técnico, profissional e dinâmico.
- **Apresentador(es):** Pode ser gravado por uma única pessoa dividindo os tópicos ou em grupo (Felipe, Lucas e Thayná).
- **Recursos Visuais:** Captura de tela com slides explicativos, código-fonte no IDE e demonstração do aplicativo em execução no navegador.

---

## 🎬 Estrutura da Gravação (Cena a Cena)

### Cena 1: Introdução e Contexto (0:00 - 0:45)
* **Visual:** Slide de abertura com o título do projeto "Checklist App - Fase V", nome dos integrantes (Felipe Portes, Lucas Reis, Thayná Antunes) e do professor Walisson Ferreira. Câmera no apresentador ou transição rápida para a tela do sistema.
* **Áudio (O que falar):**
  > "Olá a todos! Sejam muito bem-vindos à apresentação da Fase V do nosso Checklist App. Nesta etapa do projeto, nós focamos em duas grandes áreas de melhoria para o sistema: a otimização das buscas textuais utilizando algoritmos avançados de casamento de padrões e o reforço na segurança de dados através de criptografia simétrica. 
  >
  > O nosso principal objetivo nesta fase foi aplicar esses conceitos teóricos em cenários práticos e reais da nossa aplicação, garantindo alto desempenho na localização de tarefas e a privacidade das informações dos usuários."

---

### Cena 2: Escolha dos Campos Textuais (0:45 - 1:15)
* **Visual:** Mostrar o Dashboard do Checklist App em funcionamento, com a lista de tarefas e a tela de perfil ou gerenciamento de usuários.
* **Áudio (O que falar):**
  > "Para implementar a busca por padrões, nós selecionamos os campos mais críticos para o usuário final. No caso das Tarefas, aplicamos a busca nos campos de 'título' e 'descrição', que é onde os usuários mais precisam de agilidade para achar suas atividades diárias.
  >
  > Já na parte administrativa, para os Usuários, aplicamos nos campos de 'nome' e 'e-mail'. Isso torna o gerenciamento de contas muito mais rápido e eficiente."

---

### Cena 3: Algoritmos de Busca: KMP e Boyer-Moore (1:15 - 2:15)
* **Visual:** Slide comparativo rápido mostrando o conceito do KMP (tabela LPS) e do Boyer-Moore (Bad Character e Good Suffix). Em seguida, focar brevemente no código Java do KMP ou Boyer-Moore no VS Code / IntelliJ.
* **Áudio (O que falar):**
  > "Nós implementamos dois algoritmos clássicos da computação: o KMP e o Boyer-Moore.
  >
  > O KMP funciona em tempo linear $O(n + m)$ e utiliza a tabela de falhas LPS (Longest Proper Prefix which is also Suffix). Essa tabela evita que o cursor do texto volte atrás quando há um mismatch, economizando muito processamento.
  >
  > O Boyer-Moore, por outro lado, lê o texto da direita para a esquerda. Ele utiliza duas heurísticas potentes para realizar saltos maiores no texto: a regra do caractere ruim e a do sufixo bom. A cada falha, ele calcula o maior salto possível entre as duas heurísticas e desloca o padrão de forma inteligente."

---

### Cena 4: Integração do Sistema e Demonstração Prática (2:15 - 3:15)
* **Visual:** Gravação de tela mostrando a interação do usuário com o aplicativo: clicando no botão "Pesquisar Padrão (KMP/BM)", digitando um termo no modal, selecionando o algoritmo e exibindo o resultado com o texto destacado em amarelo (`<mark>`).
* **Áudio (O que falar):**
  > "A integração foi feita de ponta a ponta. No backend, criamos a classe `SearchManager.java` que unifica os dois algoritmos e disponibiliza o serviço através do endpoint REST `/api/search`. Esse endpoint já valida as permissões do usuário, retornando apenas registros que o usuário tem permissão de visualizar.
  >
  > No frontend React, adicionamos um modal moderno e interativo no Dashboard. Ao pesquisar, a resposta do backend é exibida em tempo real, destacando os trechos exatos encontrados nos títulos e descrições utilizando marcações visuais com tags de highlight, sem quebrar acentuações ou a formatação do texto."

---

### Cena 5: Dificuldades Superadas (3:15 - 4:00)
* **Visual:** Mostrar o trecho do código onde os ajustes de bug do Boyer-Moore foram feitos (especialmente a prevenção de loop infinito ou o ajuste do Bad Character).
* **Áudio (O que falar):**
  > "Durante o desenvolvimento, enfrentamos alguns desafios bem interessantes. O principal deles foi ajustar a lógica do Boyer-Moore. 
  >
  > Corrigimos um bug crítico de subtração dupla na regra do caractere ruim que fazia com que o algoritmo ignorasse ocorrências válidas no texto. Outro ponto crítico foi contornar um loop infinito que esgotava a memória quando ocorria um casamento perfeito de strings no final do texto. Resolvemos isso garantindo um avanço mínimo de pelo menos 1 caractere a cada iteração.
  >
  > Além disso, no frontend, tratamos as buscas usando expressões regulares para destacar os matches de forma dinâmica mantendo a integridade de letras maiúsculas, minúsculas e acentuações."

---

### Cena 6: Criptografia XOR (4:00 - 4:40)
* **Visual:** Mostrar a classe `XORCrypto.java` e o arquivo `dados/usuarios.db` exibindo as senhas salvas em formato Base64.
* **Áudio (O que falar):**
  > "Por fim, na área de segurança, aplicamos criptografia no campo de senha da entidade Usuário, pois é o dado mais crítico do nosso sistema.
  >
  > Implementamos uma criptografia simétrica usando a operação XOR com uma chave secreta persistente. A senha resultante é codificada em Base64 para que possa ser salva de forma legível no arquivo `usuarios.db`. Devido às propriedades matemáticas do XOR, o mesmo método com a mesma chave é usado tanto para criptografar no cadastro quanto para descriptografar e validar as credenciais durante o login."

---

### Cena 7: Conclusão (4:40 - 5:00)
* **Visual:** Tela final com o link do GitHub do projeto e agradecimento dos alunos. Câmera de volta para o apresentador se aplicável.
* **Áudio (O que falar):**
  > "Com essas melhorias, a Fase V eleva a maturidade do Checklist App, tornando-o não apenas mais rápido na busca de registros volumosos, mas também alinhado com boas práticas de proteção de dados. 
  >
  > Agradecemos a atenção do professor Walisson e de todos que assistiram. O código completo está disponível no nosso repositório no GitHub. Muito obrigado!"

---

## 💡 Dicas para uma Ótima Gravação
1. **Ritmo de Fala:** Fale de forma natural, mas com firmeza e sem pressa. Pratique a leitura do áudio algumas vezes antes de começar a gravar.
2. **Qualidade de Áudio:** Utilize um microfone adequado e grave em um local sem ruídos de fundo. O áudio de qualidade é essencial para a avaliação técnica.
3. **Divisão de Falas:** Se o grupo for se dividir na fala:
   - **Felipe:** Introdução, Tópico dos Campos Textuais.
   - **Thayná:** Algoritmos de Busca (KMP e BM), Integração Frontend/Backend.
   - **Lucas:** Dificuldades superadas e Criptografia XOR + Conclusão.
4. **Resolução de Tela:** Ao demonstrar o código ou o app, aumente o zoom da tela (Ctrl + '+' no navegador e no editor) para que os textos fiquem legíveis mesmo em resoluções menores.
