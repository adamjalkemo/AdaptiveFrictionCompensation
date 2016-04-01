
package com.aaej;

public class RLSParameters implements Cloneable {
	// TODO, these should not be initialized here
	public double lambda = 12.0;
	public double p0 = 13.0;
	public int regressorModel = 1;
	public double[][] theta0 = {{14.0,0},{15.0,16.0}}; // Vectors because of the different models. Change if needed!
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException x) {
			return null;
		}
	}
}
