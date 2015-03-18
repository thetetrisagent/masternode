package network;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Trainer implements Runnable, Controller{
	
	static final int NUM_FEATURES = 8;
	private static final int MAX_ITERATIONS = 80;
	static final int NUM_SAMPLES = 100;
	static final double SELECTION_RATIO = 0.1;
	static final double NOISE_FACTOR = 4;
	private static final String SAVED_DATA_LOCATION = "save_data_trainer";

	Random r = new Random();
	double[] currMeanVector;
	double[] currVarVector;
	private int iterations;
	private ArrayList<SampleVectorResult> sampleResults = new ArrayList<SampleVectorResult>();
	
	private int trainingLoop = 0;
	private int counter = 0;
	private volatile boolean isReturned = false;
	private DataRepo data;
	private ServerSocketHandler server;
	private Evaluator evaluator;

	public Trainer(ServerSocketHandler server, Evaluator evaluator) {
		this.server = server;
		this.evaluator = evaluator;
	}
	
	public void setData(DataRepo data) {
		this.data = data;
	}
	
	public void notifyNewResult() {
		counter++;
		if (counter == NUM_SAMPLES) {
			sampleResults = data.getResults();
			counter = 0;
			isReturned = true;
		}
	}

	@Override
	public void run() {
		initializeState();

		while (true) {
			while (iterations < MAX_ITERATIONS) {
				//Generate Samples
	//			ArrayList<double[]> commands = new ArrayList<double[]>();
	//			for (int i = 0; i < NUM_SAMPLES; i++) {
	//				//Generate new weight vector
	//				double[] newSampleWeight = new double[NUM_FEATURES];
	//				for (int j = 0; j < NUM_FEATURES; j++) {
	//					newSampleWeight[j] = (r.nextGaussian()*Math.sqrt(currVarVector[j]))+currMeanVector[j];	
	//				}
	//				commands.add(newSampleWeight);
	//			}
	//			
				//Distribute the work
	//			server.setCommandList(commands);
				server.resetJobCount(NUM_SAMPLES);
	
				//Wait for data to come back
				while (!isReturned) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				isReturned = false;
				
				//Aggregate Results
				executeAggregation();
				
				iterations++;
			}
			
			//send vector for evaluation and recording
			evaluator.addNewJob(currMeanVector, currVarVector);
			
			//reset the state
			resetTrainingState();
			saveCurrentProgress(new SavedState(currMeanVector,currVarVector,iterations,trainingLoop+1));
			trainingLoop++;
			
		}
	}

	private void executeAggregation() {
		//Evaluate And Recalculate Mean & Variance
		Collections.sort(sampleResults);
		double numSelected = NUM_SAMPLES*SELECTION_RATIO;
		for (int i = 0; i < NUM_FEATURES; i++) {
			double newMean = 0;
			double newVariance = 0;
			//Calculate New Mean
			for (int j = 0; j < numSelected; j++) {
				int curr = NUM_SAMPLES-1-j;
				newMean += sampleResults.get(curr).getWeightVector()[i];
			}
			newMean /= numSelected;
			currMeanVector[i] = newMean;
			
			//Calculate New Variance
			for (int j = 0; j < numSelected; j++) {
				int curr = NUM_SAMPLES-1-j;
				newVariance += Math.pow(newMean - sampleResults.get(curr).getWeightVector()[i],2);
			}
			newVariance /= numSelected;
			currVarVector[i] = newVariance + NOISE_FACTOR;
		}
		
		//Print Results
		System.out.print("Current Means are: ");
		for (int i = 0; i < NUM_FEATURES; i++) {
			System.out.print(currMeanVector[i] + ", ");
		}
		System.out.println();
		System.out.print("Current Vars are: ");
		for (int i = 0; i < NUM_FEATURES; i++) {
			System.out.print(currVarVector[i] + ", ");
		}
		System.out.println();
		System.out.println(iterations + ": " + sampleResults.get(sampleResults.size()-(int)numSelected).getResults());
		System.out.println("Training Loop: " + this.trainingLoop);
		saveCurrentProgress(new SavedState(currMeanVector,currVarVector,iterations+1,trainingLoop));
	}

	private void initializeState() {
		SavedState savedState = loadPreviousProgress();
		if (savedState == null) {
			resetTrainingState();
		} else {
			this.currMeanVector = savedState.getMean();
			this.currVarVector = savedState.getVar();
			this.iterations = savedState.getIterations();
			this.trainingLoop = savedState.getTrainingLoop();
			//Print Results
			System.out.print("Current Means are: ");
			for (int i = 0; i < NUM_FEATURES; i++) {
				System.out.print(this.currMeanVector[i] + ", ");
			}
			System.out.println();
			System.out.print("Current Vars are: ");
			for (int i = 0; i < NUM_FEATURES; i++) {
				System.out.print(this.currVarVector[i] + ", ");
			}
			System.out.println();
			System.out.println("Iteration: " + this.iterations);
			System.out.println("Training Loop: " + this.trainingLoop);
		}
	}

	private void resetTrainingState() {
		this.currMeanVector = new double[NUM_FEATURES];
		this.currVarVector = new double[NUM_FEATURES];
		for (int i = 0; i < NUM_FEATURES; i++) {
			currVarVector[i] = 100;	
		}
		this.iterations = 0;
		this.sampleResults.clear();
		this.data.getResults();
	}

	public double[] getSampleWeightVector() {
		//Generate new weight vector
		double[] newSampleWeight = new double[NUM_FEATURES];
		for (int j = 0; j < NUM_FEATURES; j++) {
			newSampleWeight[j] = (r.nextGaussian()*Math.sqrt(currVarVector[j]))+currMeanVector[j];	
		}
		return newSampleWeight;
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
