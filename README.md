# tf-redes

<h3>Como executar:</h3>
- Copiar a pasta deploy para o local desejado
<br>
- Rodar server ou client em um terminal (ex.: java -jar server.jar)
<br>
<h3>Server:</h3>
- Por padrão o <b>server</b> estabelece um socket UDP na <b>porta 6000</b> para recebimento de mensagens / arquivos
<br>
- O <b>server</b> utiliza o arquivo <b>config.txt</b> para criar outro socket UDP com destino para o IP e porta da primeira linha
<br>
- A segunda linha indica o apelido do <b>server</b>, a terceira o tempo para visualização das mensagens e a quarta se este servidor inicialmente possui o token
<br>
- Pode ser inicializado com outro arquivo de configuração e porta para recebimento de mensagens / arquivo desde que informado na linha de comando (ex.: java -jar server.jar config2.txt 6001)
<br>
<h3>Client:</h3>
- Inicializado automaticamente com o nome de <b>Alice</b> para enviar mensagens para o <b>servido local porta 6000</b>
<br>
- Pode ser inicializado com outro nome e porta de envio desde que informado na linha de comando (ex.: java -jar client.jar Bob 6001)
