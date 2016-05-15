package com.aaej;

import Jama.Matrix;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

// TODO comment this class
public class FrictionCompensator {
    private RLSParameters rlsParameters;
    private Matrix P_old;
    private Matrix theta_old;
    private Matrix phi;
    private Matrix A;
    private Matrix B;
    private double pendAng_old = 0;
    private double baseAngVel_old = 0;
    private double baseAng_old = 0;
    private double control_old = 0;

    public FrictionCompensator() {
        phi = new Matrix(3,1);
        P_old = new Matrix(3,3);
        theta_old = new Matrix(3,1);
    }

    public synchronized double rls(double baseAng, double baseAngVel) {

        P_old = (P_old.minus((P_old.times(phi).times(phi.transpose()).times(P_old))
                .times(1.0/(rlsParameters.lambda+(phi.transpose().times(P_old).times(phi)).get(0,0))))).times(1/rlsParameters.lambda);
        double VL = -(baseAngVel + A.get(3,0)*pendAng_old - B.get(3,0)*control_old-A.get(3,3)*baseAngVel_old)/B.get(3,0);
	//double VL = (A.get(2,2)*baseAng_old + A.get(2,3)*baseAngVel_old - baseAng)/0.0096 + control_old;
        double epsilon = VL - (phi.transpose().times(theta_old)).get(0,0);
        theta_old = theta_old.plus(P_old.times(phi).times(epsilon));

        return(VL);

    }
    public synchronized void updateStates(double baseAng, double baseAngVel, double pendAng, double control) {

        phi.set(0, 0, baseAngVel);
        phi.set(1, 0, signum(baseAngVel));
        phi.set(2, 0, 1);
        pendAng_old = pendAng;
        control_old = control;
        baseAngVel_old = baseAngVel;
	baseAng_old = baseAng;
    }

    public synchronized double compensate(double baseAngVel) {
        double F = theta_old.get(0, 0) * baseAngVel + theta_old.get(1, 0) * signum(baseAngVel) + theta_old.get(2, 0);
        return F;
    }

    public synchronized void setRLSParameters(RLSParameters newRLSParameters) {
        this.rlsParameters = newRLSParameters;
        P_old = new Matrix(new double[][] {{rlsParameters.pam,0,0},{0,rlsParameters.pbm,0},{0,0,rlsParameters.pcm}},3,3);
        theta_old = new Matrix(new double[][] {{rlsParameters.fvGuess},{rlsParameters.fcGuess},{rlsParameters.foGuess}},3,1);
    }

    public synchronized double getFv() {
        return theta_old.get(0,0);
    }
    public synchronized double getFc() {
        return theta_old.get(1,0);
    }

    public synchronized double getFo() {
        return theta_old.get(2,0);
    }
    public synchronized void newAB(Matrix A, Matrix B) {
        this.A = A;
        this.B = B;
    }

    public synchronized void reset() {
        P_old = new Matrix(new double[][] {{rlsParameters.pam,0,0},{0,rlsParameters.pbm,0},{0,0,rlsParameters.pcm}},3,3);
        theta_old = new Matrix(new double[][] {{rlsParameters.fvGuess},{rlsParameters.fcGuess},{rlsParameters.foGuess}},3,1);
    }
        


}
