package network;


public interface Controller {

	void notifyNewResult();
	double[] getSampleWeightVector();
	void setData(DataRepo data);
	
}
