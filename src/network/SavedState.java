package network;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SavedState implements Serializable {
	
	private double[] mean;
	private double[] var;
	private int iterations;
	private int trainingLoop;
	
	public SavedState(double[] mean, double[] var, int iterations, int trainingLoop) {
		this.mean = mean;
		this.var = var;
		this.iterations = iterations;
		this.trainingLoop = trainingLoop;
	}
	
	public double[] getMean() {
		return this.mean;
	}
	
	public double[] getVar() {
		return this.var;
	}
	
	public int getIterations() {
		return this.iterations;
	}
	
	public int getTrainingLoop() {
		return this.trainingLoop;
	}
}
