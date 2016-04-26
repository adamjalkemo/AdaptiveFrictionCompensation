
package com.aaej;

public class RLSParameters implements Cloneable {
	// TODO, these should not be initialized here
	public double lambda = 0.9999;
	public double p0 = 13.0;
	public int regressorModel = 1;
	public double[][] theta0 = {{14.0,0},{15.0,16.0}}; // Vectors because of the different models. Change if needed!

	public double pam = 100;
	public double pbm = 100;
	public double fvGuess = 0;
	public double fcGuess = 0;

	// TODO create kalmanparameters
	// Qk Punish model
	public double[][] Qk = new double[][]{{1000000,0,0,0},{0,1000000,0,0},{0,0,1000000,0},{0,0,0,1000000}};

	// Rk punish measurements
	public double[][] Rk = new double[][]{{1,0,0,0},{0,1,0,0},{0,0,1,0},{0,0,0,1}};
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException x) {
			return null;
		}
	}
}
