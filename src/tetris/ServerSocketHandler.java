package tetris;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.util.ArrayList;

public class ServerSocketHandler implements Runnable {
	private ServerSocket server;
	private ArrayList<ObjectOutputStream> clients;
	
	public ServerSocketHandler(int port) {
		try {
			this.server = new ServerSocket(port);
			this.clients = new ArrayList<ObjectOutputStream>();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ServerSocket getServerSocket() {
		return this.server;
	}
	
	public void addClient(ObjectOutputStream client) {
		clients.add(client);
	}
	
	public void distributeWork(ArrayList<Command> commands) {
		try {
			while (clients.size() < 5) {
				//block
			}
			for (int i = 0; i < commands.size(); i++) {
				clients.get(i%clients.size()).writeObject(commands.get(i));
			}
		} catch (Exception e) {
			
		}
	}

	@Override
	public void run() {
		System.out.println("ServerSocketHandler running...");
	}
}
