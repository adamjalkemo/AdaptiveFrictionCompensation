package com.aaej;

public class ControllerParameters implements Cloneable {
	// TODO, these should not be initialized here
	// TODO: Should we separate parameters used to calculate controller paramaters and the actual controller parameters?
	public double[][] qMatrix = new double[][]{{100, 0, 0, 0},{0, 1, 0, 0},{0, 0, 100, 0},{0, 0, 0, 10}};
	public double[][] rMatrix = new double[][]{{100.0}};
	public double ellipseRadius1 = 10;//2;//0.3142; // Not used currently
	public double ellipseRadius2 = 0.5014;	// Not used currently
	public double limit = 1.5;//1.5;
	public double gain = 0.6;// 0.6; 
	public long h = 10;
	public double omega0 = 6.5;//5.6561;

	// TODO is this set when initiallizing? In that case, we could init to zero to show this.
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
