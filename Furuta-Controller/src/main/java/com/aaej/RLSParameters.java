
package com.aaej;

public class RLSParameters implements Cloneable {
	// TODO, these should not be initialized here
	public double lambda = 1;
	public double p0 = 13.0;
	public int regressorModel = 1;
	public double[][] theta0 = {{14.0,0},{15.0,16.0}}; // Vectors because of the different models. Change if needed!

	public double pam = 100;
	public double pbm = 100;
	public double fvGuess = 0;
	public double fcGuess = 0;

	public double deadzoneBaseAngVel = 0;
	public double deadzonePendAngVel = 0;

	// TODO create kalmanparameters
	// Qk Punish model
	public double[][] qKalman = new double[][]{{1000,0,0,0},{0,1000,0,0},{0,0,1000,0},{0,0,0,1000}};

	// Rk punish measurements
	public double[][] rKalman = new double[][]{{0.143238315472186,0.0488345203934119,-2.12776741934771,-0.2956644174096},{0.0488345203934119,16.2664968543817,0.369202394734228,-16.5511953536469},{-2.12776741934771,0.369202394734228,52.3057656866251,-2.18556449574024},{-0.2956644174096,-16.5511953536469,-2.18556449574024,108.684243879183}};
	
	public Object clone() {		
		try {
			return super.clone();
		} catch (CloneNotSupportedException x) {
			return null;
		}
	}
}
