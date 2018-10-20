package client;

// classes para input e output streams e
import java.io.BufferedReader;
import java.io.InputStreamReader;
// DatagramaSocket,InetAddress,DatagramaPacket
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class UDPClient {

	private static final String DATA = "2345";
	private static String NICKNAME = "Alice";
	private static int SND_PORT = 6000;
	private static final int BUFF_SIZE = 1024;

	public static void main(String args[]) throws Exception {

		// Nome de arquivo passado por parametro?
		NICKNAME = args.length > 0 ? args[0] : NICKNAME;
		SND_PORT = args.length > 1 ? Integer.parseInt(args[1]) : SND_PORT;

		// cria o stream do teclado
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

		// declara socket cliente
		DatagramSocket clientSocket = new DatagramSocket();

		// obtem endereco IP do servidor com o DNS
		InetAddress IPAddress = InetAddress.getByName("localhost");

		while (true) {
			System.out.println("\n\nPara enviar um arquivo digite \"file \" seguido do nome do destinatário e do nome do arquivo");
			System.out.println("Para enviar uma mensagem digite \"text \" seguido do nome do destinatário e da mensagem");
			System.out.println("Para sair digite \"exit\"\n");

			// uma linha do teclado
			String sentence = inFromUser.readLine();

			// Mensagem
			if (sentence.startsWith("text ")) {

				String[] entry = sentence.substring(5).split(" ", 2);

				if (entry.length < 2) {
					System.out.println("\nEntrada inválida!");
					continue;
				}

				String destination = entry[0];
				String message = entry[1];

				sentence = DATA + ";nãocopiado:" + NICKNAME + ":" + destination + ":M:" + message;

				for (int i = 0; i < sentence.length(); i = i + BUFF_SIZE) {
					byte[] sendData = new byte[BUFF_SIZE];

					// Se tamanho restante maior que 1024, envia os primeiros 1024 caracteres
					if (sentence.length() > i + BUFF_SIZE) {
						sendData = sentence.substring(i, i + BUFF_SIZE).getBytes();
					}
					// Senao envia todo restante da mensagem
					else {
						sendData = sentence.substring(i, sentence.length()).getBytes();
					}

					// cria pacote com o dado, o endereco do server e porta do servidor
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, SND_PORT);

					// envia o pacote
					clientSocket.send(sendPacket);
					
					// avisa o usuario
					System.out.println("\nMensagem enviada ao servidor:");
					System.out.println(sentence);
				}
			}
			// Arquivo
			else if (sentence.startsWith("file ")) {

				for (int i = 0; i < sentence.length(); i = i + BUFF_SIZE) {
					byte[] sendData = new byte[BUFF_SIZE];

					// Se tamanho restante maior que 1024, envia os primeiros 1024 caracteres
					if (sentence.length() > i + BUFF_SIZE) {
						sendData = sentence.substring(i, i + BUFF_SIZE).getBytes();
					}
					// Senao envia todo restante da mensagem
					else {
						sendData = sentence.substring(i, sentence.length()).getBytes();
					}

					// cria pacote com o dado, o endereco do server e porta do servidor
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, SND_PORT);

					// envia o pacote
					clientSocket.send(sendPacket);
				}
			}
			// Sair
			else if ("exit".equals(sentence)) {
				clientSocket.send(new DatagramPacket("exit".getBytes(), 4, IPAddress, SND_PORT));
				break;
			}
			// Erro
			else {
				System.out.println("\nEntrada inválida!");
			}
		}
		// fecha o cliente
		clientSocket.close();
	}
}