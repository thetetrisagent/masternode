package tetris;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class InputHandler implements Runnable {
	
	private DataRepo data;
	private Socket clientSocket;
	
	public InputHandler(DataRepo data, Socket clientSocket) {
		this.data = data;
		this.clientSocket = clientSocket;
	}

	@Override
	public void run() {
		try {
			ObjectInputStream inFromClient = new ObjectInputStream(clientSocket.getInputStream());
			while(true) {
				SampleVectorResult sampleVectorResult = (SampleVectorResult) inFromClient.readObject();
//				System.out.println("Received: " + sampleVectorResult.toString());
				data.addResult(sampleVectorResult);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
