package network;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SavedState implements Serializable {
	
	private Object mean;
	private Object var;
	private int iterations;
	private int trainingLoop;
	
	public SavedState(Object mean, Object var, int iterations, int trainingLoop) {
		this.mean = mean;
		this.var = var;
		this.iterations = iterations;
		this.trainingLoop = trainingLoop;
	}
	
	public Object getMean() {
		return this.mean;
	}
	
	public Object getVar() {
		return this.var;
	}
	
	public int getIterations() {
		return this.iterations;
	}
	
	public int getTrainingLoop() {
		return this.trainingLoop;
	}
}
