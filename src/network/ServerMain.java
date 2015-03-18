package network;

public class ServerMain {
	private static int PORT = 8888;
	
	public static void main(String argv[]) throws Exception {
		
		ServerSocketHandler evaluatingServer = new ServerSocketHandler(PORT+1);
		Evaluator evaluator = new Evaluator(evaluatingServer);
		evaluatingServer.setController(evaluator);
		DataRepo evaluatingData = new DataRepo(evaluator);
		evaluator.setData(evaluatingData);

		ServerSocketHandler trainingServer = new ServerSocketHandler(PORT);
		Trainer trainer = new Trainer(trainingServer,evaluator);
		trainingServer.setController(trainer);
		DataRepo trainingData = new DataRepo(trainer);
		trainer.setData(trainingData);
		
		ConnectionHandler trainingConnectionHandler = new ConnectionHandler(trainingServer, trainingData);
		ConnectionHandler evaluatingConnectionHandler = new ConnectionHandler(evaluatingServer, evaluatingData);

		new Thread(trainer).start();
		new Thread(evaluator).start();
		new Thread(trainingConnectionHandler).start();
		new Thread(evaluatingConnectionHandler).start();
	}
}