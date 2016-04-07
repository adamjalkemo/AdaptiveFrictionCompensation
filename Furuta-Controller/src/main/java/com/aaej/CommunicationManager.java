package com.aaej;

import se.lth.control.realtime.AnalogIn;
import se.lth.control.realtime.AnalogOut;
import se.lth.control.realtime.IOChannelException;

import java.util.logging.Logger;
import java.util.logging.Level;

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

    public double u;
    public double pendAng;
    public double pendAngVel;
    public double pendAngTop;
    public double pendAngVelTop;
    public double baseAng;
    public double baseAngVel;

    private long t;

    public CommunicationManager(FurutaGUI gui) {
        this.gui = gui;
        //TODO check which signal is which acutally (only guesses now)
        try {
            analogU = new AnalogOut(0);
            analogPendAng = new AnalogIn(2);
            analogPendAngVel = new AnalogIn(3);
            analogPendAngTop = new AnalogIn(6);
            analogPendAngVelTop = new AnalogIn(7);
            analogBaseAng = new AnalogIn(4);
            analogBaseAngVel = new AnalogIn(5);
        } catch (IOChannelException e) {
            e.printStackTrace();
        } catch (UnsatisfiedLinkError e) {
            LOGGER.log(Level.SEVERE, "Couldn't connect to analogbox");
            //System.exit(1);
        }
    }

    public void readInput() {
        try {
            //TODO: Fix scaling and offset
            pendAng = analogPendAng.get();
            pendAngVel = analogPendAngVel.get();
            pendAngTop = analogPendAngTop.get();
            pendAngVelTop = analogPendAngVelTop.get();
            baseAng = analogBaseAng.get();
            baseAngVel = analogBaseAngVel.get();
            t = System.currentTimeMillis();
        } catch (IOChannelException e) {
            e.printStackTrace();
        }
    }
    public void writeOutput(double u) {
        try {
            analogU.set(u);
        } catch (IOChannelException e) {
            e.printStackTrace();
        }
        this.u = u;
        plotSignals();
    }
    public void plotSignals() {
        gui.putMeasurementDataPoint(t,pendAng);
        gui.putMeasurementDataPoint(t,pendAngVel);
        gui.putMeasurementDataPoint(t,baseAng);
        gui.putMeasurementDataPoint(t,baseAngVel);
        gui.putControlDataPoint(t,u);
    }
}
