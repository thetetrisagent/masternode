package network;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Trainer implements Runnable, Controller{
	
	static final int NUM_FEATURES = 8;
	static final int MAX_ITERATIONS = 80;
	static final int NUM_SAMPLES = 100;
	static final double SELECTION_RATIO = 0.1;
	static final double NOISE_FACTOR = 4;
	private static final String SAVED_DATA_LOCATION = "save_data_trainer";
	private static final double[] CONSTANT_NOISE = new double[] {1.0,0.0};
	private static final double[] DECREASING_NOISE = new double[] {0.0,1.0};
	private static final String ITERATION_TIMES_CSV_FILENAME = "iterationTimes_%d.csv";
	private static final String TRAININGLOOP_TIMES_CSV_FILENAME = "trainingLoopTimes.csv";
	

	Random r = new Random();
	double[] currMeanVector;
	double[] currVarVector;
	private int iterations;
	private double[] noiseController;
	private ArrayList<SampleVectorResult> sampleResults = new ArrayList<SampleVectorResult>();
	private StopWatch trainingLoopWatch = new StopWatch();
	private StopWatch iterationWatch = new StopWatch();
	
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
			trainingLoopWatch.reset();
			setNoiseStatus();
			
			while (iterations < MAX_ITERATIONS) {
				iterationWatch.reset();
				
				//Distribute the work
				server.resetJobCount(NUM_SAMPLES);
	
				//Wait for data to come back
				while (!isReturned) {
					block(500);
				}
				isReturned = false;
				
				//Aggregate Results
				executeAggregation();
				
				//send vector for evaluation and recording
				evaluator.addNewJob(currMeanVector, currVarVector);
				
				iterations++;
			}

			long trainingLoopTime = trainingLoopWatch.getElapsedTime();
			recordTiming(TRAININGLOOP_TIMES_CSV_FILENAME,0,trainingLoopTime);
			
			//reset the state
			resetTrainingState();
			saveCurrentProgress(new SavedState(currMeanVector,currVarVector,iterations,trainingLoop+1));
			trainingLoop++;
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
		//Evaluate And Recalculate Mean & Variance
		Collections.sort(sampleResults);
		double numSelected = NUM_SAMPLES*SELECTION_RATIO;
		for (int i = 0; i < NUM_FEATURES; i++) {
			double newMean = 0;
			double newVariance = 0;
			
			//Calculate New Mean
			double totalSumOfWeights = numSelected*(1+numSelected)/2; //arithmetic progression
			for (int j = 0; j < numSelected; j++) {
				int curr = NUM_SAMPLES-1-j;
				newMean += sampleResults.get(curr).getWeightVector()[i] * ((j+1)/totalSumOfWeights);
			}
			currMeanVector[i] = newMean;
			
			//Calculate New Variance
			for (int j = 0; j < numSelected; j++) {
				int curr = NUM_SAMPLES-1-j;
				newVariance += Math.pow(newMean - sampleResults.get(curr).getWeightVector()[i],2);
			}
			newVariance /= numSelected;
			currVarVector[i] = newVariance + noiseController[0]*NOISE_FACTOR + noiseController[1]*getDecreasingNoise();
		}

		
		long iterationTime = iterationWatch.getElapsedTime();
		long sumRows = 0;
		for (int i = 0; i < sampleResults.size(); i++) {
			sumRows += sampleResults.get(i).getResults();
		}
		recordTiming(String.format(ITERATION_TIMES_CSV_FILENAME,this.trainingLoop),sumRows,iterationTime);
		
		printResults();
		System.out.println("Iteration " + iterations + " results: " + sampleResults.get(sampleResults.size()-(int)numSelected).getResults());
		System.out.println("Noise is: " + (noiseController[0]*NOISE_FACTOR + noiseController[1]*getDecreasingNoise()));
		saveCurrentProgress(new SavedState(currMeanVector,currVarVector,iterations+1,trainingLoop));
	}

	private void recordTiming(String filePath, long results, long iterationTime) {
		try {
			FileWriter writer = new FileWriter(filePath,true);
			writer.append(""+ this.iterations + "," + results + "," + iterationTime);
			writer.append("\n");
		    writer.flush();
		    writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	
	private double getDecreasingNoise() {
		return Math.max(5-(iterations/10.0), 0);
	}

	private void initializeState() {
		SavedState savedState = loadPreviousProgress();
		if (savedState == null) {
			resetTrainingState();
		} else {
			this.currMeanVector = (double[]) savedState.getMean();
			this.currVarVector = (double[]) savedState.getVar();
			this.iterations = savedState.getIterations();
			this.trainingLoop = savedState.getTrainingLoop();
			printResults();
		}
	}

	private void setNoiseStatus() {
		//Set noise status
		if (this.trainingLoop%2 == 0) {
			//Constant Noise
			noiseController = CONSTANT_NOISE;
		} else {
			//Decreasing Noise
			noiseController = DECREASING_NOISE;
		}
	}

	private void printResults() {
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
