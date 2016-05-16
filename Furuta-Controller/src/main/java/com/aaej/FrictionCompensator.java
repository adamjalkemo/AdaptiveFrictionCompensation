package com.aaej;

import Jama.Matrix;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

/**
 * This class handles everything with the RLS parameters and how the new compensated control signal is calculated
 */
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

    }

    /**
     * Update the P matrix and theta vector for the RLS
     */
    public synchronized double rls(double baseAng, double baseAngVel) {

        P_old = (P_old.minus((P_old.times(phi).times(phi.transpose()).times(P_old))
                .times(1.0/(rlsParameters.lambda+(phi.transpose().times(P_old).times(phi)).get(0,0))))).times(1/rlsParameters.lambda);
        double VL = -(baseAngVel + A.get(3,0)*pendAng_old - B.get(3,0)*control_old-A.get(3,3)*baseAngVel_old)/B.get(3,0);
	//double VL = (A.get(2,2)*baseAng_old + A.get(2,3)*baseAngVel_old - baseAng)/0.0096 + control_old;
        double epsilon = VL - (phi.transpose().times(theta_old)).get(0,0);
        theta_old = theta_old.plus(P_old.times(phi).times(epsilon));

        return(VL);

    }

    /**
     * Update the phi vector according to which regressor is used. Also save needed values for next iteration.
     */
    public synchronized void updateStates(double baseAng, double baseAngVel, double pendAng, double control) {
        phi.set(0, 0, signum(baseAngVel));
        if (rlsParameters.regressorModel > 0)
            phi.set(1, 0, baseAngVel);
        if (rlsParameters.regressorModel == 2)
            phi.set(2, 0, 1);
        pendAng_old = pendAng;
        control_old = control;
        baseAngVel_old = baseAngVel;
	    baseAng_old = baseAng;
    }

    /**
    * Calculates how much to add to the control signal to compensate for the friction
    */
    public synchronized double compensate(double baseAngVel) {
        double F; 
        if (rlsParameters.regressorModel == 0)
            F = theta_old.get(0, 0) * signum(baseAngVel);
        else if (rlsParameters.regressorModel == 1)
            F = theta_old.get(0, 0) * signum(baseAngVel) + theta_old.get(1, 0) * baseAngVel;
        else
            F = theta_old.get(0, 0) * signum(baseAngVel) + theta_old.get(1, 0) * baseAngVel + theta_old.get(2, 0);


        return F;
    }

    /**
     * Get new parameters that are set from the GUI
     */
    public synchronized void setRLSParameters(RLSParameters newRLSParameters) {
        this.rlsParameters = newRLSParameters;
        reset();
    }

    public synchronized double getFv() {
        if (rlsParameters.regressorModel > 0)
            return theta_old.get(1,0);
        else
            return 0;  
    }
    public synchronized double getFc() {
        return theta_old.get(0,0);
    }

    public synchronized double getFo() {
        if (rlsParameters.regressorModel == 2)
            return theta_old.get(2,0);
        else
            return 0;
    }
    public synchronized void newAB(Matrix A, Matrix B) {
        this.A = A;
        this.B = B;
    }

    /**
     * Reset everything related to the RLS by creating new matrices
     */
    public synchronized void reset() {
        int m = rlsParameters.regressorModel + 1; // Order of regressor
        phi = new Matrix(m,1);
        P_old = new Matrix(m,m);
        theta_old = new Matrix(m,1);

        if (rlsParameters.regressorModel == 0) {
            P_old = new Matrix(new double[][] {{rlsParameters.pam}},1,1);
            theta_old = new Matrix(new double[][] {{rlsParameters.theta0[0][0]}},1,1);            
        } else if (rlsParameters.regressorModel == 1) {
            P_old = new Matrix(new double[][] {{rlsParameters.pam,0},{0,rlsParameters.pam}},2,2);
            theta_old = new Matrix(new double[][] {{rlsParameters.theta0[1][0]},{rlsParameters.theta0[1][1]}},2,1);            
        } else {
            P_old = new Matrix(new double[][] {{rlsParameters.pam,0,0},{0,rlsParameters.pbm,0},{0,0,rlsParameters.pcm}},3,3);
            theta_old = new Matrix(new double[][] {{rlsParameters.theta0[2][0]},{rlsParameters.theta0[2][1]},{rlsParameters.theta0[2][2]}},3,1);  
        }
    }
        


}
