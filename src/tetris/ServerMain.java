package tetris;

public class ServerMain {
	private static int PORT = 8888;
	
	public static void main(String argv[]) throws Exception {
		
		ServerSocketHandler server = new ServerSocketHandler(PORT);
		Trainer trainer = new Trainer(server);
		server.setTrainer(trainer);
		DataRepo data = new DataRepo(trainer);
		trainer.setData(data);
		ConnectionHandler connectionHandler = new ConnectionHandler(server, data);

		new Thread(trainer).start();
		new Thread(connectionHandler).start();
		
		System.out.println("server started at " + PORT);
	}
}