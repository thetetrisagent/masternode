package tetris;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionHandler implements Runnable {
	private ServerSocketHandler serverSocketHandler;
	private ServerSocket server;
	private DataRepo data;

	public ConnectionHandler(ServerSocketHandler serverSocketHandler, DataRepo data) {
		this.serverSocketHandler = serverSocketHandler;
		this.server = serverSocketHandler.getServerSocket();
		this.data = data;
	}
	
	@Override
	public void run() {
		System.out.println("ConnectionHandler running...");
		try {
			while (true) {
				Socket clientSocket;
				clientSocket = server.accept();
				serverSocketHandler.addClient(new ObjectOutputStream(clientSocket.getOutputStream()));
				new Thread(new InputHandler(data,clientSocket)).start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
