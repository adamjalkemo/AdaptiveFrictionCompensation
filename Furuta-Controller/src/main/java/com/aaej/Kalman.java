package com.aaej;

import Jama.*;
import java.util.*;


class Kalman {

	Matrix A, B, C;
	Matrix Qk, Rk;
	Matrix P, X;

	int counter = 0; // Adam test

	public Kalman(Matrix A, Matrix B, Matrix Qk, Matrix Rk) {
		
		// These should be sent to kalman
		/*A = new Matrix(new double[][]{{1.00156624468639, 0.0100052202706862, 0, 0},{0.313330682422588, 1.00156624468639, 0, 0},{-0.0000294273019572104, -0.0000000980807680405294, 1, 0.01},{-0.00588699625557634, -0.0000294273019572104, 0, 1}});
		B = new Matrix(new double[]{-0.00356262735559447, -0.712711411086724, 0.00956230711339704, 1.91246491620224},4);
		Qk = Matrix.identity(1,1);
		Rk = Matrix.identity(4,4);		*/

		this.A = A;
		this.B = B;
		this.Qk = Qk;
		this.Rk = Rk;

		C = Matrix.identity(4,4);
		P = Matrix.identity(4,4).times(1000.0);

		X = new Matrix(new double[] {0,0,0,0},4);


	}

	// u control signal, y measurement vector
	public void updateStates(double u, double[] y) {
	    //System.out.println(Arrays.deepToString(Ad.getArray()));
	    //System.out.println(Arrays.deepToString(Bd.getArray()));
		Matrix Z = Rk.plus(C.times(P).times(C.transpose()));
		Matrix K = (A.times(P).times(C.transpose())).times((Rk.plus(C.times(P).times(C.transpose()))).inverse());
		P = A.times(P).times(A.transpose()).plus(Qk).minus(K.times(Z).times(K.transpose()));
		X = A.times(X).plus(B.times(u)).plus(K.times(new Matrix(y,4).minus(C.times(X))));

		while (counter > 10) {
			System.out.println(Arrays.deepToString(P.getArray()));
			counter = 0;	
		}
	}

	public Matrix calculateYHat() {
		return C.times(X);
	}

	public void setRLSParameters(RLSParameters rlsParameters) {
		synchronized (this) {
			this.Qk = new Matrix(rlsParameters.Qk);
			this.Rk = new Matrix(rlsParameters.Rk);
		}
	}
}
	