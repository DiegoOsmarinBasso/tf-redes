package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class UDPServer {

	private static final String TOKEN = "1234";
	private static final String DADOS = "2345";

	public static void main(String args[]) throws Exception {

		String destIPPort = "";
		String nickname = "";
		int tokenTime = 3;
		boolean token = false;

		// Le o arquivo de configuracao
		try (BufferedReader br = new BufferedReader(new FileReader("config.txt"))) {

			destIPPort = br.readLine();
			nickname = br.readLine();
			tokenTime = Integer.parseInt(br.readLine());
			token = Boolean.parseBoolean(br.readLine());

		} catch (Exception e) {

			System.out.println("Arquivo de configuração inválido!");
			System.exit(0);

		}

		// Cria socket do servidor com a porta 6000
		DatagramSocket serverSocket = new DatagramSocket(6000);
		String sentence = "";

		// IPs 172.22.22.156 e 9.80.213.232

		// Espera por pacotes
		while (true) {

			byte[] receiveData = new byte[1024];
			String dest = "";

			// declara o pacote a ser recebido
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

			// recebe o pacote da rede ou cliente
			System.out.println("Aguardando pacotes...");
			serverSocket.receive(receivePacket);

			// pega os dados, o endereco IP e a porta do cliente
			// para poder mandar a msg de volta
			String packetMsg = new String(receivePacket.getData()).trim();
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();

			// Mensagem de texto da aplicacao cliente
			if (packetMsg.startsWith("text ")) {
				int i = 0;
				dest = "";

				for (i = 5; i < packetMsg.length() && packetMsg.charAt(i) != ' '; i++) {
					dest += packetMsg.charAt(i);
				}

				sentence = DADOS + ";naocopiado:" + nickname + ":" + dest + ":M:" + packetMsg.substring(++i);
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

			}
			// TOKEN recebido
			else if (packetMsg.startsWith(TOKEN)) {

			}

			// Mostra dados na tela
			System.out.println("IP: " + IPAddress + ":" + port);
			System.out.println("Mensagem recebida: " + sentence);

			if ("exit".equals(sentence))
				break;
		}
		serverSocket.close();
	}
}