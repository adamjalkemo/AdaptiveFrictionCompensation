package com.aaej;

import Jama.*;
import java.util.*;

/*
 * This class contains methods for generating a sampled system and calculating the state feedback provided
 * the system and Q and R matrices.
 */
class Discretizer {

	/*
	 * Calculates the State feedback matrix L.
	 */
	public static Matrix getL(Matrix Phi, Matrix Gamma, Matrix Q, Matrix R) {
		Matrix P = Q.copy();

		// Solve P using Phi, Gamma, Q and R.
		// Equations are long and have been splitted into t1, t2 and t3.
		// P = A'*P*A-(A'*P*B)/(R+B'*P*B)*(B'*P*A)+Q;

		for (int i = 0; i < 1000; i++) {
			Matrix t1 = Phi.transpose().times(P).times(Phi);

			Matrix t2 = Phi.transpose().times(P).times(Gamma).times((R.plus(Gamma.transpose().times(P).times(Gamma))).inverse());
			Matrix t3 = t2.times(Gamma.transpose().times(P).times(Phi));

			P = t1.minus(t3).plus(Q);
		}
		
		//L = (R+B'*P*B)\(B'*P*A)
		Matrix L = R.plus(Gamma.transpose().times(P).times(Gamma)).inverse().times(Gamma.transpose().times(P).times(Phi));
		return L;
	}

	/*
	 * Calculates the feed forward gain lr (for H(1) = 1).
	 */
	public static double getlr(Matrix Phi, Matrix Gamma, Matrix C, Matrix L) {

		// lr = 1 / (C*inv(I - A + B * L) * B);
		int m = Phi.getRowDimension();
		Matrix tf = C.times((Matrix.identity(m,m).minus(Phi).plus(Gamma.times(L))).inverse()).times(Gamma);

		return 1 / tf.get(0,0);
	}

	// Returns Matrix vector [Matrix Ad, Matrix Bd]
	/*
 	* C2d is used for generating a sampled representation of the system.
 	* Phi is calculated using tustin. Gamma is calculated using B*h.
 	*/
	public static Matrix[] c2d(long h) {
		Matrix Ac = new Matrix(new double[][]{{0, 1, 0, 0},{31.3167200666837, 0, 0, 0},{0, 0, 0, 1,},{-0.588392468761969, 0, 0, 0}});
		Matrix Bc = new Matrix(new double[]{0, -71.2339550559284, 0 ,191.245792952122},4);
		int m = Ac.getRowDimension();

		// (eye(size(Ac))+1/2*Ac*h)*inv(eye(size(Ac))-1/2*Ac*h)
		// h converted to seconds below!
		Matrix Ad = Matrix.identity(m,m).plus(Ac.times(0.5*h/1000)).times((Matrix.identity(m,m).minus(Ac.times(0.5*h/1000))).inverse());
		Matrix Bd = Bc.times((double) h / 1000);

		return new Matrix[]{Ad, Bd};
	}

	// I'm leaving this here for manual test purposes
	/*public static void main(String[] args) { 
		
		Matrix A = new Matrix(new double[][]{{-2, 0},{0, -1}});
		
		Matrix B = new Matrix(new double[]{1, 0.1},2);

		Matrix Q = new Matrix(new double[][]{{1,0},{0,1}});
		Matrix R = new Matrix(new double[]{1},1);

		System.out.println(Arrays.deepToString(Discretizer.getL(A,B,Q,R).getArray()));
		
		Matrix Ac = new Matrix(new double[][]{{0, 1, 0, 0},{31.3167200666837, 0, 0, 0},{0, 0, 0, 1,},{-0.588392468761969, 0, 0, 0}});
		Matrix Bc = new Matrix(new double[]{0, -71.2339550559284, 0 ,191.245792952122},4);
		c2d(Ac,Bc,10);
		

	}*/

}
	