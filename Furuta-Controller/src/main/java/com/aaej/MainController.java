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
    private FrictionCompensator frictionCompensator;
    private CommunicationManager communicationManager;
    private boolean on;
    private Object controllerParametersLock = new Object();
    private boolean shutDown;
    private Controller activeController = Controller.NONE;
    private int sleepAfterFall = 0;
    private ArrayList<Observer> observerList;
    private boolean enableFrictionCompensation;
    private Kalman kalmanFilter;
    private Boolean enableKalman;
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
        enableKalman = false;

        // Kalman things, TODO, move to function and make sure that it updates when h is changed
        Matrix AandB[] = Discretizer.c2d(controllerParameters.h);
        kalmanFilter = new Kalman(AandB[0], AandB[1]);
        kalmanFilter.setRLSParameters(rlsParameters);
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
	   communicationManager.writeOutput(0);
    }

    private void doControl() {
        communicationManager.readInput();
        double pendAng = communicationManager.getPendAng();
        double pendAngVel = communicationManager.getPendAngVel();
        double pendAngTop = communicationManager.getPendAngTop();
        double pendAngVelTop = communicationManager.getPendAngVelTop();
        double baseAng = communicationManager.getBaseAng();
        double baseAngVel = communicationManager.getBaseAngVel();

        double pendAngKalman, pendAngVelKalman, baseAngKalman, baseAngVelKalman;

        Boolean enableKalmanSync;
        synchronized(enableKalman) { // Hur gör vi detta snyggast?
             enableKalmanSync= enableKalman;
        }

        //KALMAN HERE
        if (enableKalmanSync) {
            Matrix yhat = kalmanFilter.calculateYHat(new double[]{pendAng, pendAngVel, baseAng, baseAngVel});
            pendAngKalman = yhat.get(0,0);
            pendAngVelKalman = yhat.get(1,0);
            baseAngKalman = yhat.get(2,0);
            baseAngVelKalman = yhat.get(3,0);
            communicationManager.pendAngKalman = pendAngKalman;
            communicationManager.pendAngVelKalman = pendAngVelKalman;
            communicationManager.baseAngKalman = baseAngKalman;
            communicationManager.baseAngVelKalman = baseAngVelKalman;
        } else { // I get errors if I don't define these
            pendAngKalman = pendAng;
            pendAngVelKalman = pendAngVel;
            baseAngKalman = baseAng;
            baseAngVelKalman = baseAngVel;
        }
        
        boolean insideDeadzone = Math.abs(pendAngKalman) < rlsParameters.deadzonePendAng;

        double u = 0;
        double uF = 0;
        double uBeforeFrictionComp = 0;
        double vL = 0;

        if(on) {
            activeController = chooseController(pendAng,pendAngVel);
            if(activeController == Controller.TOP) {
                if (enableKalmanSync) {
                    u = topController.calculateOutput(pendAngKalman, pendAngVelKalman, baseAngKalman, baseAngVelKalman, stepReference);
                    topController.update();

                    vL = frictionCompensator.rls(baseAngKalman, baseAngVelKalman);                
                } else {
                    u = topController.calculateOutput(pendAng, pendAngVel, baseAng, baseAngVel, stepReference);
                    topController.update();

                    vL = frictionCompensator.rls(baseAng, baseAngVel);
                }

        uBeforeFrictionComp = u;
		if(enableFrictionCompensation && !insideDeadzone) {
                if (enableKalmanSync) {
                    uF = frictionCompensator.compensate(baseAngVelKalman);
                    u = u + uF;
                } else {
                    uF = frictionCompensator.compensate(baseAngVel);
                    u = u + uF;
                }
	        }
            } else if(activeController == Controller.SWINGUP) {
                if (sleepAfterFall > 0) {
                    sleepAfterFall--;
                    u = 0;
                } else {
                    u = swingUpController.calculateOutput(pendAng, pendAngVel);
                }
            } else {
                //Keep u as 0
            }

        }
        u = communicationManager.writeOutput(u);
        communicationManager.saveUF(uF);
        communicationManager.saveVL(vL);

        if (enableKalmanSync) {
            frictionCompensator.updateStates(baseAngKalman, baseAngVelKalman, pendAngKalman, u);
        } else {
            frictionCompensator.updateStates(baseAng, baseAngVel, pendAng, u);
        }

        // I think you can run this even if enableKalman = false;
        //kalmanFilter.updateStates(uBeforeFrictionComp);

        communicationManager.plotRLSParameters(frictionCompensator.getFv(), frictionCompensator.getFc(), frictionCompensator.getFo());
   }

    private Controller chooseController(double pendAng, double pendAngVel) {
        //TODO: Test different switching schemes, parameters should be part of ControllerParameters
        Catcher catcher;
        Controller newController;

        synchronized (controllerParametersLock) {
            catcher = controllerParameters.catcher;
        }
        if(catcher == Catcher.ELLIPSE) {
            //double ar=sqrt(0.62*0.62+9.4*9.4);
            //ar=ar*0.2;
            double ar = controllerParameters.ellipseRadius1;
            //double br=sqrt(0.19*0.19+2.5*2.5);
            //br=br*0.2;
            double br = controllerParameters.ellipseRadius2;
            double alfar=atan(9.4/0.62); // Could add this to GUI
            double X = pendAng;
            double Y = pendAngVel;
            double term1 = X*cos(alfar)+Y*sin(alfar);
            double term2 = -X*sin(alfar) + Y*cos(alfar);

            if((term1*term1/(ar*ar) + term2*term2/(br*br)) < controllerParameters.limit) {
                newController = Controller.TOP;
            } else {
                newController = Controller.SWINGUP;
            }

        } else if(catcher == Catcher.REASONABLEANGLE) {
            if ((Math.abs(pendAng) + Math.abs(pendAngVel)) < 0.8) {
                newController = Controller.TOP;
            } else {
                newController = Controller.SWINGUP;
            }
        } else if(catcher == Catcher.ENERGY) {
            double omega0squared;
            synchronized (controllerParametersLock) {
                omega0squared = controllerParameters.omega0 * controllerParameters.omega0;
            }
            double E = Math.cos(pendAng) - 1 + 1 / (2 * omega0squared) * pendAngVel * pendAngVel;
            if (E > 0) {
                newController = Controller.TOP;
            } else {
                newController = Controller.SWINGUP;
            }
        } else {
            newController = Controller.NONE;
        }
        checkForChangeOfController(newController);
        return newController;
    }

    // Logs changes of controller
    private void checkForChangeOfController(Controller newController) {
        if (activeController != newController) {
            if (newController == Controller.TOP) {
		//communicationManager.setOffsetBaseAngScaled(-communicationManager.getBaseAng());
                communicationManager.resetOffsetBaseAng();
                LOGGER.log(Level.INFO, "Switching to top controller");

                // To decrease irratic behavior
                //sleepAfterFall = (int) (((long) 4000)/controllerParameters.h); // Corresponds to 4s rest
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
        frictionCompensator.setRLSParameters(this.rlsParameters);
        kalmanFilter.setRLSParameters(this.rlsParameters);
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

    public synchronized void toggleKalman() {
        enableKalman = !enableKalman;
        LOGGER.log(Level.INFO, "Kalman enabled: " + (enableKalman ? "True" : "False"));
    }

    public synchronized void toggleStepResponse() {
        if (stepReference == 0) {
            stepReference = Math.PI;
        } else {
            stepReference = 0;
        }
    }
}
