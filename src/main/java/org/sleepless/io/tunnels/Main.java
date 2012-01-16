package org.sleepless.io.tunnels;

import java.io.File;

import javax.swing.UIManager;

public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        
        setNativeLookAndFeel();

        Tunnels t = new Tunnels(new File("config.properties"));
        t.start();
        
    }
    
    public static void setNativeLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
    }


}
