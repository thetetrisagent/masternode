package tetris;

import java.util.ArrayList;

public class DataRepo{

	private ArrayList<SampleVectorResult> resultsBuffer = new ArrayList<SampleVectorResult>();
	private Trainer trainer;
	
	public DataRepo(Trainer trainer) {
		this.trainer = trainer;
	}
	
	public synchronized void addResult(SampleVectorResult result) {
		resultsBuffer.add(result);
		trainer.notifyNewResult();
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
