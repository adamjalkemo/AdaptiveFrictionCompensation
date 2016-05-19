package com.aaej;


import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt64;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
    This class holds functionality for saving data to a MATLAB .mat file (data is stored
    in CommunicationManager until it is written to file.
    There are also methods available for running simple step response tests etc.
 */
public class SpecificTests {
    private CommunicationManager communicationManager;
    private MainController controller;
    public SpecificTests(CommunicationManager communicationManager, MainController controller) {
        this.communicationManager = communicationManager;
        this.controller = controller;
    }

    /**
        Runs a test with two step response test (one in each direction) for phi.
        First it moves 180 deg counter clockwise direction, then back.
     */
    public void stepResponse() {
        //communicationManager.startSaveArrays();
        //threadSleep(3000);
        controller.setReference(Math.PI);
        threadSleep(3000);
        controller.setReference(0);
        //threadSleep(3000);
        
        //communicationManager.stopSaveArrays();
        //String fileName = new SimpleDateFormat("'stepResponse-'yyyyMMddhhmmss'.mat'").format(new Date());
        //saveData(fileName);
    }

    /**
    * Help method for sleep
    */
    private void threadSleep(long time) {
        try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }

    /**
        Stops the pendulum, resets RLS estimation. Runs the controller and plots its convergence
     */
    public void rlsConverge() {
        controller.regulatorActive(false);
        threadSleep(5000);
        controller.resetEstimator();
        controller.regulatorActive(true);
        communicationManager.startSaveArrays();
        threadSleep(20000);
        communicationManager.stopSaveArrays();
        String fileName = new SimpleDateFormat("'rlsConverge-'yyyyMMddhhmmss'.mat'").format(new Date());
        saveData(fileName);
    }

    /**
        This method saves data to a .mat file.
     */
    public void saveData(String file) {
        ArrayList data = new ArrayList();
        data.add(new MLDouble("u", communicationManager.getuArray().toArray(new Double[]{}), 1));
        data.add(new MLDouble("baseAng", communicationManager.getBaseAngArray().toArray(new Double[]{}), 1));
        data.add(new MLDouble("baseAngVel", communicationManager.getBaseAngVelArray().toArray(new Double[]{}), 1));
        data.add(new MLDouble("pendAng", communicationManager.getPendAngArray().toArray(new Double[]{}), 1));
        data.add(new MLDouble("pendAngVel", communicationManager.getPendAngVelArray().toArray(new Double[]{}), 1));
        data.add(new MLInt64("t", communicationManager.gettArray().toArray(new Long[]{}), 1));
        data.add(new MLDouble("uF", communicationManager.getuFArray().toArray(new Double[]{}), 1));
        data.add(new MLDouble("fv", communicationManager.getFvArray().toArray(new Double[]{}), 1));
        data.add(new MLDouble("fc", communicationManager.getFcArray().toArray(new Double[]{}), 1));
        data.add(new MLDouble("fo", communicationManager.getFoArray().toArray(new Double[]{}), 1));
        data.add(new MLDouble("VL", communicationManager.getVLArray().toArray(new Double[]{}), 1));
        try {
            new MatFileWriter(file, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
        If save data is called from the GUI, this method is called and below filename is used.
     */
    public void saveDataGeneral() {
        communicationManager.stopSaveArrays();
        String fileName = new SimpleDateFormat("'data-'yyyyMMddhhmmss'.mat'").format(new Date());
        saveData(fileName);
    }
}
