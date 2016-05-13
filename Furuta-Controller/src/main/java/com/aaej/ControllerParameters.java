package com.aaej;

/*
 * Controller parameters. Also see RLSParameters where we have the parameters for the RLS estimation.
 */
public class ControllerParameters implements Cloneable {
	// General parameters
	public long h = 10;

	// Swing up controller parameters
	public double ellipseRadius1 = 10;
	public double ellipseRadius2 = 0.5014;
	public double limit = 1.5;
	public double gain = 0.6;
	public double omega0 = 6.5;

	// Top controller parameters
	public double[][] qMatrix = new double[][]{{100, 0, 0, 0},{0, 1, 0, 0},{0, 0, 100, 0},{0, 0, 0, 10}};
	public double[][] rMatrix = new double[][]{{100.0}};

	// These are calculated when setParameters is called.
	public double[] L = new double[]{0, 0, 0, 0};
	public double lr = 0;

	// TODO add more deadzones
	public double deadzoneBaseAngVel = 0;
	public double deadzonePendAngVel = 0;

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException x) {
			return null;
		}
	}
}
