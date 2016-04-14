package com.aaej;

import se.lth.control.realtime.AnalogIn;
import se.lth.control.realtime.AnalogOut;
import se.lth.control.realtime.IOChannelException;

import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.cos;
/**
 * This class contains the thread for the controller and will decide which
 * controller to be used depending on the output from the process.
 */
class MainController extends Thread {
    private enum Controller {
        TOP,SWINGUP,NONE;
    };
    private final static Logger LOGGER = Logger.getLogger(MainController.class.getName());
    private ControllerParameters controllerParameters;
    private RLSParameters rlsParameters;
    private TopController topController;
    private SwingUpController swingUpController;
    private CommunicationManager communicationManager;
    private boolean on;
    private Object controllerParametersLock = new Object();
    private boolean shutDown;
    private Controller activeController = Controller.NONE;

    public MainController(int priority, CommunicationManager communicationManager) {
        setPriority(priority);
        this.communicationManager = communicationManager;
        ControllerParameters newControllerParameters = new ControllerParameters();
        rlsParameters = new RLSParameters();
        topController = new TopController();
        swingUpController = new SwingUpController();
        setControllerParameters(newControllerParameters);
        on = false;
        shutDown = false;
	}

    public void run() {
        long duration;
        long t = System.currentTimeMillis();

        while(!shutDown) {
            doControl();
            synchronized (controllerParametersLock) {
                t = t + controllerParameters.h;
            }
            duration = t-System.currentTimeMillis();
            if(duration > 0) {
                try {
                    Thread.sleep(duration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                LOGGER.log(Level.WARNING, "Missed Deadline");
            }
        }
    }

    private void doControl() {
        communicationManager.readInput();
        double pendAng = communicationManager.getPendAng();
        double pendAngVel = communicationManager.getPendAngVel();
        double baseAng = communicationManager.getBaseAng();
        double baseAngVel = communicationManager.getBaseAngVel();
        double u = 0;
        if(on) {
            activeController = chooseTopControl(pendAng,pendAngVel);
            if(activeController == Controller.TOP) {
                u = topController.calculateOutput(pendAng, pendAngVel, baseAng, baseAngVel);
                topController.update();
            } else if(activeController == Controller.SWINGUP) {
                u = swingUpController.calculateOutput(pendAng, pendAngVel);
            } else {
                //Keep u as 0
            }
        }
        communicationManager.writeOutput(u);

    }

    private Controller chooseTopControl(double pendAng, double pendAngVel) {
        //TODO: Test different switching schemes, parameters should be part of ControllerParameters
        double omega0squared;
        synchronized (controllerParametersLock) {
            omega0squared = controllerParameters.omega0 * controllerParameters.omega0;
        }
        double E = Math.cos(pendAng) - 1 + 1/(2*omega0squared)*pendAngVel*pendAngVel;
        if(E > 0) {
            if(activeController != Controller.TOP) {
                LOGGER.log(Level.INFO, "Switching to top controller");
            }
            return Controller.TOP;
        } else {
            if(activeController != Controller.SWINGUP) {
                LOGGER.log(Level.INFO, "Switching to swingup controller");
            }
            return Controller.SWINGUP;
        }
    }

    public void setControllerParameters(ControllerParameters newParameters) {
        synchronized(controllerParametersLock) {
            controllerParameters = (ControllerParameters) newParameters.clone();
        }
        swingUpController.setControllerParameters(controllerParameters);
        topController.setControllerParameters(controllerParameters);
    }

    public void shutDown() {
        shutDown = true;
    }
    public ControllerParameters getControllerParameters() {
        //TODO should this be synchronized?
        return (ControllerParameters)controllerParameters.clone();
    }
    public RLSParameters getRLSParameters() {
        return (RLSParameters)rlsParameters.clone();
    }
    public void setRLSParameters(RLSParameters rlsParameters) {
        this.rlsParameters = (RLSParameters)rlsParameters.clone();
    }
    public void resetEstimator() {
        //TODO
    }
    public void regulatorActive(boolean on) {
        this.on = on;
    }
}
