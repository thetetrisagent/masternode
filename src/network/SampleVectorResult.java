package network;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SampleVectorResult implements Comparable<SampleVectorResult>, Serializable {
	private double[] weightVector;
	private int results;
	
	public SampleVectorResult(double[] weightVector, int results) {
		this.weightVector = weightVector;
		this.results = results;
	}
	
	public double[] getWeightVector() {
		return this.weightVector;
	}
	
	public int getResults() {
		return this.results;
	}
	
	public int compareTo(SampleVectorResult otherSampleVectorResult) {
		int otherResults = ((SampleVectorResult) otherSampleVectorResult).getResults();
		return this.results - otherResults;
	}
	
	public String toString() {
		return ("(" + this.weightVector[0] + "," + this.weightVector[1] + "):" + this.results);
	}
}
