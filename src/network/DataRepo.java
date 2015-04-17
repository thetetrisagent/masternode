package network;

import java.util.ArrayList;

//DataRepo stores the results from the clients and notifies the controller(either evaluator or trainer) so that it can make decisioms
public class DataRepo{

	private ArrayList<SampleVectorResult> resultsBuffer = new ArrayList<SampleVectorResult>();
	private Controller controller;
	
	public DataRepo(Controller evaluator) {
		this.controller = evaluator;
	}
	
	public synchronized void addResult(SampleVectorResult result) {
		resultsBuffer.add(result);
		controller.notifyNewResult();
	}
	
	public synchronized SampleVectorResult getNextResult() {
		SampleVectorResult result = resultsBuffer.remove(0);
		return result;
	}

	public synchronized ArrayList<SampleVectorResult> getResults() {
		ArrayList<SampleVectorResult> results = resultsBuffer;
		this.resultsBuffer = new ArrayList<SampleVectorResult>();
		return results;
	}

	public void print(String string) {
		System.out.println(string);
	}
}
