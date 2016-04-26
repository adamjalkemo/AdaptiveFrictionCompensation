package com.aaej;


import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLDouble;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SpecificTests {
    private CommunicationManager communicationManager;
    private MainController controller;
    public SpecificTests(CommunicationManager communicationManager, MainController controller) {
        this.communicationManager = communicationManager;
        this.controller = controller;
    }



    public void stepResponse() {
        communicationManager.startSaveArrays();
        communicationManager.changeOffsetBaseAng(Math.PI);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        communicationManager.stopSaveArrays();
        String fileName = new SimpleDateFormat("'stepResponse-'yyyyMMddhhmmss'.mat'").format(new Date());
        saveData(fileName);
    }

    public void rlsConverge() {
        controller.regulatorActive(false);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        controller.resetEstimator();
        controller.regulatorActive(true);
        communicationManager.startSaveArrays();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        communicationManager.stopSaveArrays();
        String fileName = new SimpleDateFormat("'rlsConverge-'yyyyMMddhhmmss'.mat'").format(new Date());
        saveData(fileName);
    }

    public void saveData(String file) {
        ArrayList data = new ArrayList();
        data.add(new MLDouble("u", (Double[])communicationManager.getuArray().toArray(), 1));
        data.add(new MLDouble("baseAng", (Double[])communicationManager.getBaseAngArray().toArray(), 1));
        data.add(new MLDouble("baseAngVel", (Double[])communicationManager.getBaseAngVelArray().toArray(), 1));
        data.add(new MLDouble("pendAng", (Double[])communicationManager.getPendAngArray().toArray(), 1));
        data.add(new MLDouble("pendAngVel", (Double[])communicationManager.getPendAngVelArray().toArray(), 1));
        data.add(new MLDouble("t", (Double[])communicationManager.gettArray().toArray(), 1));
        data.add(new MLDouble("fv", (Double[])communicationManager.getFvArray().toArray(), 1));
        data.add(new MLDouble("fc", (Double[])communicationManager.getFcArray().toArray(), 1));
        try {
            new MatFileWriter(file, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
