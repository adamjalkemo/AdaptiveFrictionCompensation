package com.aaej;

public class Regul {

	void setControllerParameters(ControllerParameters ctrlPar) {
		// Todo
		System.out.println("DEBUG: save ctrlPar");
	}

	void setRLSParameters(RLSParameters rlsPar) {
		// Todo
		System.out.println("DEBUG: save rlsPar");
	}

	void resetEstimator() {
		// Todo
		System.out.println("DEBUG: reset Estimator");
	}
	
	void shutDown() {
		// Todo
		System.out.println("DEBUG: graceful shutdown");
	}

	void regulatorActive(boolean active) {
		//Todo
		if (active)
			System.out.println("DEBUG: activating regulator");
		else
			System.out.println("DEBUG: inactivating regulator");		
	}

	ControllerParameters getControllerParameters() {
		return new ControllerParameters();
	}

	RLSParameters getRLSParameters() {
		return new RLSParameters();
	}
	
}