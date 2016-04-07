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



class Dlqr {

	public Dlqr() {
		
	}

	public static Matrix getL(Matrix A, Matrix B, Matrix Q, Matrix R) {
		Matrix P = Q.copy();

		for (int i = 0; i < 100; i++) {
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

	// I'm leaving this here for manual test purposes
	public static void main(String[] args) { 
		Matrix A = new Matrix(new double[][]{{-2, 0},{0, -1}});
		
		Matrix B = new Matrix(new double[]{1, 0.1},2);

		Matrix Q = new Matrix(new double[][]{{1,0},{0,1}});
		Matrix R = new Matrix(new double[]{1},1);

		System.out.println(Arrays.deepToString(Dlqr.getL(A,B,Q,R).getArray()));
	}

}
