package com.aaej;

import Jama.Matrix;
import se.lth.control.realtime.AnalogIn;
import se.lth.control.realtime.AnalogOut;
import se.lth.control.realtime.IOChannelException;

import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.atan;
import static java.lang.Math.PI;

import java.util.Observer;
import java.util.Observable;
import java.util.ArrayList;

/**
 * This class contains the thread for the controller and will decide which
 * controller(s) to be used depending on the output from the process.
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
    private FrictionCompensator frictionCompensator;
    private CommunicationManager communicationManager; // Handles all input/output scaling and talks to the analog box
    private boolean on; // determines if controller is on or off
    private Object controllerParametersLock = new Object();
    private boolean shutDown; // determines if the application should shut down
    private Controller activeController = Controller.NONE;
    private ArrayList<Observer> observerList; // Used to update GUI about which is the current controller
    private boolean enableFrictionCompensation;
    private double r = 0; // reference signal

    public MainController(int priority, CommunicationManager communicationManager) {
        setPriority(priority);
        this.communicationManager = communicationManager;
        ControllerParameters newControllerParameters = new ControllerParameters();
        rlsParameters = new RLSParameters();
        topController = new TopController();
        swingUpController = new SwingUpController();
        frictionCompensator = new FrictionCompensator();
        frictionCompensator.setRLSParameters(rlsParameters);
        setControllerParameters(newControllerParameters);
        observerList = new ArrayList<Observer>();
        on = false;
        shutDown = false;
        enableFrictionCompensation = true;
	}

    /*
        Outputs warnings if deadlines are missed.
        Shuts down if the shutdown variable is set to true.
        Writes zero to the communicationManager when shutting down.
     */
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
	   communicationManager.writeOutput(0);
    }

    /*
        Determines which controllers are to be used (with help from chooseController) and
        writes outputs and updates states.
     */
    private void doControl() {
        communicationManager.readInput();
        double pendAng = communicationManager.getPendAng();
        double pendAngVel = communicationManager.getPendAngVel();
        double baseAng = communicationManager.getBaseAng();
        double baseAngVel = communicationManager.getBaseAngVel();

        boolean insideDeadzone;

        // TODO add deadzones for all
        insideDeadzone = Math.abs(baseAngVel) < rlsParameters.deadzoneBaseAngVel;
        insideDeadzone = insideDeadzone || Math.abs(pendAngVel) < rlsParameters.deadzonePendAngVel;

        double u = 0;

        if(on) {
            activeController = chooseController(pendAng,pendAngVel);
            if(activeController == Controller.TOP && !insideDeadzone) {
                u = topController.calculateOutput(pendAng, pendAngVel, baseAng, baseAngVel, r);

                frictionCompensator.rls(baseAng, baseAngVel);

        		if(enableFrictionCompensation && !insideDeadzone) {
                    u = u + frictionCompensator.compensate(baseAngVel);
    	        }
            } else if(activeController == Controller.SWINGUP) {
                u = swingUpController.calculateOutput(pendAng, pendAngVel);
            } else {
                //Keep u as 0
            }

        }
        u = communicationManager.writeOutput(u);

        frictionCompensator.updateStates(baseAng, baseAngVel, pendAng, u);

        communicationManager.plotRLSParameters(frictionCompensator.getFv(), frictionCompensator.getFc());
   }

    private Controller chooseController(double pendAng, double pendAngVel) {
        //TODO: Test different switching schemes, parameters should be part of ControllerParameters
        Controller newController;

        double ar = controllerParameters.ellipseRadius1;
        double br = controllerParameters.ellipseRadius2;
        double alfar=atan(9.4/0.62); // Could add this to GUI
        double X = pendAng;
        double Y = pendAngVel;
        double term1 = X * cos(alfar) + Y * sin(alfar);
        double term2 = -X * sin(alfar) + Y * cos(alfar);

        if((term1*term1/(ar*ar) + term2*term2/(br*br)) < controllerParameters.limit) {
            newController = Controller.TOP;
        } else {
            newController = Controller.SWINGUP;
        }
        checkForChangeOfController(newController);
        return newController;
    }

    // Logs changes of controller
    private void checkForChangeOfController(Controller newController) {
        if (activeController != newController) {
            if (newController == Controller.TOP) {
                communicationManager.resetOffsetBaseAng();
                LOGGER.log(Level.INFO, "Switching to top controller");

            } else if (newController == Controller.SWINGUP) {
                LOGGER.log(Level.INFO, "Switching to swingup controller");
            }

            notifyObservers(newController);
        }
    }

    public void setControllerParameters(ControllerParameters newParameters) {
        synchronized(controllerParametersLock) {
            controllerParameters = (ControllerParameters) newParameters.clone();
        }
        swingUpController.setControllerParameters(controllerParameters);
        topController.setControllerParameters(controllerParameters);

        //Also update friction compensator (it doesn't have its own copy of controller parameters, just need the A and B matrix
        Matrix AandB[] = Discretizer.c2d(controllerParameters.h);
        frictionCompensator.newAB(AandB[0], AandB[1]);
    }

    public synchronized void shutDown() {
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
        frictionCompensator.setRLSParameters(this.rlsParameters);
    }
    public void resetEstimator() {
	frictionCompensator.reset();
    }
    public void regulatorActive(boolean on) {
        this.on = on;
    }

    // For updates about which controller being used
    public void registerObserver(Observer o) {
        observerList.add(o);
    }
    public void notifyObservers(Controller newController) {
        for (Observer o : observerList)
            o.update(null, newController.name());
    }

    public void setEnableFrictionCompensation(boolean enableFrictionCompensation) {
        this.enableFrictionCompensation = enableFrictionCompensation;
    }

    public synchronized void setReference(double r) {
        this.r = r;
    }
}
