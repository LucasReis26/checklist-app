# Classe Arquivo

## Descrição

A classe arquivo é uma classe genérica que será utilizada por todos as classes que implementarem as funções existentes na interface Registro, essas funções serão necessárias para um bom funcionamento da classe Arquivo.

## Atributos

```java 
private static final int TAM_CABECALHO = 12;
private RandomAccessFile arquivo;
private String nomeArquivo;
private Constructor<T> construtor;
```

**TAM_CABECALHO:** Variável utilizada pra guardar o tamanho que será utilizada no *cabeçalho do arquivo* nesse caso estamos utilizando 12 bytes. 

> **CABEÇALHO:** O cabeçalho é basicamente a primeira informação que teremos no nosso arquivo, nesse caso guardaremos o ID do último usuário criado que será um int (4 bytes), e um long(8 bytes) contendo o endereço de memória secundária do primeiro dado excluído se houver, o tamanho do nosso cabecalho será a junção do tamanho dessas duas informações, ou seja, 4 + 8 = 12. 

**arquivo:** aqui estamos requisitando nossa classe uma forma de acessar o arquivo que estamos criando, RandomAccessFile é a classe que fará a ponte entre o arquivo e a memória primária (memória RAM), ela também é importante por nos permitir mudar a posição do ponteiro do arquivo com a função `seek()`, nos permitindo pular dados com mais facilidade. 

**nomeArquivo:** Como a classe Arquivo é uma classe genéria e utilizaremos ela pra criar o arquivo de várias classes ao longo do TP de AEDs-III precisamos de um nome para conseguir criar e gerenciar múltiplos arquivos de classes diferentes.

**construtor:** Outra classe genéria, motivo parecido com o nome do arquivo mas com alguns detalhes a mais, fora a questão de estarmos utilizando uma classe pra gerenciar vários arquivos e várias outras classes, e apesar de em AEDs-III mexermos com memória secundária (HDs e SSDs) em alguns momentos utilizaremos a memória primária (memória RAM) para manipular alguns dados, instanciaremos objetos e realizaremos operações com essas classes, logo precisamos do seu construtor 

> Obs.: Mais um motivo também para a classe genérica que utilizarmos precisar ser uma extensão da interface Registro, utilizaremos suas funções para manipular seus dados.

