package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;

class UDPServer {

	private static final String TOKEN = "1234";
	private static final String DADOS = "2345";
	private static int RCV_PORT = 6000;
	private static final int BUFF_SIZE = 1024;
	// IPs 172.22.22.156 e 9.85.175.242

	public static void main(String args[]) throws Exception {

		String fileName = "";
		String destIP = "";
		int destPort = 0;
		String nickname = "";
		int tokenTime = 3000;
		boolean token = false;
		LinkedList<String> queue = new LinkedList<>();

		// Nome de arquivo passado por parametro?
		fileName = args.length > 0 ? args[0] : "config.txt";
		RCV_PORT = args.length > 1 ? Integer.parseInt(args[1]) : 6000;

		// Le o arquivo de configuracao
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

			String destIPPort = br.readLine();
			destIP = destIPPort.split(":")[0];
			destPort = Integer.parseInt(destIPPort.split(":")[1]);

			nickname = br.readLine();
			tokenTime = Integer.parseInt(br.readLine()) * 1000;
			token = Boolean.parseBoolean(br.readLine());

		} catch (Exception e) {

			System.out.println("Arquivo de configuração inválido!");
			System.exit(0);

		}

		// Cria socket do servidor com a porta RCV_PORT
		DatagramSocket serverSocket = new DatagramSocket(RCV_PORT);
		String sentence = "";

		// Espera por pacotes
		while (true) {

			/* ****************************************************
			 * SE POSSUI O TOKEN ENVIA MENSAGEM / ARQUIVO / TOKEN
			 **************************************************** */
			if (token) {
				// Se ha dados a enviar
				if (!queue.isEmpty()) {

				}
				// senao envia o token
				else {
					sentence = TOKEN;
					byte[] sendData = new byte[BUFF_SIZE];
					sendData = sentence.getBytes();

					// cria pacote com o dado, o endereco do server e porta do servidor
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
							InetAddress.getByName(destIP), destPort);

					// envia o pacote
					serverSocket.send(sendPacket);

					// Nao possui mais o token
					token = false;
				}
			}

			/* ****************************************************
			 * AGUARDA RECEBIMENTO DE MENSAGEM / ARQUIVO
			 **************************************************** */
			byte[] receiveData = new byte[BUFF_SIZE];
			String dest = "";

			// declara o pacote a ser recebido
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

			// recebe o pacote da rede ou cliente
			System.out.println("\nAguardando pacotes...");
			serverSocket.receive(receivePacket);

			// Pega os dados
			String packetMsg = new String(receivePacket.getData()).trim();

			// Pega o endereco IP e a porta da origem para mostrar na tela se for o caso
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();

			// Mensagem de texto da aplicacao cliente
			if (packetMsg.startsWith("text ")) {

				int i = 0;
				dest = "";

				for (i = 5; i < packetMsg.length() && packetMsg.charAt(i) != ' '; i++) {
					dest += packetMsg.charAt(i);
				}

				// Destinatatrio eh o proprio servidor, apenas imprime na tela
				if (dest.equals(nickname)) {
					System.out.println(packetMsg.substring(++i));
				}
				// Destinatario nao eh local
				else {
					// Monta a mensagem para enviar
					sentence = DADOS + ";naocopiado:" + nickname + ":" + dest + ":M:" + packetMsg.substring(++i);

					// Se possui o token, envia a mensagem
					if (token) {

						byte[] sendData = new byte[BUFF_SIZE];
						sendData = sentence.getBytes();

						// cria pacote com o dado, o endereco do server e porta do servidor
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
								InetAddress.getByName(destIP), destPort);

						// envia o pacote
						serverSocket.send(sendPacket);
					}
					// Senao coloca mensagem na fila
					else {
						// Descarta mensagem se a fila esta cheia
						if (queue.size() >= 10) {
							System.out.println("Fila cheia. Mensagem descartada!");
						}
						// Senao coloca a mensagem no final da fila
						else {
							queue.add(sentence);
						}

					}

				}

			}
			// Arquivo da aplicacao cliente
			else if (packetMsg.startsWith("file ")) {
				int i = 0;
				dest = "";

				for (i = 5; i < packetMsg.length() && packetMsg.charAt(i) != ' '; i++) {
					dest += packetMsg.charAt(i);
				}

				sentence = DADOS + ";naocopiado:" + nickname + ":" + dest + ":A:" + packetMsg.substring(++i);
			}
			// Mensagem de dados vinda de outro servidor
			else if (packetMsg.startsWith(DADOS)) {

				// Mostra dados na tela
				System.out.println("IP: " + IPAddress + ":" + port);
				System.out.println("Mensagem recebida: " + sentence);

			}
			// TOKEN recebido
			else if (packetMsg.startsWith(TOKEN)) {
				token = true;
				System.out.println("\nToken recebido!\n");
				Thread.sleep(tokenTime);
			}

			if ("exit".equals(sentence))
				break;
		}
		serverSocket.close();
	}
}