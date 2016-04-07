package com.aaej;

import se.lth.control.realtime.AnalogIn;
import se.lth.control.realtime.AnalogOut;
import se.lth.control.realtime.IOChannelException;

import java.util.logging.Level;
import java.util.logging.Logger;

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


	public MainController(int priority, CommunicationManager communicationManager) {
        setPriority(priority);
        this.communicationManager = communicationManager;
        controllerParameters = new ControllerParameters();
        rlsParameters = new RLSParameters();
        topController = new TopController();
        swingUpController = new SwingUpController();
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
        double u = 0;
        if(on) {
            if (chooseTopControl()) {
                u = topController.calculateOutput();
                topController.update();
            } else {
                u = swingUpController.calculateOutput(communicationManager.pendAng, communicationManager.pendAngVel);
            }
        }
        communicationManager.writeOutput(u);

    }

    private boolean chooseTopControl() {
        //TODO: Test different switching schemes, parameters should be part of ControllerParameters
        return false;
    }

    public void setControllerParameters(ControllerParameters newParameters) {
        //TODO: decide what to synchronize on
        controllerParameters = (ControllerParameters)newParameters.clone();
        swingUpController.setControllerParameters(controllerParameters);
        topController.setControllerParameters(controllerParameters);
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
