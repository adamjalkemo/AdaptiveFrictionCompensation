package com.aaej;

import Jama.*;
import java.util.*;

class TopController {
	private ControllerParameters controllerParameters;
	private double[] L;
	public TopController() {
	}
	public synchronized double calculateOutput(double pendAng, double pendAngVel, double baseAng, double baseAngVel) {
		// x = (theta thetavel phi phivel)
		double[] L = controllerParameters.L;
		double u = - (L[0] * pendAng + L[1] * pendAngVel + L[2] * baseAng + L[3] * baseAngVel);
		return u;
	}
	
	// If states due to for instance Kalman filter is used!
	public synchronized void update() {

	}
	public void setControllerParameters(ControllerParameters controllerParameters) {
		Matrix Q = new Matrix(controllerParameters.qMatrix);
		Matrix R = new Matrix(controllerParameters.rMatrix[0],controllerParameters.rMatrix[0].length);;

		Matrix LMatrix = calculateLMatrix(Q,R);
		synchronized (this) {
			this.controllerParameters = controllerParameters;
			this.L = calculateLMatrix(Q, R).getArray()[0];
		}

	}

	private Matrix calculateLMatrix(Matrix Q, Matrix R) {
		//Matrix A = new Matrix(new double[][]{{1.00156624468639, 0.0100052202706862, 0},{0.313330682422588, 1.00156624468639, 0},{-0.00588699625557634, 0.0000294273019572104, 1}});
		//Matrix B = new Matrix(new double[]{-0.00356262735559447, -0.712711411086724, 1.91246491620224},3);
		Matrix A = new Matrix(new double[][]{{1.00156624468639, 0.0100052202706862, 0, 0},{0.313330682422588, 1.00156624468639, 0, 0},{-0.0000294273019572104, -0.0000000980807680405294, 1, 0.01},{-0.00588699625557634, -0.0000294273019572104, 0, 1}});
                Matrix B = new Matrix(new double[]{-0.00356262735559447, -0.712711411086724, 0.00956230711339704, 1.91246491620224},4);

		return Dlqr.getL(A,B,Q,R);
	}
}
