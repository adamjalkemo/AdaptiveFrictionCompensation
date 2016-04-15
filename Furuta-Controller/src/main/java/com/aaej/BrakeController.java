package com.aaej;


class BrakeController {
    private ControllerParameters controllerParameters;
    private PID pid;
    public BrakeController() {
        pid = new PID();
    }

    public synchronized double calculateOutput(double baseAng) {
        return pid.calculateOutput(baseAng);
    }

    public synchronized void updateState(double u) {
        pid.updateState(u);
    }

    public synchronized void setControllerParameters(ControllerParameters controllerParameters) {
        this.controllerParameters = controllerParameters;
        pid.updateParameters(0.002, 1000, 0.02, 1, 0.5, 1000, controllerParameters.h);
    }

    public void reset() {
        pid.reset();
    }

    class PID {

        private double u, e, v;//, y;
        private double K, Ti, Td, Beta, Tr, N, h;
        private double ad, bd;
        private double D, I, yOld;

        private double yref = 0;

        public PID() {

        }

        /*public PID(double nK, double nTi, double nTd, double nBeta, double nTr,
                    double nN, double nh) {
            updateParameters(nK, nTi, nTd, nBeta, nTr, nN, nh);
        }*/

        public void updateParameters(double nK, double nTi, double nTd, double nBeta, double nTr,
                    double nN, double nh) {
            /*K = 0.2;
            Ti = 1000;
            Td = 0.02;
            Beta = 1;
            Tr = 0.5;
            N = 1000;*/

            K = nK;
            Ti = nTi;
            Td = nTd;
            Beta = nBeta;
            Tr = nTr;
            N = nN;
            h = nh;
            ad = Td / (Td + N*h);
            bd = K*ad*N;
        }
        
        public double calculateOutput(double y) {
            e = yref - y;
            D = ad*D - bd*(y - yOld);
            v = K*(Beta*yref - y) + I + D;
            yOld = y;
            return v;
        }

        private double saturate(double u) {
            u = (u > 0.6 ? 0.6 : u);
            u = (u < -0.6 ? -0.6 : u);
            return u;
        }

        public void updateState(double u) {
            I = I + (K*h/Ti)*e + (h/Tr)*(u-v);
            //yOld = y;
        }

        public void reset() {
            I = 0;
            D = 0;
            yOld = 0;
            //v = 0;

        }

    }

}
