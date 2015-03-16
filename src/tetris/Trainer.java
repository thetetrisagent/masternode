package tetris;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Trainer implements Runnable{
	
	private static final int NUM_FEATURES = 6;
	private static final int MAX_ITERATIONS = 80;
	private static final int NUM_SAMPLES = 100;
	private static final double SELECTION_RATIO = 0.1;
	private static final double NOISE_FACTOR = 4;
	
	private int counter = 0;
	private boolean isReturned = false;
	private DataRepo data;
	private ArrayList<SampleVectorResult> sampleResults = new ArrayList<SampleVectorResult>();
	private ServerSocketHandler server;

	public Trainer(ServerSocketHandler server) {
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
			isReturned = true;
		}
	}

	private void executeAggregation(int iterations, double[] currMeanVector, double[] currVarVector) {
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
	}

	@Override
	public void run() {
		int iterations = 0;
		Random r = new Random();
		double[] currMeanVector = new double[NUM_FEATURES];
		double[] currVarVector = new double[NUM_FEATURES];
		for (int i = 0; i < NUM_FEATURES; i++) {
			currVarVector[i] = 100;	
		}

		while (iterations < MAX_ITERATIONS) {
			//Generate Samples
			ArrayList<Command> commands = new ArrayList<Command>();
			for (int i = 0; i < NUM_SAMPLES; i++) {
				//Generate new weight vector
				double[] newSampleWeight = new double[NUM_FEATURES];
				for (int j = 0; j < NUM_FEATURES; j++) {
					newSampleWeight[j] = (r.nextGaussian()*Math.sqrt(currVarVector[j]))+currMeanVector[j];	
				}
				commands.add(new Command(newSampleWeight));
			}
			
			//Distribute the work
			server.distributeWork(commands);

			//Wait for data to come back
			while (!isReturned) {
				//block
			}
			isReturned = false;
			
			//Aggregate Results
			executeAggregation(iterations, currMeanVector, currVarVector);
			
			iterations++;
		}
	}
}
