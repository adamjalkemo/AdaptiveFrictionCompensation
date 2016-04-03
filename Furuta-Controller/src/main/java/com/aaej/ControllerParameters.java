package com.aaej;

public class ControllerParameters implements Cloneable {
	// TODO, these should not be initialized here
	public double[][] qMatrix = new double[][]{{1.0, 2.0},{3.0,4.0}}; 
	public double[][] rMatrix = new double[][]{{5.0}}; 
	public double ellipseRadius1 = 6.0;
	public double ellipseRadius2 = 7.0;
	public double limit = 8.0;
	public double gain = 9.0;
	public long h = 10;
	public double omega0 = 11.0;
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException x) {
			return null;
		}
	}
}
