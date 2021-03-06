package com.aaej;


/**
 * RLS estimation parameters. Also see ControllerParameters.
 */
public class RLSParameters implements Cloneable {
	public double lambda = 1;
	public int regressorModel = 2; // 0. Coloumb, 1. + Viscous, 2. + Viscous & Offset
	public double[][] theta0 = {{0,0,0},{0,0,0},{0,0,0}}; // Vectors because of the different models. Change if needed!

	public double pam = 100;
	public double pbm = 100;
	public double pcm = 100;

	public Object clone() {		
		try {
			return super.clone();
		} catch (CloneNotSupportedException x) {
			return null;
		}
	}
}
