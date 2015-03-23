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
	private static final int MAX_ITERATIONS = Trainer.MAX_ITERATIONS;
	private static final String SAVED_DATA_LOCATION = "save_data_evaluator";
	private static final String FINAL_CSV_FILENAME = "results_data_%d.csv";
	private static final int NUM_SAMPLES = 30;

	double[] currMeanVector;
	double[] currVarVector;
	private ArrayList<double[]> jobListMean = new ArrayList<double[]>();
	private ArrayList<double[]> jobListVar = new ArrayList<double[]>();
	private ArrayList<SampleVectorResult> sampleResults = new ArrayList<SampleVectorResult>();
	
	private int evaluationLoop = 0;
	private int iterations = 0;
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
			while (iterations < MAX_ITERATIONS) {
				
				//Wait for an evaluation job
				System.out.println("Wait for job");
				while(jobListMean.size() == 0) {
					block(500);
				}
				this.currMeanVector = this.jobListMean.get(0);
				this.currVarVector = this.jobListVar.get(0);
				
				//Distribute the work
				System.out.println("give jobs!");
				server.resetJobCount(NUM_SAMPLES);
	
				//Wait for data to come back
				while (!this.isReturned) {
					block(500);
				}
				this.isReturned = false;
				
				//Aggregate Results
				executeAggregation();
	
				//write to CSV the last mean/var vectors
				recordFinalState();
				this.jobListMean.remove(0); //we only remove here in case not written to CSV, so we can resume.
				this.jobListVar.remove(0);
				
				this.iterations++;
			}
			
			//reset the state
			resetTrainingState();
			saveCurrentProgress(new SavedState(jobListMean,jobListVar,iterations,evaluationLoop+1));
			evaluationLoop++;
		}
	}

	private void block(int timing) {
		try {
			Thread.sleep(timing);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void executeAggregation() {
		this.results = 0;
		for (int i = 0; i < sampleResults.size(); i++) {
			this.results += sampleResults.get(i).getResults();
		}
		this.results /= (double)sampleResults.size();
		saveCurrentProgress(new SavedState(jobListMean,jobListVar,iterations+1,evaluationLoop));
	}

	@SuppressWarnings("unchecked")
	private void initializeState() {
		SavedState savedState = loadPreviousProgress();
		if (savedState == null) {
			resetTrainingState();
		} else {
			this.jobListMean = (ArrayList<double[]>) savedState.getMean();
			this.jobListVar = (ArrayList<double[]>) savedState.getVar();
			this.evaluationLoop = savedState.getTrainingLoop();
			this.iterations = savedState.getIterations();
		}
	}

	private void resetTrainingState() {
		this.sampleResults.clear();
		this.data.getResults();
		this.iterations = 0;
	}

	public double[] getSampleWeightVector() {
		return currMeanVector;
	}

	
	private void recordFinalState() {
		try {
			FileWriter writer = new FileWriter(String.format(FINAL_CSV_FILENAME,this.evaluationLoop),true);
			writer.append(""+ this.iterations + "," + this.results);
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
