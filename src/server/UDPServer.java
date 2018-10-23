package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Random;

class UDPServer {

	private static final String TOKEN = "1234";
	private static final String DATA = "2345";
	private static final int BUFF_SIZE = 1024;
	private static final int PERCENT_ERR = 0;
	private static final String NAO_COPIADO = "naocopiado";
	private static final String ERRO = "erro";
	private static final String OK = "OK";
	private static final String TODOS = "TODOS";
	private static final Random RAND = new Random();
	private static final String LOCALHOST = "127.0.0.1";

	private static String fileName = "config.txt";
	private static int rcvPort = 6000;
	private static String destIP = "";
	private static int destPort = 6000;
	private static String nickname = "";
	private static int sleepTime = 3000;
	private static boolean token = false;
	private static LinkedList<String> queue = new LinkedList<>();
	private static String sentence = "";
	private static boolean primeiroErro = true;
	private static boolean enviaDados = true;

	public static void main(String args[]) throws Exception {

		// Nome de arquivo passado por parametro?
		fileName = args.length > 0 ? args[0] : "config.txt";
		rcvPort = args.length > 1 ? Integer.parseInt(args[1]) : rcvPort;

		// Le o arquivo de configuracao
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

			String destIPPort = br.readLine();
			destIP = destIPPort.split(":")[0];
			destPort = Integer.parseInt(destIPPort.split(":")[1]);

			nickname = br.readLine();
			sleepTime = Integer.parseInt(br.readLine()) * 1000;
			token = Boolean.parseBoolean(br.readLine());

		} catch (Exception e) {

			System.out.println("Arquivo de configuracao invalido!");
			System.exit(0);

		}

		// Descomente para descobrir o proprio IP
		InetAddress inetAddress = InetAddress.getLocalHost();
		System.out.println("IP Address:- " + inetAddress.getHostAddress());
		System.out.println("Host Name:- " + inetAddress.getHostName());

		// Cria socket do servidor com a porta rcvPort
		DatagramSocket serverSocket = new DatagramSocket(rcvPort);

		// Espera por pacotes
		while (true) {
			/*
			 * SE POSSUI O TOKEN ENVIA MENSAGEM / ARQUIVO / TOKEN
			 */
			if (token) {

				// Buffer
				byte[] sendData = new byte[BUFF_SIZE];

				// Se ha dados a enviar e eh permitido enviar
				if (!queue.isEmpty() && enviaDados) {
					sentence = queue.peekFirst();
					sendData = sentence.getBytes();
					enviaDados = false;
				}
				// senao envia o token
				else {
					System.out.println("Token enviado!");

					sentence = TOKEN;
					sendData = sentence.getBytes();

					// Nao possui mais o token
					token = false;

					// Quando receber o token, envia dados se houver
					enviaDados = true;
				}

				// cria pacote com o dado, o endereco do server e porta do servidor
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(destIP),
						destPort);

				// envia o pacote
				serverSocket.send(sendPacket);
			}
			/*
			 * SENAO AGUARDA RECEBIMENTO DE MENSAGEM / ARQUIVO
			 */
			else {
				byte[] receiveData = new byte[BUFF_SIZE];
				byte[] sendData = new byte[BUFF_SIZE];

				// declara o pacote a ser recebido
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

				// recebe o pacote da rede ou cliente
				System.out.println("\n\n\nAguardando pacotes...");
				serverSocket.receive(receivePacket);

				// Pega os dados
				String packetMsg = new String(receivePacket.getData()).trim();

				// Pega o endereco IP e a porta da origem para mostrar na tela se for o caso
				InetAddress IPAddress = receivePacket.getAddress();
				int port = receivePacket.getPort();

				// Mensagem recebida do Client
				if (IPAddress.getHostAddress().equals(LOCALHOST)) {
					queue.add(packetMsg);
				}
				// Mensagem de dados recebida da Rede
				else if (packetMsg.startsWith(DATA)) {

					// Descomente para auxiliar no debug
					System.out.println("\nMensagem recebida:\n" + packetMsg + "\n");

					String[] dados = packetMsg.substring(5).split(":", 5);
					String controleErro = dados[0];
					String origem = dados[1];
					String destino = dados[2];
					String tipoDado = dados[3];
					String mensagem = dados[4];

					// Se eh o remetente
					if (origem.equals(nickname)) {
						// Se foi broadcast
						if (TODOS.equals(destino)) {
							System.out.println("\nBroadcast entrege com sucesso!");
							primeiroErro = true;
							if (!queue.isEmpty())
								queue.removeFirst();
						}
						// Senao verifica o controle de erro
						// OK
						else if (OK.equals(controleErro)) {
							System.out.println("\nMensagem entregue ao destino com sucesso!");
							primeiroErro = true;
							if (!queue.isEmpty())
								queue.removeFirst();
						}
						// NAO_COPIADO
						else if (NAO_COPIADO.equals(controleErro)) {
							System.out.println("\nDestinatario nao encontrado!");
							primeiroErro = true;
							if (!queue.isEmpty())
								queue.removeFirst();
						}
						// ERRO
						else if (ERRO.equals(controleErro)) {
							// Se eh o primeiro erro, nao remove mensagem da fila
							if (primeiroErro) {
								System.out.println(
										"Ocorreu um erro, a mesagem sera reenviada na proxima passagem do token!");
								primeiroErro = false;
							}
							// Senao, remove mensagem da fila para nao ser enviada novamente
							else {
								System.out.println("Ocorreu novamente um erro, a mesagem sera descartada!");
								primeiroErro = true;
								if (!queue.isEmpty())
									queue.removeFirst();
							}
						}

						// Obriga a passar o token adiante
						enviaDados = false;

						// Tempo para acompanhar a execucao
						Thread.sleep(sleepTime);
					}
					// Se nao eh o remetente, trata a mensagem e envia para o proximo
					else {
						// Se eh broadcast, apenas mostra dados na tela
						// Se eh o destinatario, mostra dados na tela e retorna com ok / erro
						if (destino.equals(nickname) || destino.equals(TODOS)) {

							System.out.println("\nMensagem recebida de: " + origem);
							System.out.println("IP: " + IPAddress + ":" + port);
							System.out.println("\n" + mensagem + "\n");

							if (destino.equals(nickname)) {
								controleErro = RAND.nextInt(100) < PERCENT_ERR ? ERRO : OK;
							}
						}

						// Tempo para acompanhar a execucao
						Thread.sleep(sleepTime);

						// Envia pacote para o proximo servidor
						packetMsg = DATA + ";" + controleErro + ":" + origem + ":" + destino + ":" + tipoDado + ":"
								+ mensagem;
						sendData = packetMsg.getBytes();

						// Descomente para auxiliar no debug
						System.out.println("\nMensagem enviada:\n" + packetMsg + "\n");

						// declara o pacote a ser enviado
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
								InetAddress.getByName(destIP), destPort);

						// envia o pacote
						serverSocket.send(sendPacket);
					}
				}
				// TOKEN recebido
				else if (packetMsg.startsWith(TOKEN)) {
					token = true;
					System.out.println("\n\n\nToken recebido!\n");
					Thread.sleep(sleepTime);
				}
				// Comando para encerrar o servidor
				else if ("exit".equals(sentence))
					break;
			}
		}
		serverSocket.close();
	}
}