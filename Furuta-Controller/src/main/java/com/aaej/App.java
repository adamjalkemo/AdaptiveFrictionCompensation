package com.aaej;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        FurutaGUI gui = new FurutaGUI(5);
        CommunicationManager cm = new CommunicationManager(gui);
        MainController controller = new MainController(6,cm);
        gui.setController(controller);
        gui.initializeGUI();

        // Start threads
        controller.run();
        gui.start();
    }
}
