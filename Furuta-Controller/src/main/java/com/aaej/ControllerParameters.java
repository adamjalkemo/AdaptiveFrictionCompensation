package com.aaej;

public class ControllerParameters implements Cloneable {
	// TODO, these should not be initialized here
	// TODO: Should we separate parameters used to calculate controller paramaters and the actual controller parameters?
	public double[][] qMatrix = new double[][]{{100, 0, 0, 0},{0, 1, 0, 0},{0, 0, 10, 0},{0, 0, 0, 10}};
	public double[][] rMatrix = new double[][]{{100.0}};
	public double ellipseRadius1 = 6.0;
	public double ellipseRadius2 = 7.0;
	public double limit = 8.0;
	public double gain = 9.0;
	public long h = 10;
	public double omega0 = 11.0;
	public double[] L = new double[]{-8.83491466316015, -1.58036477625261, -0.220472707928114, -0.304872980032455};
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException x) {
			return null;
		}
	}
}
