package network;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class Evaluator implements Runnable, Controller {
	
	private static final int NUM_FEATURES = Trainer.NUM_FEATURES;
	private static final String SAVED_DATA_LOCATION = "save_data_evaluator";
	private static final String FINAL_CSV_FILENAME = "results_data.csv";
	private static final int NUM_SAMPLES = 30;

	double[] currMeanVector;
	double[] currVarVector;
	private ArrayList<double[]> jobListMean = new ArrayList<double[]>();
	private ArrayList<double[]> jobListVar = new ArrayList<double[]>();
	private ArrayList<SampleVectorResult> sampleResults = new ArrayList<SampleVectorResult>();
	
	private int evaluationLoop = 0;
	private int counter = 0;
	private double results;
	private volatile boolean isReturned = false;
	private DataRepo data;
	private ServerSocketHandler server;

	public Evaluator(ServerSocketHandler server) {
		this.server = server;
	}
	
	public void setData(DataRepo data) {
		this.data = data;
	}
	
	public void notifyNewResult() {
		counter++;
		if (counter == NUM_SAMPLES) {
			sampleResults = data.getResults();
			counter = 0;
			this.isReturned = true;
		}
	}
	
	public void addNewJob(double[] meanVector, double[] varVector) {
		jobListMean.add(meanVector);
		jobListVar.add(varVector);
	}

	@Override
	public void run() {
		initializeState();

		while (true) {
			
			//Wait for an evaluation job
			System.out.println("Wait for job");
			while(jobListMean.size() == 0) {
				//block
			}
			this.currMeanVector = this.jobListMean.remove(0);
			this.currVarVector = this.jobListVar.remove(0);
			
			//Distribute the work
			server.resetJobCount(NUM_SAMPLES);

			//Wait for data to come back
			while (!this.isReturned) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			this.isReturned = false;
			
			System.out.println("Got all results back!");
//			this.isReturned = false;
			//Aggregate Results
			executeAggregation();

			//write to CSV the last mean/var vectors
			recordFinalState();
				
			//reset the state
			resetTrainingState();
			saveCurrentProgress(new SavedState(currMeanVector,currVarVector,0,evaluationLoop+1));
			evaluationLoop++;	
		}
	}

	private void executeAggregation() {
		this.results = 0;
		for (int i = 0; i < sampleResults.size(); i++) {
			this.results += sampleResults.get(i).getResults();
		}
		this.results /= (double)sampleResults.size();
	}

	private void initializeState() {
		SavedState savedState = loadPreviousProgress();
		if (savedState == null) {
			resetTrainingState();
		} else {
			this.currMeanVector = savedState.getMean();
			this.currVarVector = savedState.getVar();
			this.evaluationLoop = savedState.getTrainingLoop();
		}
	}

	private void resetTrainingState() {
		this.sampleResults.clear();
		this.data.getResults();
	}

	public double[] getSampleWeightVector() {
		return currMeanVector;
	}

	
	private void recordFinalState() {
		try {
			FileWriter writer = new FileWriter(FINAL_CSV_FILENAME,true);
			writer.append(""+this.results);
			for (int i = 0; i < NUM_FEATURES; i++) {
				writer.append("," + currMeanVector[i]);
			}
			for (int i = 0; i < NUM_FEATURES; i++) {
				writer.append("," + currVarVector[i]);
			}
			writer.append("\n");
			 
		    writer.flush();
		    writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	
	private void saveCurrentProgress(SavedState savedState) {
		try {
	         FileOutputStream fos= new FileOutputStream(SAVED_DATA_LOCATION);
	         ObjectOutputStream oos= new ObjectOutputStream(fos);
	         oos.writeObject(savedState);
	         oos.close();
	         fos.close();
		} catch(Exception e) {
		     e.printStackTrace();
		} 
	}
	
	private SavedState loadPreviousProgress() {
		try {
			FileInputStream fis = new FileInputStream(SAVED_DATA_LOCATION);
			ObjectInputStream ois = new ObjectInputStream(fis);
			SavedState savedState = (SavedState) ois.readObject();
			ois.close();
			fis.close();
			return savedState;
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}
