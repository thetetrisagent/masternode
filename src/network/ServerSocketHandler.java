package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

//ServerSocketHandler handles the server socket and the maintains a list of the clients connected. It is also in charge of dispatching work to clients.
public class ServerSocketHandler{
	private ServerSocket server;
	private ArrayList<ClientHandler> clients;
	private int jobCount = 0;
	private Controller controller;
	
	public ServerSocketHandler(int port) {
		System.out.println("server started at " + port);
		try {
			this.server = new ServerSocket(port);
			this.clients = new ArrayList<ClientHandler>();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ServerSocket getServerSocket() {
		return this.server;
	}
	
	public void setController(Controller controller) {
		this.controller = controller;
	}
	
	public void addClient(ClientHandler client) {
		clients.add(client);
	}
	
	public synchronized void removeClient(ClientHandler client) {
		clients.remove(clients.indexOf(client));
		jobCount++;
	}
	
	public synchronized boolean distributeWork(ClientHandler client) {
		try {
			if (jobCount > 0) {
				client.getObjectOutputStream().writeObject(controller.getSampleWeightVector());
				jobCount--;
				return true;
			} else {
				client.getObjectOutputStream().writeObject(null);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public synchronized void resetJobCount(int jobCount) {
		this.jobCount = jobCount;
	}
}
