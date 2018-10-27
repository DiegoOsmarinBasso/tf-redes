package client;

// classes para input e output streams e
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
// DatagramaSocket,InetAddress,DatagramaPacket
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class UDPClient {

	private static final String DATA = "2345";
	private static String NICKNAME = "Alice";
	private static int SND_PORT = 6000;

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

		System.out.println("\nClient Name: " + NICKNAME);

		while (true) {
			System.out.println(
					"\n\nPara enviar uma mensagem digite \"text \" seguido do nome do destinatario e da mensagem");
			System.out.println(
					"\nPara enviar um arquivo digite \"file \" seguido do nome do destinatario e do nome do arquivo");
			System.out.println("\nPara sair digite \"exit\"\n");

			// uma linha do teclado
			String sentence = inFromUser.readLine();

			// Mensagem
			if (sentence.startsWith("text ")) {

				String[] entry = sentence.substring(5).split(" ", 2);

				if (entry.length < 2) {
					System.out.println("\nEntrada invalida!");
					continue;
				}

				String destination = entry[0];
				String message = entry[1];

				// Monta a mensagem a ser enviada
				sentence = DATA + ";naocopiado:" + NICKNAME + ":" + destination + ":M:" + message;

				// Obtem os bytes da mensagem
				byte[] sendData = sentence.getBytes();

				// Cria pacote com o dado, o endereco do server e porta do servidor
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, SND_PORT);

				// Envia o pacote
				clientSocket.send(sendPacket);

				// Avisa o usuario
				System.out.println("\nMensagem enviada ao servidor:");
				System.out.println(sentence);

			}
			// Arquivo
			else if (sentence.startsWith("file ")) {

				String[] entry = sentence.split(" ");
				String destination = "";
				String file = "";
				String fileContent = "";

				if (entry.length != 3) {
					System.out.println("\nEntrada invalida!");
					continue;
				} else {
					destination = entry[1];
					file = System.getProperty("user.dir") + "/" + entry[2];
				}

				try (BufferedReader br = new BufferedReader(new FileReader(file))) {

					while (br.ready()) {
						fileContent += br.readLine() + "\r\n";
					}

					sentence = DATA + ";naocopiado:" + NICKNAME + ":" + destination + ":A:" + fileContent;

					byte[] sendData = sentence.getBytes();

					// cria pacote com o dado, o endereco do server e porta do servidor
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, SND_PORT);

					// envia o pacote
					clientSocket.send(sendPacket);

				} catch (Exception e) {
					System.out.println("\nArquivo de entrada invalido!");
				}
			}
			// Sair
			else if ("exit".equals(sentence)) {
				break;
			}
			// Erro
			else {
				System.out.println("\nEntrada invalida!");
			}
		}
		// fecha o cliente
		clientSocket.close();
	}
}