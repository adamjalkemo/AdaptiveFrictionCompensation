package com.aaej;

import Jama.*;

/**
	This class represents the top controller (LQR) which takes over when the pendulum is in a high enough position.
 */
class TopController {
	private ControllerParameters controllerParameters;
	double I = 0;

	public TopController() {
	}

	/**
		The four direct measurements available and a scalar reference corresponding to phi are the arguments.
		The L matrix and lr are calculated when controllerParameters are changed.
	 */
	public synchronized double calculateOutput(double pendAng, double pendAngVel, double baseAng, double baseAngVel, double ref) {
		// x = (theta thetavel phi phivel)
		double[] L = controllerParameters.L;
		double lr = controllerParameters.lr;
		double u = - (L[0] * pendAng + L[1] * pendAngVel + L[2] * baseAng + L[3] * baseAngVel) + lr * ref;
		return u + I;
	}


	public synchronized void updateStates(double baseAng, double ref, double u) {
		I = controllerParameters.L[2] * ((double)controllerParameters.h/1000)/controllerParameters.ti * (ref - baseAng) + I;
		//I = I - 0.01*(CommunicationManager.saturate(u) - u); // Anti-windup
	}

	/**
		When this is called from the GUI the GUI thread handles all calculations below. Once L and lr is
		calculated the controllerParameters are set using a synchronized block.
		Changes in the Q and R matrices during the calculations are impossible since calculations are done
		in the same thread. Since the controller doesn't use the Q and R matrices directly no deep copy is
		necessary for these (2d arrays are not cloned using clone()).
	 */
	public void setControllerParameters(ControllerParameters controllerParameters) {
		Matrix Q = new Matrix(controllerParameters.qMatrix);
		Matrix R = new Matrix(controllerParameters.rMatrix[0],controllerParameters.rMatrix[0].length);

		// Discretizes the system based on current sampling time h. A and B are stored in Discretizer.
		Matrix phiAndGamma[] = Discretizer.c2d(controllerParameters.h);

		// Calculates state feedback matrix L
		controllerParameters.L = Discretizer.getL(phiAndGamma[0], phiAndGamma[1], Q, R).getArray()[0];

		// Feedforward C (should only be for phi);
		Matrix C = new Matrix(1,4);
		C.set(0,2,1);

		// Calculated the feed forward for unity gain in stationary
		controllerParameters.lr = Discretizer.getlr(phiAndGamma[0], phiAndGamma[1], C, new Matrix(controllerParameters.L,1));

		synchronized (this) {
			this.controllerParameters = controllerParameters;
		}

		resetIntegrator();
	}

	public synchronized void resetIntegrator() {
		this.I = 0;
	}
}
