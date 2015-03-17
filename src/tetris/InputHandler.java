package tetris;

import java.io.IOException;
import java.io.ObjectInputStream;

public class InputHandler implements Runnable {
	
	private DataRepo data;
	private ClientHandler client;
	private ServerSocketHandler serverSocketHandler;
	
	public InputHandler(DataRepo data, ClientHandler client, ServerSocketHandler serverSocketHandler) {
		this.data = data;
		this.client = client;
		this.serverSocketHandler = serverSocketHandler;
	}

	@Override
	public void run() {
		try {
			ObjectInputStream inFromClient = client.getObjectInputStream();
			while(true) {
				//Wait for a request
				Object obj = inFromClient.readObject();
				while (!(obj instanceof Integer)) {
					obj = inFromClient.readObject();
				}
				
				//Send a job
				if (serverSocketHandler.distributeWork(client)) {
					SampleVectorResult sampleVectorResult = (SampleVectorResult) inFromClient.readObject();
					data.addResult(sampleVectorResult);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			removeClientSocket();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void removeClientSocket() {
		serverSocketHandler.removeClient(client);
//		data.addResult(new SampleVectorResult(new double[Trainer.NUM_FEATURES], -1));
	}

}
