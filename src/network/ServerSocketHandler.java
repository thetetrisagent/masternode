package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class ServerSocketHandler{
	private ServerSocket server;
	private ArrayList<ClientHandler> clients;
//	private ArrayList<double[]> vectors;
	private int jobCount = 0;
	private Trainer trainer;
	
	public ServerSocketHandler(int port) {
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
	
	public void setTrainer(Trainer trainer) {
		this.trainer = trainer;
	}
	
	public void addClient(ClientHandler client) {
		clients.add(client);
	}
	
	public synchronized void removeClient(ClientHandler client) {
		clients.remove(clients.indexOf(client));
		jobCount++;
	}
	
	public synchronized boolean distributeWork(ClientHandler client) {
//		try {
//			if (vectors.size() > 0) {
//				client.getObjectOutputStream().writeObject(vectors.remove(0));
//				return true;
//			} else {
//				client.getObjectOutputStream().writeObject(null);
//			}
		try {
			if (jobCount > 0) {
				client.getObjectOutputStream().writeObject(trainer.generateSampleWeightVector());
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

//	public synchronized void setCommandList(ArrayList<double[]> vectors) {
//		this.vectors = vectors;
//	}

	public synchronized void resetJobCount(int jobCount) {
		this.jobCount = jobCount; 
	}
}
