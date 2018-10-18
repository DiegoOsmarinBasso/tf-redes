package client;

// classes para input e output streams e
import java.io.BufferedReader;
import java.io.InputStreamReader;
// DatagramaSocket,InetAddress,DatagramaPacket
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class UDPClient {
	public static void main(String args[]) throws Exception {
		// cria o stream do teclado
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

		// declara socket cliente
		DatagramSocket clientSocket = new DatagramSocket();

		// obtem endereco IP do servidor com o DNS
		InetAddress IPAddress = InetAddress.getByName("localhost");

		while (true) {
			System.out.println("\nPara enviar um arquivo digite \"file \" seguido do nome do arquivo");
			System.out.println("Para enviar uma mensagem digite \"text \" seguido da mensagem");
			System.out.println("Para sair digite \"exit\"");

			// uma linha do teclado
			String sentence = inFromUser.readLine();

			// Mensagem
			if (sentence.startsWith("text ")) {

				for (int i = 0; i < sentence.length(); i = i + 1024) {
					byte[] sendData = new byte[1024];

					// Se tamanho restante maior que 1024, envia os primeiros 1024 caracteres
					if (sentence.length() > i + 1024) {
						sendData = sentence.substring(i, i + 1024).getBytes();
					}
					// Senao envia todo restante da mensagem
					else {
						sendData = sentence.substring(i, sentence.length()).getBytes();
					}

					// cria pacote com o dado, o endereco do server e porta do servidor
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 6000);

					// envia o pacote
					clientSocket.send(sendPacket);
				}
			}
			// Arquivo
			else if (sentence.startsWith("file ")) {

				for (int i = 0; i < sentence.length(); i = i + 1024) {
					byte[] sendData = new byte[1024];

					// Se tamanho restante maior que 1024, envia os primeiros 1024 caracteres
					if (sentence.length() > i + 1024) {
						sendData = sentence.substring(i, i + 1024).getBytes();
					}
					// Senao envia todo restante da mensagem
					else {
						sendData = sentence.substring(i, sentence.length()).getBytes();
					}

					// cria pacote com o dado, o endereco do server e porta do servidor
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 6000);

					// envia o pacote
					clientSocket.send(sendPacket);
				}
			}
			// Sair
			else if ("exit".equals(sentence)) {
				clientSocket.send(new DatagramPacket("exit".getBytes(), 4, IPAddress, 6000));
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