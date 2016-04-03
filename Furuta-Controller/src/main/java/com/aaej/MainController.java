package com.aaej;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains the thread for the controller and will decide which
 * controller to be used depending on the output from the process.
 */
class MainController extends Thread {
    private final static Logger LOGGER = Logger.getLogger(MainController.class.getName());
    private ControllerParameters controllerParameters;

	public MainController() {
	}

    public void run() {
        long duration;
        long t = System.currentTimeMillis();

        while(true) {
            doControl();
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

    }
}
