package network;

public class ServerMain {
	private static int PORT = 8888;
	
	public static void main(String argv[]) throws Exception {
		
		//initialize evaluator with evaluating server with socket 
		ServerSocketHandler evaluatingServer = new ServerSocketHandler(PORT+1);
		Evaluator evaluator = new Evaluator(evaluatingServer);
		evaluatingServer.setController(evaluator);
		DataRepo evaluatingData = new DataRepo(evaluator);
		evaluator.setData(evaluatingData);

		//initialize trainer with training server with socket
		ServerSocketHandler trainingServer = new ServerSocketHandler(PORT);
		Trainer trainer = new Trainer(trainingServer,evaluator);
		trainingServer.setController(trainer);
		DataRepo trainingData = new DataRepo(trainer);
		trainer.setData(trainingData);
		
		//create connection handlers for each of the servers
		ConnectionHandler trainingConnectionHandler = new ConnectionHandler(trainingServer, trainingData);
		ConnectionHandler evaluatingConnectionHandler = new ConnectionHandler(evaluatingServer, evaluatingData);

		//start training and evaluating
		new Thread(trainer).start();
		new Thread(evaluator).start();
		new Thread(trainingConnectionHandler).start();
		new Thread(evaluatingConnectionHandler).start();
	}
}