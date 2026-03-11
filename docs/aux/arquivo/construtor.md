# Construtor da classe Arquivo

## Descrição

Construtor responsável por criar e preparar o cabecalho para começarmos a escrever.

## Criando o diretório

```java 
File diretorio = new File("./dados");
if (!diretorio.exists()) diretorio.mkdir();

diretorio = new File("./dados/" + nomeArquivo);
if (!diretorio.exists()) diretorio.mkdir();
```

Nas linhas acima nosso programa está basicamente verificando se existe algum arquivo ou diretório chamado "dados", caso exista ele não faz nada mas caso não exista ele irá criar os diretórios baseados no nome que estamos recebendo da classe que chamou *Arquivo*.

> Exemplo: No caso do CRUD de Usuário estamos criando o caminho de pastas './dados/usuario/' até então

## Criação do arquivo

```java 
this.nomeArquivo = "./dados/" + nomeArquivo + "/" + nomeArquivo + ".db";
this.construtor = construtor;
this.arquivo = new RandomAccessFile(this.nomeArquivo, "rw");
```

Aqui estamos de fato criando o arquivo binário que utilizaremos para escrever nossos dados, além disso também estamos definindo o construtor como o construtor da classe que chamou *Arquivo* para realizaros operações que foram implementadas graças a interface Registro.

> Exemplo: No caso do CRUD de Usuário, estamos criando o arquivo no seguinte caminho: ./dados/usuario/usuario.db.
>
> Obs: O construtor no caso do exemplo acima está armazenando o construtor de usuário para que a gente consiga instanciar um objeto usuário dentro da classe Arquivo e realizar operações.

## Preparando o cabeçalho

```java 
if (arquivo.length() < TAM_CABECALHO) {
      arquivo.writeInt(0); // Último ID usado
      arquivo.writeLong(-1); // Lista de registros excluídos
}
```

Ao instanciar um arquivo duas coisas podem acontecer, ou é um novo arquivo que não tem nada, ou esse arquivo já existe e tem coisas escritas, lembre-se que quando criamos o diretório nós verificamos se tem o caminho e arquivos já existentes. No caso do arquivo já existir o tamanho do arquivo já será **pelo menos** igual ao *tamanho do cabeçalho*(12 bytes) logo não executamos nosso if, mas caso seja um arquivo novo ele conterá 0 bytes, menor que o *tamanho do cabeçalho*, então criaremos o cabeçalho dizendo que o último ID que utilizamos é o 0, e que ainda não possuíms uma lista de excluídos utilizaremos nesse caso o -1 pra representar que não há excluídos.
