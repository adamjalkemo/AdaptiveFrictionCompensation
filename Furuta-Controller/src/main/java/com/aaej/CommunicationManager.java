package com.aaej;

import se.lth.control.realtime.AnalogIn;
import se.lth.control.realtime.AnalogOut;
import se.lth.control.realtime.IOChannelException;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.lang.Math;

/**
 * This is the class which handles the communication with the analog box.
 * It provides scaling and offsets for the signals and ensures that the control signal saturates.
 * There are also methods for saving data in arrays, which are then used by SpecificTests (which writes to file).
 */
public class CommunicationManager {
    private final static Logger LOGGER = Logger.getLogger(MainController.class.getName());
    private FurutaGUI gui;
    private AnalogOut analogU;
    private AnalogIn analogPendAng;
    private AnalogIn analogPendAngVel;
    private AnalogIn analogBaseAng;
    private AnalogIn analogBaseAngVel;

    private double u;
    private double pendAng;
    private double pendAngVel;
    private double baseAng;
    private double baseAngVel;

    private long t;
    private long startTime;

    private double offsetPendAng = 5.0673;
    private double scalingPendAng = 0.3091;

    private double offsetPendAngVel = -0.0350;
    private double scalingPendAngVel = 3.76;

    private double offsetBaseAng = 0.5;
    private double scalingBaseAng = 2.56;

    private double offsetBaseAngVel = 0.1482;
    private double scalingBaseAngVel = 2;

    private double scalingOutput = -1.40;

    private ArrayList<Double> uArray;
    private ArrayList<Double> uFArray;
    private ArrayList<Double> vLArray;
    private ArrayList<Double> pendAngArray;
    private ArrayList<Double> pendAngVelArray;
    private ArrayList<Double> baseAngArray;
    private ArrayList<Double> baseAngVelArray;
    private ArrayList<Long> tArray;
    private ArrayList<Double> fvArray;
    private ArrayList<Double> fcArray;
    private ArrayList<Double> foArray;
    private boolean saveArray = false;

    /*
     * Constructor, initiates communication with the analog box. Exits on error.
     */
    public CommunicationManager(FurutaGUI gui) {
        this.gui = gui;
	    startTime = System.currentTimeMillis();
        try {
            analogU = new AnalogOut(0);
            analogPendAng = new AnalogIn(6);
            analogPendAngVel = new AnalogIn(7);
            analogBaseAng = new AnalogIn(4);
            analogBaseAngVel = new AnalogIn(5);
        } catch (IOChannelException e) {
            e.printStackTrace();
        } catch (UnsatisfiedLinkError e) {
            LOGGER.log(Level.SEVERE, "Couldn't connect to analogbox");
            System.exit(1);
        }
    }

    /*
     *  Reads measurement values from the analog box. Adds offsets and then scales.
     *  If values are to be saved in array (for write to file), this happens here.
     *  This is called from the controller at every sampling instant.
     */
    public synchronized void readInput() {
        try {
            pendAng = (analogPendAng.get() + offsetPendAng) * scalingPendAng;
            pendAngVel = (analogPendAngVel.get() + offsetPendAngVel) * scalingPendAngVel;
            baseAng = (analogBaseAng.get() + offsetBaseAng) * scalingBaseAng;
            baseAngVel = (analogBaseAngVel.get() + offsetBaseAngVel) * scalingBaseAngVel;
            t = System.currentTimeMillis() - startTime;
            if(saveArray) {
                pendAngArray.add(pendAng);
                pendAngVelArray.add(pendAngVel);
                baseAngArray.add(baseAng);
                baseAngVelArray.add(baseAngVel);
                tArray.add(t);
            }
        } catch (IOChannelException e) {
            e.printStackTrace();
        }
    }

    /*
     * Called by the controller. Saturates if necessary.
     */
    public double writeOutput(double u) {
        try {
    	    if(u > 1) {
    		    u = 1;
    	    } else if(u < -1) {
    		    u = -1;
    	    }

            // If something goes wrong in the calculations, set u to 0.
            u = Double.isNaN(u) ? 0 : u;

            analogU.set(u * scalingOutput);
        } catch (IOChannelException e) {
            e.printStackTrace();
        }

	    //If we want to retain the value for other calculations
        this.u = u;
        if(saveArray) {
            uArray.add(u);
        }
        plotSignals();
        return u;
    }

    /*
     * Plot the measured signals and the control signal.
     */
    public void plotSignals() {
        double t = (double)this.t/1000;
	    //1 black, 2 red, 3 green, 4 blue
        gui.putMeasurementDataPoint(t,baseAng,baseAngVel,pendAng,pendAngVel);
        gui.putControlDataPoint(t,u);
    }

    /*
     * Plots the estimated friction coefficients
     */
    public void plotRLSParameters(double fv, double fc, double fo) {
        double t = (double)this.t/1000;
        gui.putRLSDataPoint(t, fv, fc, fo);
        if(saveArray) {
            fvArray.add(fv);
            fcArray.add(fc);
            foArray.add(fo);
        }
    }

    /*
     * Resets the offsets. Should be called when the pendulum is down and still.
     * Pi is subtracted because 0 is at the top.
     */
    public synchronized void resetOffsets() {
        try {
            offsetPendAng = -(analogPendAng.get()*scalingPendAng - Math.PI)/scalingPendAng;
    	    offsetPendAngVel = -analogPendAngVel.get();
            offsetBaseAng = -analogBaseAng.get();
    	    offsetBaseAngVel = -analogBaseAngVel.get();
        } catch (IOChannelException e) {
            e.printStackTrace();
        }
    }

    /*
     * When the controller switches to top controller it might be a bit aggressive depending on what value
     * phi has. We solve this by resetting phi by manipulating the offsets.
     */
    public synchronized void resetOffsetBaseAng() {
        offsetBaseAng = -(baseAng/scalingBaseAng - offsetBaseAng);
    }

    // ---------- Get methods for measurements ----------
    public synchronized double getPendAngVel() {
        return pendAngVel;
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
    // ----------

    /*
     * Called when values are to be saved for later use (e.g print to file).
     */
    public synchronized void startSaveArrays() {
        uArray = new ArrayList<Double>();
        uFArray = new ArrayList<Double>();
        vLArray = new ArrayList<Double>();
        baseAngArray = new ArrayList<Double>();
        baseAngVelArray = new ArrayList<Double>();
        pendAngArray = new ArrayList<Double>();
        pendAngVelArray = new ArrayList<Double>();
        tArray = new ArrayList<Long>();
        fvArray = new ArrayList<Double>();
        fcArray = new ArrayList<Double>();
        foArray = new ArrayList<Double>();
        saveArray = true;
    }

    public synchronized void stopSaveArrays() {
        saveArray = false;
    }

    // ---------- Get methods for measurement arrays ----------
    public ArrayList<Double> getuArray() {
        return uArray;
    }

    public ArrayList<Double> getuFArray() {
        return uFArray;
    }

    public ArrayList<Double> getVLArray() {
        return vLArray;
    }

    public ArrayList<Double> getPendAngArray() {
        return pendAngArray;
    }

    public ArrayList<Double> getPendAngVelArray() {
        return pendAngVelArray;
    }

    public ArrayList<Double> getBaseAngArray() {
        return baseAngArray;
    }

    public ArrayList<Double> getBaseAngVelArray() {
        return baseAngVelArray;
    }

    public ArrayList<Long> gettArray() {
        return tArray;
    }

    public ArrayList<Double> getFvArray() {
        return fvArray;
    }

    public ArrayList<Double> getFcArray() {
        return fcArray;
    }

    public ArrayList<Double> getFoArray() {
        return foArray;
    }

    public void saveUF(double uF) {
        if(saveArray) {
            uFArray.add(uF);
        }
    }

    public void saveVL(double vL) {
        if(saveArray) {
            vLArray.add(vL);
        }
    }
    // ----------
}
