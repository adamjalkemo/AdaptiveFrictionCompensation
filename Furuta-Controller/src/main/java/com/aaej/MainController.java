package com.aaej;

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

    public MainController(int priority, CommunicationManager communicationManager) {
        setPriority(priority);
        this.communicationManager = communicationManager;
        ControllerParameters newControllerParameters = new ControllerParameters();
        rlsParameters = new RLSParameters();
        topController = new TopController();
        swingUpController = new SwingUpController();
        setControllerParameters(newControllerParameters);
        frictionCompensator = new FrictionCompensator();
        frictionCompensator.setRLSParameters(rlsParameters);
        observerList = new ArrayList<Observer>();
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
        double u = 0;
        if(on) {
            activeController = chooseController(pendAng,pendAngVel);
            if(activeController == Controller.TOP) {
                u = topController.calculateOutput(pendAng, pendAngVel, baseAng, baseAngVel);
                topController.update();
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
        communicationManager.writeOutput(u);
    }

    private Controller chooseController(double pendAng, double pendAngVel) {
        //TODO: Test different switching schemes, parameters should be part of ControllerParameters
        Catcher catcher;
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
            if((term1*term1/(ar*ar) + term2*term2/(br*br)) < controllerParameters.limit) { // 
                if (activeController != Controller.TOP) {
                    LOGGER.log(Level.INFO, "Switching to top controller");

                    // To decrease irratic behavior
                    sleepAfterFall = (int) (((long) 4000)/controllerParameters.h); // Corresponds to 4s rest
                }
                return Controller.TOP;
            } else {
                if (activeController != Controller.SWINGUP) {
                    LOGGER.log(Level.INFO, "Switching to swingup controller");

                }
                return Controller.SWINGUP;
            }
        } else if(catcher == Catcher.REASONABLEANGLE) {
            if ((Math.abs(pendAng) + Math.abs(pendAngVel)) < 0.8) {
                if (activeController != Controller.SWINGUP) {
                    LOGGER.log(Level.INFO, "Switching to top controller");
                }
                return Controller.TOP;
            } else {
                if (activeController != Controller.SWINGUP) {
                    LOGGER.log(Level.INFO, "Switching to swingup controller");
                }
                return Controller.SWINGUP;
            }
        } else if(catcher == Catcher.ENERGY) {
            double omega0squared;
            synchronized (controllerParametersLock) {
                omega0squared = controllerParameters.omega0 * controllerParameters.omega0;
            }
            double E = Math.cos(pendAng) - 1 + 1 / (2 * omega0squared) * pendAngVel * pendAngVel;
            if (E > 0) {
                if (activeController != Controller.TOP) {
                    LOGGER.log(Level.INFO, "Switching to top controller");
                }
                return Controller.TOP;
            } else {
                if (activeController != Controller.SWINGUP) {
                    LOGGER.log(Level.INFO, "Switching to swingup controller");
                }
                return Controller.SWINGUP;
            }
        } else {
            return Controller.NONE;
        }
    }

    private void logChangeController() {
        //adam
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
        frictionCompensator.setRLSParameters(this.rlsParameters);
    }
    public void resetEstimator() {
        //TODO
    }
    public void regulatorActive(boolean on) {
        this.on = on;
    }

    // For updates about which controller being used
    public void registerObserver(Observer o) {
        observerList.add(o);
    }
    public void notifyObservers() {
        for (Observer o : observerList)
            o.update(null, activeController.name());
    }
}
