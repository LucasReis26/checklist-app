# Método create da classe Arquivo

## Descrição

Esse método será responsável por criar um novo registro de determinada classe na nossa memória secundária.

## Completando o Registro
```java 

public int create(T obj) throws Exception {
    arquivo.seek(0);
    int novoID = arquivo.readInt() + 1;
    arquivo.seek(0);
    arquivo.writeInt(novoID);
    obj.setId(novoID);
    byte[] dados = obj.toByteArray();
```

A classe arquivo já receberá o objeto praticamente pronto mas tem algumas informações que não tem como a gente definir antes de verificar o arquivo relacionado a aquele objeto.

No caso de Usuário, nós já podemos saber o nome do usuário, email, senha mas não tem como a gente saber o ID que esse usuário terá, por sorte o nosso cabeçalho diz isso pra gente então a primeira coisa que faremos é ler o último ID utilizado informado no cabeçalho do arquivo e adicionarmos 1 para definir um novo ID. 

Mas obviamente para o nosso cabeçalho funcionar corretamente devemos atualizar ele, então após definir o novo ID, vamos voltar ao início do arquivo onde o cabecalho está e atualizar o último ID utilizado.

Temos que definir o ID do usuário também então chamamos o método setId do nosso objeto e informamos pra ele qual será o ID dele.

Perceba que nós ainda **não escrevemos nada do nosso objeto no nosso arquivo ainda!** A única informação que escrevemos no arquivo de fato é o ID do objeto que ainda **iremos** escrever.

Na última linha começamos um dos principais passos para de fato escrever o nosso usuário dentro do nosso arquivo, transformar ele em um array de bytes, e pra isso utilizamos o método `toByteArray()` um dos métodos que são requisitados pela interface Registro.

## Procurando um registro excluído

```java 
long endereco = getDeleted(dados.length);
```

Temos sempre que lembrar que esse arquivo ele pode já existir anteriormente, e para melhorar a eficiência do uso de memória do nosso HD ou SSD, nós podemos reutilizar o espaço de um registro que foi excluído anteriormente desde que esse registro seja grande o suficiente pra isso.

`getDeleted(dados.length);`: é um método cujo enviamos o tamanho do registro que iremos escrever e ele retorna um endereço de memória caso tenha um registro do tamanho adequado para ser sobreescrito e retorna -1 caso ou não haja registros excluídos ou não encontre um registro excluído com o tamanho adequado.

### Caso **NÃO** encontre um Registro.

```java 
if (endereco == -1) {
      arquivo.seek(arquivo.length());
      endereco = arquivo.getFilePointer();
      arquivo.writeByte(' '); // Lápide
      arquivo.writeShort(dados.length);
      arquivo.write(dados);
} else {
```

No caso do endereço ser -1, significa que não foi encontrado ou registros excluídos ou um registro com o tamanho adequado, nesse caso nós pegaremos o ponteiro do arquivo e iremos até o final do arquivo para escrever nossos dados.

Aqui escreveremos primeiramente um byte que representará a nossa lápide.

> **Lápide:** Espaço do nosso arquivo que será utilizado para exclusão lógica dos nossos registros, aqui definimos se o registro existe ou não, no caso de existir deixamos a lápide vazia ' ', no caso de não existir a completamos com um asterisco '*' simbolizando que esse registro foi excluído lógicamente (apesar de seus dados permanecerem gravados em memória)

Após a lápide nós escrevemos a quantidade de bytes que o nosso registro irá ocupar, isso é importante tanto para lermos a quantidade de bytes correta de um registro sem contaminação, imagine vc ler os dados de um registro + a lápide e tamanho de registro de outro registro nosso programa quebraria, quanto pra pular um registro que a gente saiba que não é do nosso interesse.

Agora SIM já que escrevemos a lápide pra informar que o nosso registro existe de fato e escrevemos o tamanho do registro nós escrevemos os dados desse registro.
