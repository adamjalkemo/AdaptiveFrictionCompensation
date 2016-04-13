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
    private final static Logger LOGGER = Logger.getLogger(MainController.class.getName());
    private ControllerParameters controllerParameters;
    private RLSParameters rlsParameters;
    private TopController topController;
    private SwingUpController swingUpController;
    private CommunicationManager communicationManager;
    private boolean on;
    private Object controllerParametersLock = new Object();


    public MainController(int priority, CommunicationManager communicationManager) {
        setPriority(priority);
        this.communicationManager = communicationManager;
        ControllerParameters newControllerParameters = new ControllerParameters();
        rlsParameters = new RLSParameters();
        topController = new TopController();
        swingUpController = new SwingUpController();
	setControllerParameters(newControllerParameters);
        on = false;
	}

    public void run() {
        long duration;
        long t = System.currentTimeMillis();

        while(true) {
            doControl();
            //TODO: Synchronization needed?
            t = t + controllerParameters.h;
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
            if (chooseTopControl(pendAng,pendAngVel)) {
                u = topController.calculateOutput(pendAng, pendAngVel, baseAng, baseAngVel);
                topController.update();
            } else {
                u = swingUpController.calculateOutput(pendAng, pendAngVel);
            }
        }
        communicationManager.writeOutput(u);

    }

    private boolean chooseTopControl(double pendAng, double pendAngVel) {
        //TODO: Test different switching schemes, parameters should be part of ControllerParameters
        double omega0squared;
        synchronized (controllerParametersLock) {
            omega0squared = controllerParameters.omega0 * controllerParameters.omega0;
        }
	double E = Math.cos(pendAng) - 1 + 1/(2*omega0squared)*pendAngVel*pendAngVel;
	if(E > 0) {
            //on = false;
	    LOGGER.log(Level.INFO, "Switching to top controller");
	}
        return false;
    }

    public void setControllerParameters(ControllerParameters newParameters) {
        //TODO: decide what to synchronize on
        synchronized(controllerParametersLock) {
            controllerParameters = (ControllerParameters) newParameters.clone();
        }
        swingUpController.setControllerParameters(controllerParameters);
        //topController.setControllerParameters(controllerParameters);
    }

    public void shutDown() {
        // Todo
    }
    public ControllerParameters getControllerParameters() {
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
