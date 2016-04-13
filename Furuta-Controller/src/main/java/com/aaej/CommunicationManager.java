package com.aaej;

import se.lth.control.realtime.AnalogIn;
import se.lth.control.realtime.AnalogOut;
import se.lth.control.realtime.IOChannelException;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.lang.Math;

/**
 * Created by alexander on 4/5/16.
 */
public class CommunicationManager {
    private final static Logger LOGGER = Logger.getLogger(MainController.class.getName());
    private FurutaGUI gui;
    private AnalogOut analogU;
    private AnalogIn analogPendAng;
    private AnalogIn analogPendAngVel;
    private AnalogIn analogPendAngTop;
    private AnalogIn analogPendAngVelTop;
    private AnalogIn analogBaseAng;
    private AnalogIn analogBaseAngVel;

    private double u;
    private double pendAng;
    private double pendAngVel;
    private double pendAngTop;
    private double pendAngVelTop;
    private double baseAng;
    private double baseAngVel;

    private long t;
    private long startTime;

    private double offsetPendAngTop = 0.7792;
    private double scalingPendAngTop = 0.058;

    private double offsetPendAngVelTop = 0;
    private double scalingPendAngVelTop = 0.68;

    private double offsetPendAng = 5.1763;
    private double scalingPendAng = 0.3091;

    private double offsetPendAngVel = -0.022;
    private double scalingPendAngVel = 3.76;

    private double offsetBaseAng = 0.5;//0;
    private double scalingBaseAng = 1.28;//2.56;

    private double offsetBaseAngVel = 0.0708;
    private double scalingBaseAngVel = 2;

    private double scalingOutput = -1.40;


    public CommunicationManager(FurutaGUI gui) {
        this.gui = gui;
	startTime = System.currentTimeMillis();
        try {
            analogU = new AnalogOut(0);
            analogPendAng = new AnalogIn(6);
            analogPendAngVel = new AnalogIn(7);
            analogPendAngTop = new AnalogIn(2);
            analogPendAngVelTop = new AnalogIn(3);
            analogBaseAng = new AnalogIn(4);
            analogBaseAngVel = new AnalogIn(5);
        } catch (IOChannelException e) {
            e.printStackTrace();
        } catch (UnsatisfiedLinkError e) {
            LOGGER.log(Level.SEVERE, "Couldn't connect to analogbox");
            //System.exit(1);
        }
    }

    public synchronized void readInput() {
        try {
            pendAng = (analogPendAng.get()+offsetPendAng)*scalingPendAng;
            pendAngVel = (analogPendAngVel.get()+offsetPendAngVel)*scalingPendAngVel;
            pendAngTop = (analogPendAngTop.get()+offsetPendAngTop)*scalingPendAngTop;
            pendAngVelTop = (analogPendAngVelTop.get()+offsetPendAngVelTop)*scalingPendAngVelTop;
            baseAng = (analogBaseAng.get()+offsetBaseAng)*scalingBaseAng;
            baseAngVel = (analogBaseAngVel.get()+offsetBaseAngVel)*scalingBaseAngVel;
            t = System.currentTimeMillis() - startTime;
        } catch (IOChannelException e) {
            e.printStackTrace();
        }
    }
    public void writeOutput(double u) {
        try {
	    if(u > 2) {
		    u = 2;
	    } else if(u < -2) {
		    u = -2;
	    }
            analogU.set(u*scalingOutput);
        } catch (IOChannelException e) {
            e.printStackTrace();
        }
	//If we want to retain the value for other calculations
        this.u = u;
        plotSignals();
    }
    public void plotSignals() {
        double t = (double)this.t/1000;
	//1 black, 2 red, 3 green, 4 blue
        gui.putMeasurementDataPoint(t,baseAng,baseAngVel,pendAng,pendAngVel);
	//Top and 360 sensor seems to really follow each other
        //gui.putMeasurementDataPoint(t,pendAng,pendAngVel,pendAngTop,pendAngVelTop);
        gui.putControlDataPoint(t,u);
    }

    public synchronized void resetOffsets() {
        try {
            offsetPendAng = -(analogPendAng.get()*scalingPendAng - Math.PI)/scalingPendAng;
	    offsetPendAngVel = -analogPendAngVel.get();
	    offsetBaseAngVel = -analogBaseAngVel.get();
        } catch (IOChannelException e) {
            e.printStackTrace();
        }
    }
    public synchronized double getPendAngVel() {
        return pendAngVel;
    }

    public synchronized double getPendAngTop() {
        return pendAngTop;
    }

    public synchronized double getPendAngVelTop() {
        return pendAngVelTop;
    }

    public synchronized double getBaseAng() {
        return baseAng;
    }

    public synchronized double getBaseAngVel() {
        return baseAngVel;
    }

    public double getPendAng() {
        return pendAng;
    }
}
