package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler {
	private Socket clientSocket;
	private ObjectInputStream input;
	private ObjectOutputStream output;

	public ClientHandler(Socket clientSocket) {
		try {
			this.clientSocket = clientSocket;
			this.input = new ObjectInputStream(clientSocket.getInputStream());
			this.output = new ObjectOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ObjectInputStream getObjectInputStream() {
		return this.input;
	}

	public ObjectOutputStream getObjectOutputStream() {
		return this.output;
	}

	public Socket getClientSocket() {
		return this.clientSocket;
	}
}
