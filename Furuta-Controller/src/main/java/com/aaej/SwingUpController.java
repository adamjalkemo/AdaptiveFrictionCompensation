package com.aaej;


import static java.lang.Math.cos;
import static java.lang.Math.signum;

class SwingUpController {
    private ControllerParameters controllerParameters;

    public SwingUpController() {
    }

    public synchronized double calculateOutput(double pendAng, double pendAngVel) {
        double omegaSquared = controllerParameters.omega0 * controllerParameters.omega0;
        double pendAngVelSquared = pendAngVel*pendAngVel;
        //double y = (cos(pendAng) * (pendAngVel * (cos(pendAng)) - 1 + pendAngVelSquared / (2 * omegaSquared)));
        double y = cos(pendAng) * pendAngVel * (cos(pendAng) - 1 + pendAngVelSquared / (2 * omegaSquared));
        return controllerParameters.gain*signum(y);
    }

    public synchronized void setControllerParameters(ControllerParameters controllerParameters) {
        this.controllerParameters = controllerParameters;
    }

}
