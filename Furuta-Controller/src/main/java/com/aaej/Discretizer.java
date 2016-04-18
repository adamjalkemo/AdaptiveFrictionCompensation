package com.aaej;

import Jama.*;
import java.util.*;

/* How to iterate the dlqr sol
A = [-2 0; 0 -1];
B = [1; 0.1];

Q = eye(2)
R = 1;

P = Q;
for i = 0:1000
P = A'*P*A-(A'*P*B)/(R+B'*P*B)*(B'*P*A)+Q;
end

F = (R+B'*P*B)\(B'*P*A)

% Compare with
dlqr(A,B,Q,R)

*/



class Discretizer {

	public Discretizer() {
		
	}

	public static Matrix getL(Matrix A, Matrix B, Matrix Q, Matrix R) {
		Matrix P = Q.copy();

		for (int i = 0; i < 1000; i++) {
			// P = A'*P*A-(A'*P*B)/(R+B'*P*B)*(B'*P*A)+Q;
			Matrix t1 = A.transpose().times(P).times(A);
			
			Matrix t2 = A.transpose().times(P).times(B).times((R.plus(B.transpose().times(P).times(B))).inverse());
			Matrix t3 = t2.times(B.transpose().times(P).times(A));

			P = t1.minus(t3).plus(Q);
		}
		
		//L = (R+B'*P*B)\(B'*P*A)
		Matrix L = R.plus(B.transpose().times(P).times(B)).inverse().times(B.transpose().times(P).times(A));
		return L;
	}

	// Returns Matrix vector [Matrix Ad, Matrix Bd]
	public static Matrix[] c2d(long h) {
		/*% Approximations for Ad = expm(Ac*h)
		    %ZOH (if expm available)
		    %expm(Ac*h)

		    %eye(size(Ac)) + Ac*h (Worst)

		    %inv(eye(size(Ac)) - Ac*h) (Medium good)

		    %Bilinear transform (Best wo. exmp function)
		    (eye(size(Ac))+1/2*Ac*h)*inv(eye(size(Ac))-1/2*Ac*h)
		    
		% Approximations for Bd
		    % Medium (works)
		    h * Bc

		    % Probably better, but wont work due to Ac not invertible.
		    %inv(Ac)*(Ad - eye(size(Ad)))*Bc*/
			Matrix Ac = new Matrix(new double[][]{{0, 1, 0, 0},{31.3167200666837, 0, 0, 0},{0, 0, 0, 1,},{-0.588392468761969, 0, 0, 0}});
			Matrix Bc = new Matrix(new double[]{0, -71.2339550559284, 0 ,191.245792952122},4);
		    int m = Ac.getRowDimension();
		    // h converted to seconds below!
		    Matrix Ad = Matrix.identity(m,m).plus(Ac.times(0.5*h/1000)).times((Matrix.identity(m,m).minus(Ac.times(0.5*h/1000))).inverse());
		    Matrix Bd = Bc.times((double) h / 1000);
		    //System.out.println(Arrays.deepToString(Ad.getArray()));
		    //System.out.println(Arrays.deepToString(Bd.getArray()));
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
	