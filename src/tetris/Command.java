package tetris;

import java.io.Serializable;


@SuppressWarnings("serial")
public class Command implements Serializable {
	private double[] newSampleWeight;
	
	public Command(double[] newSampleWeight) {
		this.newSampleWeight = newSampleWeight;
	}
	
	public SampleVectorResult execute() {
		State s = new State();
		PlayerSkeleton p = new PlayerSkeleton(newSampleWeight);
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
		}
		return new SampleVectorResult(newSampleWeight, s.getRowsCleared());
	}
}
