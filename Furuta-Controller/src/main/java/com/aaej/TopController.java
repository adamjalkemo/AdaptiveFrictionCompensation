package com.aaej;

import Jama.*;
import java.util.*;

class TopController {
	private ControllerParameters controllerParameters;
	public TopController() {
	}
	public int calculateOutput() {
		return 0;
	}
	public void update() {

	}
	public synchronized void setControllerParameters(ControllerParameters controllerParameters) {
		this.controllerParameters = controllerParameters;

		// TODO: THESE SHOULD BE READ FROM CONTROLLER PARAMETERS
		Matrix Q = new Matrix(controllerParameters.qMatrix); 
		Matrix R = new Matrix(controllerParameters.rMatrix[0],controllerParameters.rMatrix[0].length);;

		Matrix LMatrix = calculateLMatrix(Q,R);
		controllerParameters.L = calculateLMatrix(Q, R).getArray()[0];
	}

	public Matrix calculateLMatrix(Matrix Q, Matrix R) {
		Matrix A = new Matrix(new double[][]{{1.00156624468639, 0.0100052202706862, 0},{0.313330682422588, 1.00156624468639, 0},{-0.00588699625557634, 0.0000294273019572104, 1}});
		Matrix B = new Matrix(new double[]{-0.00356262735559447, -0.712711411086724, 1.91246491620224},3);

		return Dlqr.getL(A,B,Q,R);
	}
}
