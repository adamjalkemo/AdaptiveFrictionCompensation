package com.aaej;


import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt64;

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
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        communicationManager.stopSaveArrays();
        String fileName = new SimpleDateFormat("'rlsConverge-'yyyyMMddhhmmss'.mat'").format(new Date());
        saveData(fileName);
    }

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

    public void saveDataGeneral() {
        communicationManager.stopSaveArrays();
        String fileName = new SimpleDateFormat("'data-'yyyyMMddhhmmss'.mat'").format(new Date());
        saveData(fileName);
    }
}
