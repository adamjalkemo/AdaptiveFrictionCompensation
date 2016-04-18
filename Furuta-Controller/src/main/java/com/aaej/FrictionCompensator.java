package com.aaej;

import Jama.Matrix;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

public class FrictionCompensator {
    private RLSParameters rlsParameters;
    private Matrix P_old;
    private Matrix theta_old;
    private Matrix phi;
    private Matrix A;
    private Matrix B;
    private double pendAng_old = 0;
    private double pendAngVel_old = 0;
    private double control_old = 0;

    public FrictionCompensator() {
    }

    public synchronized double[] rls(double pendAng, double pendAngVel, double control) {

        P_old = P_old.minus((P_old.times(phi).times(phi.transpose()).times(P_old))
                .times(1.0/(1.0+(phi.transpose().times(P_old).times(phi)).get(0,0))));
        double VL = abs(pendAngVel + A.get(3,2)*pendAng_old - B.get(0,3)*control_old-pendAngVel_old)/B.get(0,3);
        double epsilon = VL - (phi.transpose().times(theta_old)).get(0,0);
        theta_old = theta_old.plus(P_old.times(phi).times(epsilon));
        phi.set(0, 0, pendAngVel);
        phi.set(1, 0, signum(pendAngVel));
        pendAng_old = pendAng;
        control_old = control;
        return(theta_old.getColumnPackedCopy());

    }

    public synchronized void setRLSParameters(RLSParameters newRLSParameters) {
        this.rlsParameters = newRLSParameters;
        P_old = new Matrix(new double[][] {{rlsParameters.pam,0},{0,rlsParameters.pbm}},2,2);
        theta_old = new Matrix(new double[][] {{rlsParameters.fvGuess},{rlsParameters.fcGuess}},2,1);
        phi = new Matrix(1,2);

    }

}
