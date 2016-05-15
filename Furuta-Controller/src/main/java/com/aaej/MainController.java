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
    private double stepReference = 0;

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

        boolean insideDeadzone = false;

        // TODO add deadzones for all
        //insideDeadzone = Math.abs(baseAngVel) < controllerParameters.deadzoneBaseAngVel;
        //insideDeadzone = insideDeadzone || Math.abs(pendAngVel) < controllerParameters.deadzonePendAngVel;

        double u = 0;
        double uF = 0;
        double vL = 0;

        if(on) {
            activeController = chooseController(pendAng,pendAngVel);
            if(activeController == Controller.TOP && !insideDeadzone) {
                u = topController.calculateOutput(pendAng, pendAngVel, baseAng, baseAngVel, r);
        		if(enableFrictionCompensation) {
                    uF = frictionCompensator.compensate(baseAngVel);
                    u = u + uF;

    	        }
            } else if(activeController == Controller.SWINGUP) {
                u = swingUpController.calculateOutput(pendAng, pendAngVel);
            } else {
                //Keep u as 0
            }
        }
        u = communicationManager.writeOutput(u);
        communicationManager.saveUF(uF);
        communicationManager.saveVL(vL);


        frictionCompensator.rls(baseAng, baseAngVel);
        frictionCompensator.updateStates(baseAng, baseAngVel, pendAng, u);

        // We need to tell the communication manager what Fv, Fc and Fo is
        communicationManager.plotRLSParameters(frictionCompensator.getFv(), frictionCompensator.getFc(), frictionCompensator.getFo());
   }

    private Controller chooseController(double pendAng, double pendAngVel) {
        Controller newController;
        synchronized (controllerParametersLock) {
            double ar = controllerParameters.ellipseRadius1;
            double br = controllerParameters.ellipseRadius2;
            double alfar = atan(9.4 / 0.62); // TODO Could add this to GUI
            double X = pendAng;
            double Y = pendAngVel;
            double term1 = X * cos(alfar) + Y * sin(alfar);
            double term2 = -X * sin(alfar) + Y * cos(alfar);

            if ((term1 * term1 / (ar * ar) + term2 * term2 / (br * br)) < controllerParameters.limit) {
                newController = Controller.TOP;
            } else {
                newController = Controller.SWINGUP;
            }
        }
        // If controller changes, it should notify the UI.
        checkForChangeOfController(newController);
        return newController;
    }

    /*
        Logs change of controller and notifies the GUI about the change.
     */
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


    /*
        Sets the controller parameters for all underlying controllers.
        The whole method is not synchronized to avoid blocking
    */
    public void setControllerParameters(ControllerParameters newParameters) {
        synchronized(controllerParametersLock) {
            // Only shallow clone, references to objects will not be copied.
            this.controllerParameters = (ControllerParameters) newParameters.clone();
        }
        swingUpController.setControllerParameters(controllerParameters);
        topController.setControllerParameters(controllerParameters);

        // Also update friction compensator (it doesn't have its own copy of controller parameters,
        // it just needs the Phi and Gamma matrices
        Matrix phiAndGamma[] = Discretizer.c2d(controllerParameters.h); // c2d has continous A and B matrices
        frictionCompensator.newAB(phiAndGamma[0], phiAndGamma[1]);
    }


    public void setRLSParameters(RLSParameters rlsParameters) {
        // this.rlsParameters is not used in the controller thread, no synchronization needed here
        this.rlsParameters = (RLSParameters)rlsParameters.clone();

        // This method is synchronized
        frictionCompensator.setRLSParameters(this.rlsParameters);
    }

    // For shutting down the whole application
    public synchronized void shutDown() {
        shutDown = true;
    }

    // Just used when initializing the GUI
    public ControllerParameters getControllerParameters() {
        return (ControllerParameters)controllerParameters.clone();
    }

    // Just used when initializing the GUI
    public RLSParameters getRLSParameters() {
        return (RLSParameters)rlsParameters.clone();
    }

    // Called from the GUI when RLS should be reset
    public void resetEstimator() {
	frictionCompensator.reset();
    }

    // Called from the GUI when controller should run
    public void regulatorActive(boolean on) {
        this.on = on;
    }

    // For updates about which controller is being used
    public void registerObserver(Observer o) {
        observerList.add(o);
    }

    public void notifyObservers(Controller newController) {
        for (Observer o : observerList) // it's really just one observer.
            o.update(null, newController.name()); // This sends a reference to a string, no sync needed here
    }

    // On and of switch for friction compensation
    public void setEnableFrictionCompensation(boolean enableFrictionCompensation) {
        this.enableFrictionCompensation = enableFrictionCompensation;
    }

    // For change of reference, acts
    public synchronized void setReference(double r) {
        this.r = r;
    }

    public synchronized void toggleStepResponse() {
        if (stepReference == 0) {
            stepReference = Math.PI;
        } else {
            stepReference = 0;
        }
    }
}
