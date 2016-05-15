
package com.aaej;

public class RLSParameters implements Cloneable {
	// TODO, these should not be initialized here
	public double lambda = 1;
	public double p0 = 13.0;
	public int regressorModel = 1;
	public double[][] theta0 = {{14.0,0},{15.0,16.0}}; // Vectors because of the different models. Change if needed!

	public double pam = 100;
	public double pbm = 100;
	public double pcm = 100;
	public double fvGuess = 0;
	public double fcGuess = 0;
	public double foGuess = 0;

	public Object clone() {		
		try {
			return super.clone();
		} catch (CloneNotSupportedException x) {
			return null;
		}
	}
}
