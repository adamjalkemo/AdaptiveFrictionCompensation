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
    private double baseAngVel_old = 0;
    private double control_old = 0;

    public FrictionCompensator() {
        phi = new Matrix(2,1);
        P_old = new Matrix(2,2);
        theta_old = new Matrix(2,1);
    }

    public synchronized double[] rls(double baseAngVel) {

        P_old = P_old.minus((P_old.times(phi).times(phi.transpose()).times(P_old))
                .times(1.0/(1.0+(phi.transpose().times(P_old).times(phi)).get(0,0))));
        double VL = abs(baseAngVel + A.get(3,2)*pendAng_old - B.get(0,3)*control_old-baseAngVel_old)/B.get(0,3);
        double epsilon = VL - (phi.transpose().times(theta_old)).get(0,0);
        theta_old = theta_old.plus(P_old.times(phi).times(epsilon));

        return(theta_old.getColumnPackedCopy());

    }
    public synchronized void updateStates(double baseAngVel, double pendAng, double control) {

        phi.set(0, 0, baseAngVel);
        phi.set(1, 0, signum(baseAngVel));
        pendAng_old = pendAng;
        control_old = control;
        baseAngVel_old = baseAngVel;
    }

    public synchronized double compensate(double baseAngVel) {
        double F = theta_old.get(0, 0) * baseAngVel + theta_old.get(1, 0) * signum(baseAngVel);
        return F;
    }

    public synchronized void setRLSParameters(RLSParameters newRLSParameters) {
        this.rlsParameters = newRLSParameters;
        P_old = new Matrix(new double[][] {{rlsParameters.pam,0},{0,rlsParameters.pbm}},2,2);
        theta_old = new Matrix(new double[][] {{rlsParameters.fvGuess},{rlsParameters.fcGuess}},2,1);
        phi = new Matrix(1,2);
    }

    public synchronized double getFv() {
        return theta_old.get(0,0);
    }
    public synchronized double getFc() {
        return theta_old.get(1,0);
    }
    public synchronized void newAB(Matrix A, Matrix B) {
        this.A = A;
        this.B = B;
    }


}
