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

		String destIP = "";
		int destPort = 0;
		String nickname = "";
		int tokenTime = 3000;
		boolean token = false;
		LinkedList<String> queue = new LinkedList<>();

		// Nome de arquivo passado por parametro?
		String fileName = args.length > 0 ? args[0] : "config.txt";
		RCV_PORT = args.length > 1 ? Integer.parseInt(args[1]) : RCV_PORT;

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

			/*
			 * SE POSSUI O TOKEN ENVIA MENSAGEM / ARQUIVO / TOKEN
			 */
			if (token) {

				// Buffer
				byte[] sendData = new byte[BUFF_SIZE];

				// Se ha dados a enviar
				if (!queue.isEmpty()) {
					sentence = queue.pollFirst();
					sendData = sentence.getBytes();
				}
				// senao envia o token
				else {
					sentence = TOKEN;
					sendData = sentence.getBytes();

					// Nao possui mais o token
					token = false;
				}

				// cria pacote com o dado, o endereco do server e porta do servidor
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(destIP),
						destPort);

				// envia o pacote
				serverSocket.send(sendPacket);
			}

			/*
			 * AGUARDA RECEBIMENTO DE MENSAGEM / ARQUIVO
			 */
			byte[] receiveData = new byte[BUFF_SIZE];

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

			// Mensagem de dados recebida
			if (packetMsg.startsWith(DADOS)) {

				System.out.println("\nMensagem recebida:\n" + packetMsg + "\n");
				
				String[] dados = packetMsg.substring(5).split(":");
				String controleErro = dados[0];
				String origem = dados[1];
				String destino = dados[2];
				String tipoDado = dados[3];
				String mensagem = "";

				for (int i = 4; i < dados.length; i++) {
					mensagem += dados[i];
				}

				// Se eh o destinatario, mostra dados na tela
				if (destino.equals(nickname)) {
					System.out.println("\nMensagem recebida de: " + origem);
					System.out.println("IP: " + IPAddress + ":" + port);
					System.out.println("Mensagem recebida: " + mensagem);
					System.out.println("\n");
				}
				// Senao coloca pacote na fila
				else {
					queue.add(packetMsg);
				}

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