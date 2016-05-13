package com.aaej;

/**
 * This is the application that should be run.
 * It will create all necessary objects and initiate threads to the correct priorities.
 * Se classes for information about usage.
 */
public class App 
{
    public static void main( String[] args )
    {
        FurutaGUI gui = new FurutaGUI(5);
        CommunicationManager cm = new CommunicationManager(gui);
        MainController controller = new MainController(6,cm);
        gui.setController(controller);
        gui.setCommunicationManager(cm);
        gui.initializeGUI();
        
        // Start threads
        gui.start();
        controller.start();
    }
}
